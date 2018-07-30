package com.radixdlt.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;

public class Server extends Thread {
    

    protected Socket socket;
    private MicrofundingApp app;

    public Server(Socket socket, MicrofundingApp app) {
        this.socket = socket;
        this.app = app;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        Date today = new Date();
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
        try {
			this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        start();
    }
    
    public void setApp(MicrofundingApp app) {
    	this.app = app;
    }
    

    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String request;
            while ((request = br.readLine()) != null) {
                System.out.println("Contract Key?:" + request);
                
                String state = app.getState(request);
                System.out.println(state);
                state += '\n';
                out.write(state.getBytes());
            }

        } catch (IOException ex) {
            System.out.println("Unable to get streams from client");
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}