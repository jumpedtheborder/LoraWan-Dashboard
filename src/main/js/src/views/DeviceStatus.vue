<template>
    <v-card>
        <v-card-title primary-title>
            <div class="deviceStatus"><strong>Device Name: {{status.deviceName}}</strong></div>
        </v-card-title>
        <v-card-text class="text-xs-left">
            <div>
                <div><b>Device Latitude: </b> {{status.latitude}}</div>
                <div><b>Device Longitude: </b>{{status.longitude}}</div>
                <div><b>Device Region: </b>{{status.region}}</div>
                <div><b>Device Group: </b>{{status.group}}</div>
                <div><b>Order value from left to right: </b>{{status.groupOrder}}</div>
                <div><b>Device Voltage (last reported): </b>{{status.batteryLevel}}</div>
                <br>
            </div>

            <v-btn color="normal" @click="routeToCandidateConsists">Click here to view candidate consists</v-btn>
            <v-btn color="normal" @click="routeToCalculatedConsists">Click here to view calculated consists</v-btn>

        </v-card-text>
    </v-card>
</template>

<script>
    import axios from 'axios'
    export default {
        data() {
            return {
                status: []
            }
        },
        props: {
            deviceId: {
                type: String,
                required: true
            }
        },
        methods: {
            getDeviceStatus() {
                axios.get(`/rest/device/status/${this.deviceId}`).then(response => {
                    this.status = response.data
                }).catch()
            },
            routeToCalculatedConsists() {
                this.$router.push(`/device/${this.deviceId}/calculatedConsists`)
            },
            routeToCandidateConsists() {
                this.$router.push(`/device/${this.deviceId}/candidateConsists`)
            }
        },
        mounted() {
            this.getDeviceStatus();
        }
    }
</script>

<style scoped>

</style>