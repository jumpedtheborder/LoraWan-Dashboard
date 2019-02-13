<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <form-register v-on:success="eventLogin"/>
    </v-flex>
        <ul>
          <li>Fill out the form to register for an account.</li>
          <li>Click on 'Submit' to agree and create the account with those details.</li>
        </ul>
  </v-layout>
</template>

<script>
import FormRegister from '../forms/Register'

export default {
  data() {
    return {

    }
  },
  methods: {
    eventLogin(user) {
      global.app.eventLogin(user)
    }
  },
  components: {
    FormRegister
  }
}
</script>


<style scoped>
</style>
