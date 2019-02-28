<template v-if="candidateConsists.length > 0">
    <div style="position: absolute; left: 50%;">
        <v-btn color="normal" @click="routeToCalculatedConsists">Click here to view calculated consists</v-btn>
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
                axios.get(`/rest/webhook/consist/${this.deviceId}`).then(response => {
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
