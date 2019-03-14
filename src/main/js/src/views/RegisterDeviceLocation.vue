<template>
    <v-layout align-center justify-center>
        <v-flex xs12 sm10 md8>
            <form-register-device-location v-bind:deviceName="deviceName" v-bind:deviceGroup="deviceGroup" />
        </v-flex>
        <ul>
            <li>Fill out the form to register for an account.</li>
            <li>Click on 'Submit' to agree and create the account with those details.</li>
        </ul>
    </v-layout>
</template>

<script>
    import FormRegisterDeviceLocation from '../forms/RegisterDeviceLocation.vue'

    export default {
        data() {
            return {
            }
        },
        props: {
            deviceName: {
              type: String,
              required: true
          },
            deviceGroup: {
              type: String,
                required: true
            }
        },
        methods: {
        },
        components: {
            FormRegisterDeviceLocation
        }
    }
</script>


<style scoped>
</style>
