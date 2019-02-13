<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <form-register-device/>
    </v-flex>
        <ul>
          <li>Fill out the form to register for an account.</li>
          <li>Click on 'Submit' to agree and create the account with those details.</li>
        </ul>
  </v-layout>
</template>

<script>
import FormRegisterDevice from '../forms/RegisterDevice.vue'

export default {
  data() {
    return {

    }
  },
  methods: {
  },
  components: {
      FormRegisterDevice
  }
}
</script>


<style scoped>
</style>
