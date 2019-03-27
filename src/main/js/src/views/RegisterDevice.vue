<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <form-register-device/>
    </v-flex>
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
