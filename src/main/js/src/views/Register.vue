<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <form-register v-on:success="eventLogin"/>
    </v-flex>
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
