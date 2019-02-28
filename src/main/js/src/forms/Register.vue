<template>
  <v-card>
    <v-card-title>
      <h3 class="headline mb-0">Create a new account for an employee</h3>
    </v-card-title>
    <v-card-text>
      <v-form v-model="valid" ref="form">
        <v-layout row wrap>
          <v-flex xs12 md6>
            <p>Credentials:</p>
            <v-text-field type="text" label="E-Mail" v-model="user.username" placeholder="Email address" :rules="usernameRules"></v-text-field>
            <v-text-field type="password" label="Password" v-model="user.password" placeholder="Password" :rules="passwordRules"></v-text-field>
            <v-select v-model="user.isAdmin" :items="user.isAdmin" label="Is this user an administrator?"></v-select>
            <v-select v-model="user.regions" :items="user.regions" label="Region"></v-select>
          </v-flex>
        </v-layout>
        <div :v-if="!!message" class="red--text">
          <p>{{message}}</p>
        </div>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn flat @click="clear">Close</v-btn>
      <v-btn color="primary" @click="submit">Submit</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      valid: false,
      message: '',
      user: {
        username: '',
        password: '',
        regions: [],
        isAdmin: ["Yes", "No"]
      },
      usernameRules: [
        v => !!v || 'E-mail is required',
        v => /.+@.+/.test(v) || 'E-mail must be valid',
        v => v && v.length <= 200 || 'E-mail must be less than 200 characters'
      ],
        passwordRules: [
            v => !!v || 'Password is required',
            v => v && v.length <= 100 || 'Password must be less than 100 characters'
      ]
    }
  },
  methods: {
    submit() {
      if (this.$refs.form.validate()) {
          axios.post("/rest/user", {
              username: this.user.username,
              password: this.user.password,
              isAdmin: this.user.isAdmin,
              region: this.user.regions
          }).catch(err => {
          this.message = err.response.data && err.response.data.message ? 'Error: ' + err.response.data.message : err.message
        })
      }
    },
    clear() {
      this.$refs.form.reset()
      this.message = ''
    },
      getRegions() {
        axios.get('/rest/regions').then(response => {
            this.user.regions = response.data
        })
      }
  },
  mounted: function () {
      this.$nextTick(function () {
          this.getRegions()
      })
  }
}
</script>
