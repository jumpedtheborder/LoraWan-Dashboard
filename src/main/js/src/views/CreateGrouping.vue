<template>
    <v-card>
        <v-card-title>
            <h3 class="headline mb-0">Create a group</h3>
        </v-card-title>
        <v-card-text>
            <div :v-if="!!message" class="red--text">
                <p>{{message}}</p>
            </div>
            <v-form ref="form">
                <v-layout row wrap>
                    <v-flex xs12 md6>
                        <p>Please type the name of the group that you wish to create</p>
                        <v-text-field type="text" label="Group name" v-model="groupName" placeholder="Group name" :rules="groupRules"></v-text-field>
                    </v-flex>
                </v-layout>
            </v-form>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="createGroup">Create the group</v-btn>
        </v-card-actions>
    </v-card>
</template>

    <script>
    import axios from 'axios'

    export default {
        data() {
            return {
                groupName: '',
                message: '',
                groupRules: [
                    v => !!v || 'Group name is required',
                    v => v && v.length <= 100 || 'Group name must be less than 30 characters'
                ]
            }
        },
        methods: {
            createGroup() {
                axios.post("/rest/createGroup", {
                    groupName: this.groupName
                }).catch(err => {
                    this.message = err.response.data && err.response.data.message ? 'Error: ' + err.response.data.message : err.message
                }).finally(
                    this.$router.push('/map')
                )
            }
        }
    }
</script>

<style scoped>

</style>