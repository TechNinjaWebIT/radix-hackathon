package com.radixdlt.examples;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.radixdlt.client.assets.Asset;
import com.radixdlt.client.core.Bootstrap;
import com.radixdlt.client.core.RadixUniverse;
import com.radixdlt.client.core.address.RadixAddress;
import com.radixdlt.client.core.atoms.ApplicationPayloadAtom;
import com.radixdlt.client.core.atoms.Payload;
import com.radixdlt.client.core.atoms.TransactionAtom;
import com.radixdlt.client.core.atoms.UnsignedAtom;
import com.radixdlt.client.core.atoms.AtomBuilder;
import com.radixdlt.client.core.identity.EncryptedRadixIdentity;
import com.radixdlt.client.core.identity.RadixIdentity;
import com.radixdlt.client.core.identity.SimpleRadixIdentity;
import com.radixdlt.client.core.network.AtomSubmissionUpdate;
import com.radixdlt.client.core.network.AtomSubmissionUpdate.AtomSubmissionState;
import com.radixdlt.client.messaging.RadixMessaging;
import com.radixdlt.client.wallet.RadixWallet;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * A service which sends tokens to whoever sends it a message through
 * a Radix Universe.
 */

class MicrofundingContract {
	private final static long DELAY = 1000 * 5; //5 seconds
	private final RadixAddress companyAddress;
	private final RadixAddress teamAddress;
	private int totalAmount = 0;
	private int dailyAllowance = 0;
	private int withdrawableAmount = 0;
	private long lastUpdateTime;
	
	private final RadixIdentity contractAddress;
	
	public void setDailyAllowance(int dailyAllowance) {
		this.withdrawableAmount += this.dailyAllowance * (new Date().getTime() - lastUpdateTime)/DELAY;
		this.dailyAllowance = dailyAllowance;	
	}
	
	public int getWithrawableAmount() {
		this.withdrawableAmount += this.dailyAllowance * (new Date().getTime() - lastUpdateTime)/DELAY;
		return this.withdrawableAmount;
	}
	
	public int getWithdrawal() {
		this.withdrawableAmount += this.dailyAllowance * (new Date().getTime() - lastUpdateTime)/DELAY;
		int amount = Math.min(this.withdrawableAmount, this.totalAmount);
		this.totalAmount -= amount;
		this.withdrawableAmount = 0;
		return amount;
	}
	
	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	
	public int getDailyAllowance() {
		return this.dailyAllowance;
	}
	
	public int getTotalAmountLeft() {
		return this.totalAmount;
	}
	
	public RadixAddress getCompanyAddress() {
		return this.companyAddress;
	}
	
	public RadixAddress getTeamAddress() {
		return this.teamAddress;
	}
	
	public MicrofundingContract(RadixIdentity radixIdentity, RadixAddress companyAddress, RadixAddress teamAddress, int totalAmount) {
		this.contractAddress = radixIdentity;
		this.companyAddress = companyAddress;
		this.teamAddress = teamAddress;
		this.totalAmount = totalAmount;
		this.lastUpdateTime = new Date().getTime();
	}
	
	public void process(JsonObject payload) {
		System.out.println(payload.get("name"));
	}
	
}

public class MicrofundingApp {

	private final RadixIdentity radixIdentity;
	private final static long DELAY = 1000 * 60 * 1;
	public static final int PORT_NUMBER = 8081;
	private Hashtable<BigInteger, MicrofundingContract> contracts = new Hashtable<BigInteger, MicrofundingContract>();
	
	private MicrofundingApp(RadixIdentity radixIdentity) {
		this.radixIdentity = radixIdentity;
	}
	
	private void initContract(RadixAddress sender, BigInteger contractId, JsonObject data) {
		if (contracts.containsKey(contractId)) {
			sendContractIdExistsMessage(sender);
			return;
		}
		contracts.put(contractId, new MicrofundingContract(radixIdentity, sender, new RadixAddress(data.get("receiver").getAsString()), data.get("totalAmount").getAsInt()));
		contracts.get(contractId).setDailyAllowance(data.get("initialAllowance").getAsInt());
		
	}
	
	private void setAllowance(RadixAddress sender, BigInteger contractId, JsonObject data) {
		if (!contracts.containsKey(contractId)) {
			sendContractIdNonExistingMessage(sender);
			return;
		}
		if (!contracts.get(contractId).getCompanyAddress().equals(sender)) {
			sendWrongSenderMessage(sender);
			return;
		}
		contracts.get(contractId).setDailyAllowance(data.get("newAllowance").getAsInt());
	}
	
	private void destroyContract(RadixAddress sender, BigInteger contractId, JsonObject data) {
		if (!contracts.get(contractId).getCompanyAddress().equals(sender)) {
			sendWrongSenderMessage(sender);
			return;
		}
		MicrofundingContract contract = contracts.get(contractId);
		if (contract.getTotalAmountLeft() != 0) {
			RadixWallet.getInstance().transferXRD(contract.getTotalAmountLeft(), radixIdentity, sender).subscribe(System.out::println);
		}	
		contracts.remove(contractId);
	}
	
	private void withdraw(RadixAddress sender, BigInteger contractId, JsonObject data) {
		if (!contracts.get(contractId).getTeamAddress().equals(sender)) {
			sendWrongSenderMessage(sender);
			return;
		}
		MicrofundingContract contract = contracts.get(contractId);
		int amount = contract.getWithdrawal();
		if (amount != 0) {
			RadixWallet.getInstance().transferXRD(amount, radixIdentity, sender).subscribe(System.out::println);
		}	
	}
	
	private void sendAppAtom(RadixAddress address, JsonObject payload, String appId) {
		
		AtomBuilder atomBuilder = new AtomBuilder()
				   .type(ApplicationPayloadAtom.class)
				   .applicationId(appId)
				   .payload(payload.getAsString())
				   .addDestination(address);

		UnsignedAtom unsignedAtom = atomBuilder.buildWithPOWFee(RadixUniverse.getInstance().getMagic(), radixIdentity.getPublicKey());
		radixIdentity.sign(unsignedAtom).flatMapObservable(RadixUniverse.getInstance().getLedger()::submitAtom).subscribe(System.out::println);
		
	}
	
	private void sendMessage(RadixAddress address, String messageContent) {
		RadixMessaging.getInstance().sendMessage(messageContent, radixIdentity, address).subscribe(System.out::println);
	}
	
	private void sendWrongCallMessage(RadixAddress address) {
		sendMessage(address, "Contract call unknown");
	}
	
	private void sendWrongSignatureMessage(RadixAddress address) {
		sendMessage(address, "Authenticity check failed: wrong signature");
	}
	
	private void sendContractIdExistsMessage(RadixAddress address) {
		sendMessage(address, "Init failed: Contract id already exists");
	}
	
	private void sendContractIdNonExistingMessage(RadixAddress address) {
		sendMessage(address, "Action failed: no such contract id");
	}
	
	private void sendWrongSenderMessage(RadixAddress address) {
		sendMessage(address, "Action failed: the sender is invalid");
	}

	public void run() {
		final RadixAddress sourceAddress = RadixUniverse.getInstance().getAddressFrom(radixIdentity.getPublicKey());

		System.out.println("Contract server address:" + sourceAddress);

		// Print out current balance of faucet
		RadixWallet.getInstance().getSubUnitBalance(sourceAddress, Asset.XRD)
			.subscribe(
				balance -> System.out.println("Remaining Balance: " + ((double)balance) / Asset.XRD.getSubUnits()),
				Throwable::printStackTrace
			)
		;
		
		RadixUniverse.getInstance()
			.getLedger()
			.getAllAtoms(sourceAddress.getUID(), ApplicationPayloadAtom.class)
			.filter(atom -> atom.getApplicationId().equalsIgnoreCase("Microfunding"))
			.filter(message -> Math.abs(message.getTimestamp() - System.currentTimeMillis()) < 60000) // Only deal with recent messages
			.subscribe(atom -> {
				JsonObject payload = new JsonParser().parse(new String(atom.getPayload().getBytes())).getAsJsonObject();
				RadixAddress senderAddress = new RadixAddress(payload.get("sender").getAsString());
				
				System.out.println("Sender: " + senderAddress);
				
				if (atom.getSignature(senderAddress.getUID()) == null) {
					sendWrongSignatureMessage(senderAddress);
				}
				if (payload.get("contractCall") == null) {
					return;
				}
				System.out.println(payload.get("contractCall").getAsString());
				
				switch(payload.get("contractCall").getAsString()) {
					case "init":
						initContract(senderAddress, payload.get("contractId").getAsBigInteger(), payload.get("data").getAsJsonObject());
						break;
					case "setAllowance":
						setAllowance(senderAddress, payload.get("contractId").getAsBigInteger(), payload.get("data").getAsJsonObject());
						break;
					case "destroyContract":
						destroyContract(senderAddress, payload.get("contractId").getAsBigInteger(), payload.get("data").getAsJsonObject());
						break;
					case "withdraw":
						withdraw(senderAddress, payload.get("contractId").getAsBigInteger(), payload.get("data").getAsJsonObject());
					default:
						sendWrongCallMessage(senderAddress);
						break;
				}
			});
			
	}
	
	public String getState(String key) {
		String state = "";
		BigInteger contractKey =  new BigInteger(key);
		MicrofundingContract microfundingContract = contracts.get(contractKey);
		state += "withdrawable amount: "+microfundingContract.getWithrawableAmount()+", daily allowance: " + microfundingContract.getDailyAllowance() +", total amount: "+ microfundingContract.getTotalAmountLeft();
		return state;
	}
	
	private static void initServer(MicrofundingApp app) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(PORT_NUMBER);
			new Server(server.accept(), app);
			
		} catch (IOException ex) {
			System.out.println("Unable to start server.");
		} finally {
			try {
				if (server != null)
					server.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Simple Rate Limiter helper class
	 */
	private static class RateLimiter {
		private final AtomicLong lastTimestamp = new AtomicLong();
		private final long millis;
		
		private RateLimiter(long millis) {
			this.millis = millis;
		}
		
		String getTimeLeftString() {
			long timeSince = System.currentTimeMillis() - lastTimestamp.get();
			long secondsTimeLeft = ((DELAY - timeSince) / 1000) % 60;
			long minutesTimeLeft = ((DELAY - timeSince) / 1000) / 60;
			return minutesTimeLeft + " minutes and " + secondsTimeLeft + " seconds";
		}
		
		boolean check() {
			return lastTimestamp.get() == 0 || (System.currentTimeMillis() - lastTimestamp.get() > millis);
		}
		
		void reset() {
			lastTimestamp.set(System.currentTimeMillis());
		}
	}
	
	

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java com.radixdlt.client.services.Faucet <highgarden|sunstone|winterfell|winterfell_local> <keyfile> <password>");
			System.exit(-1);
		}

		RadixUniverse.bootstrap(Bootstrap.valueOf(args[0].toUpperCase()));

//		RadixUniverse.getInstance()
//			.getNetwork()
//			.getStatusUpdates()
//			.subscribe(System.out::println);

		final RadixIdentity faucetIdentity = new EncryptedRadixIdentity(args[2], args[1]);
		MicrofundingApp faucet = new MicrofundingApp(faucetIdentity);
		faucet.run();
		initServer(faucet);
	}
}
