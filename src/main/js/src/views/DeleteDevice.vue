<template>
    <v-card>
        <v-card-title>
            <h3 class="headline mb-0">Delete a device</h3>
        </v-card-title>
        <v-card-text>
            <v-form ref="form">
                <v-layout row wrap>
                    <v-flex xs12 md6>
                        <p>By deleting a device, all webhooks, battery reports, pairings and the device itself shall be deleted. Please ensure you select the correct device before proceeding</p>
                        <v-select v-model="deviceName" :items="devices" label="Devices to delete"></v-select>
                    </v-flex>
                </v-layout>
            </v-form>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="deleteDevice">Delete the device</v-btn>
        </v-card-actions>
    </v-card>
</template>

    <script>
    import axios from 'axios'

    export default {
        data() {
            return {
                devices: [],
                deviceName: ''
            }
        },
        methods: {
            getAllDevices() {
                axios.get('/rest/getDeviceNames').then(response => {
                    this.devices = response.data
                })
            },
            deleteDevice() {
                axios.delete(`/rest/device/${this.deviceName}`).catch().finally(
                    this.$router.push('/map')
                )
            }
        },
        mounted() {
            this.getAllDevices()
        }
    }
</script>

<style scoped>

</style>