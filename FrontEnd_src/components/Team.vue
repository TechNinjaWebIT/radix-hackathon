<template lang="pug">
  div
    h3 Team 1
    // input.form-control(type="text", placeholder="Application ID..", v-model="applicationId")
    // input.form-control(type="text", placeholder="Destination addresses (separated by comma)..", v-model="destinations")
    // textarea.form-control(rows="5", placeholder="Message..", v-model="message")
    // div(style="display: flex; align-items: center; justify-content: flex-end")
    //   div.form-check(style="margin-right: 20px; margin-bottom: 0")
    //     input.form-check-input(type="checkbox", value="", id="encryptedCheck", v-model="encrypted")
    //     label.form-check-label(for="encryptedCheck") Encrypted
    //   button.btn.btn-outline-secondary(type="button", @click="sendApplicationMessage()") SEND
    //   button.btn.btn-outline-secondary(type="button", @click="sendMessageToUser()") SEND To User
    //   button.btn.btn-outline-secondary(type="button", @click="sendTokensToUser()") SEND Tokens
    
    br
    div.row
      div.column
        label Remaining funds: 300 RAD
    div.row
      div.column
        label Available withdraw: 30 RAD
</template>

<script>
import { connectApp } from 'radixdlt-js-lite'

export default {
  name: 'SendApplicationMessage',
  props: ['token'],
  data () {
    return {
      applicationId: '',
      destinations: '',
      message: '',
      encrypted: true
    }
  },
  methods: {
    sendApplicationMessage () {
      connectApp(this.token)
        .then(function(radixConnection) {
          const messaging = radixConnection.getMessaging()

          // Send application message
          messaging.sendApplicationMessage(
            this.applicationId,
            this.destinations.split(','),
            this.message,
            this.encrypted
          ).subscribe(
            response => console.log(`Your application message was succesfully submitted: ${response}`),
            error => console.log(`Sorry, something went wrong: ${error}`)
          )
        })
        .catch(error => {
          console.log(`Error while re-using a token: ${error}`)
        })
    },
    sendMessageToUser () {
      connectApp(this.token)
        .then(radixConnection => { 
          const messaging = radixConnection.getMessaging();
          
          //Send to the user
          messaging.sendMessage('9enesZWdWZqgA4EcevbysZj7dW533TVoM3YY9TZ4268WH8R9xF8', 'Test message to Chris')
          // messaging.sendMessage('9hBR5amQKoKf1zqsHLqNLU7Goxp9eRseDGruSjs7wZCebBRrScQ', 'Test message to Team')
          .subscribe(
            response => console.log('Your message was succesfully submitted: ${JSON.stringify(response)}'),
            error => console.log('Sorry, something went wrong: ${JSON.stringify(error)}')
          );
        })
    },
    sendTokensToUser () {
      connectApp(this.token)
        .then(radixConnection => {
          const wallet = radixConnection.getWallet();
          wallet.sendTransaction('9enesZWdWZqgA4EcevbysZj7dW533TVoM3YY9TZ4268WH8R9xF8', 'TEST', 3, 'Sending 3 tokens')
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${JSON.stringify(response)}`),
              error => console.log(`Sorry, something went wrong: ${JSON.stringify(error)}`)
            );
         })
    }
  }
}
</script>
