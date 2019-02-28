<template v-if="calculatedConsists.length > 0">
    <div style="position: absolute; left: 50%;">
        <v-btn color="normal" @click="routeToCandidateConsists">Click here to view candidate consists</v-btn>
        <h3>Calculated Consists from {{calculatedConsists[0].deviceName}}</h3>
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
                axios.get(`/rest/webhook/consist/${this.deviceId}`).then(response => {
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
