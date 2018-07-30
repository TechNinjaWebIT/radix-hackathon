<template lang="pug">
  div
    h3 Projects:
    // input.form-control(type="text", placeholder="Destination address..", v-model="destination")
    // input.form-control(type="text", placeholder="Asset ISO..", v-model="asset", disabled)
    // input.form-control(type="number", placeholder="Min: 0.00001", min="0.00001", step="0.00001", v-model="quantity")
    // textarea.form-control(rows="4", placeholder="Optional message..", v-model="message")
    // div(style="display: flex; align-items: center; justify-content: flex-end")
    //   button.btn.btn-outline-secondary(type="button", @click="sendTransaction()") SEND
    // div(style="display: flex; align-items: center; justify-content: flex-start")
    //   button.btn.btn-outline-secondary(type="button", @click="sendMessageToUser()") SEND Message
    // br
    // div(style="display: flex; align-items: center; justify-content: flex-start")
    //   button.btn.btn-outline-secondary(type="button", @click="sendTokensToUser()") SEND Tokens
    
    .row
      .col-md-4.col-lg-3.column
        img.logo(style='height: 80px; float: center', src='../assets/logo.png')
      .col-md-8.col-lg-3.column
        img.logo(style='height: 80px; float: center', src='../assets/logo.png')
      .col-md-12.col-lg-3.column
        img.logo(style='height: 80px; float: center', src='../assets/logo1.jpg')
      .col-md-16.col-lg-3.column
        img.logo(style='height: 80px; float: center', src='../assets/logo1.jpg')
    .row
      .col-md-4.col-lg-3.column(style='padding-left: 110px;')
        button.btn.btn-outline-secondary(type='button', @click='sendTokensToFirstPr') FUND
      .col-md-8.col-lg-3.column(style='padding-left: 110px;')
        button.btn.btn-outline-secondary(type='button', @click='sendTokensToSecondPr') FUND
      .col-md-12.col-lg-3.column(style='padding-left: 110px;')
        button.btn.btn-outline-secondary(type='button', @click='sendTransaction()') FUND
      .col-md-16.col-lg-3.column(style='padding-left: 110px;')
        button.btn.btn-outline-secondary(type='button', @click='sendTransaction()') FUND

    br
    br
    div.row
      div.column
        label Funding amount:
      div.column
        input.form-control(type="text", placeholder="RAD", style='float: left;')
      div.column(style='padding-left: 80px;')
        label Daily amount:
      div.column
        input.form-control(type="text", placeholder="RAD", style='float: left;')
    br
    br
    br
    // div.row
    //   div(style="display: flex; align-items: center; justify-content: flex-start")
    //     span Select: 
    //     select(v-model='selected')
    //       option(disabled='', value='') Please select one
    //       option(value='1') Send to Chris
    //       option(value='2') Send to Team 1
    //       option(value='3') Send to Team 2
    // div.row
    //   span Selected: {{ selected }}
    // br
    // div.row
    //   div(style="display: flex; align-items: center; justify-content: flex-start")
    //     button.btn.btn-outline-secondary(type="button", @click="sendToSelected()") SEND Tokens

</template>

<script>
import { connectApp } from 'radixdlt-js-lite'

export default {
  name: 'SendTransaction',
  props: ['token'],
  data () {
    return {
      destination: '',
      asset: 'TEST',
      quantity: 0.00001,
      message: '',
      selected: ''
    }
  },
  methods: {
    sendTransaction () {
      connectApp(this.token)
        .then(radixConnection => {
          const wallet = radixConnection.getWallet()

          // Send transaction
          wallet.sendTransaction(this.destination, this.asset, this.quantity, this.message)
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${response}`),
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
          wallet.sendTransaction('9f2QbKEQVSBwAih5XTSz7RJTT4esMmxJDqxK2Z2nkcYHrjcFAoT', 'TEST', 3, 'Sending 3 tokens')
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${JSON.stringify(response)}`),
              error => console.log(`Sorry, something went wrong: ${JSON.stringify(error)}`)
            );
         })
    },
    sendTokensToFirstPr () {
      connectApp(this.token)
        .then(radixConnection => {
          const wallet = radixConnection.getWallet();
          wallet.sendTransaction('9enesZWdWZqgA4EcevbysZj7dW533TVoM3YY9TZ4268WH8R9xF8', 'TEST', 3, 'Sending 3 tokens')
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${JSON.stringify(response)}`),
              error => console.log(`Sorry, something went wrong: ${JSON.stringify(error)}`)
            );
         })
    },
    sendTokensToSecondPr () {
      connectApp(this.token)
        .then(radixConnection => {
          const wallet = radixConnection.getWallet();
          wallet.sendTransaction('9f2QbKEQVSBwAih5XTSz7RJTT4esMmxJDqxK2Z2nkcYHrjcFAoT', 'TEST', 3, 'Sending 3 tokens')
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${JSON.stringify(response)}`),
              error => console.log(`Sorry, something went wrong: ${JSON.stringify(error)}`)
            );
         })
    },
    sendToSelected() {
      var selectedAddress;
      if (this.selected == 1) {
        selectedAddress = '9enesZWdWZqgA4EcevbysZj7dW533TVoM3YY9TZ4268WH8R9xF8';
      } else if(this.selected == 2) {
        selectedAddress = '9f2QbKEQVSBwAih5XTSz7RJTT4esMmxJDqxK2Z2nkcYHrjcFAoT';
      }
      connectApp(this.token)
        .then(radixConnection => {
          const wallet = radixConnection.getWallet();
          wallet.sendTransaction(selectedAddress, 'TEST', 3, 'Sending 3 tokens')
            .subscribe(
              response => console.log(`Your transaction was succesfully submitted: ${JSON.stringify(response)}`),
              error => console.log(`Sorry, something went wrong: ${JSON.stringify(error)}`)
            );
         })
      console.log(this.selected);
      console.log(selectedAddress);
    }
  }
}
</script>
