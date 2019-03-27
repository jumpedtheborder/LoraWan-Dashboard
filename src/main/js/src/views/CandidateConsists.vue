<template v-if="candidateConsists.length > 0">
    <div>
        <v-btn color="normal" @click="routeToCalculatedConsists">Click here to view calculated consists</v-btn>
        <h3 v-if="candidateConsists.length == 0">There are no candidate consists available. This means that the device has not received a consist, or it has already been calculated</h3>
        <div v-else>
            <h3>Candidate Consists from {{candidateConsists[0].deviceName}}</h3>
            <table>
                <thead>
                <tr bgcolor="#80ffd4">
                    <th>Time</th>
                    <th>Candidate consist</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="candidate in candidateConsists">
                    <td>{{candidate.formattedTime}}</td>
                    <td class="color">{{candidate.candidateConsist}}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
    import axios from 'axios'
    export default {
        data() {
            return {
                candidateConsists: [],
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
                axios.get(`/rest/webhook/candidateConsist/${this.deviceId}`).then(response => {
                    this.candidateConsists = response.data
                }).catch()
            },
            routeToCalculatedConsists() {
                this.$router.push(`/device/${this.deviceId}/calculatedConsists`)
            }
        },
        mounted() {
            this.getCandidateConsists();
        }
    }
</script>


<style scoped>

</style>
