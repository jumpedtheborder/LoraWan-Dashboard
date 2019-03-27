<template v-if="calculatedConsists.length > 0">
    <div>
        <v-btn color="normal" @click="routeToCandidateConsists">Click here to view candidate consists</v-btn>
        <h3 v-if="calculatedConsists.length == 0">There are no calculated consists available. This means that the device has not received a consist, or a consist has yet to be calculated</h3>
        <h3 v-else>Calculated Consists from {{calculatedConsists[0].deviceName}}</h3>
        <table>
            <thead>
            <tr bgcolor="#80ffd4">
                <th>Time</th>
                <th>Calculated consist</th>
                <th>Direction</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="calculated in calculatedConsists">
                <td>{{calculated.formattedTime}}</td>
                <td class="color">{{calculated.candidateConsist}}</td>
                <td>{{calculated.direction}}</td>
            </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
    import axios from 'axios'
    export default {
        data() {
            return {
                calculatedConsists: [],
            }
        },
        props: {
            deviceId: {
                type: String,
                required: true
            }
        },
        methods: {
            getCandidateConsists() {
                axios.get(`/rest/webhook/calculatedConsist/${this.deviceId}`).then(response => {
                    this.calculatedConsists = response.data
                }).catch()
            },
            routeToCandidateConsists() {
                this.$router.push(`/device/${this.deviceId}/candidateConsists`)
            }
        },
        mounted() {
            this.getCandidateConsists();
        }
    }
</script>


<style scoped>

</style>
