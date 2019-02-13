<template>
    <div style="float: left; margin-left: 50px;">
        <h3>Candidate Consists from {{pairing[0].deviceName}}</h3>
        <table>
            <thead>
            <tr bgcolor="#80ffd4">
                <th>Time</th>
                <th>Candidate consist</th>
            </tr>
            </thead>
            <tbody v-if="device1 != null">
                <tr v-for="deviceReport in device1">
                    <td>{{deviceReport.dateTime}}></td>
                    <td class="color">{{deviceReport.candidate_consist}}</td>
                </tr>
            </tbody>
        </table>
    </div>
    <div style="float: right; margin-right: 50px;">
        <h3>Candidate Consists from {{pairing[1].deviceName}}</h3>
        <table>
            <thead>
            <tr bgcolor="#80ffd4">
                <th>Time</th>
                <th>Candidate consist</th>
            </tr>
            </thead>
            <tbody v-if="device2 != null">
            <tr v-for="deviceReport in device2">
                <td>{{deviceReport.dateTime}}></td>
                <td class="color">{{deviceReport.candidate_consist}}</td>
            </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
    export default {
        name: "CandidateConsists",
        pairing: [],
        device1: [],
        device2: []
    }
</script>

<style scoped>

</style>