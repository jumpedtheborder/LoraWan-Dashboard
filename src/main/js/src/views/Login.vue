<template>
      <v-card>
        <v-card-title class="headline">Login</v-card-title>

        <v-card-text>
          <div :v-if="!!message" class="red--text">
            <p>{{message}}</p>
          </div>
          <v-form v-model="valid" ref="form">
            <v-text-field
              v-model="userDetails.username"
              :rules="emailRules"
              label="E-mail"
              required
              @input="inputEvent()"
            ></v-text-field>
            <v-text-field
              v-model="userDetails.password"
              :rules="passwordRules"
              type="password"
              label="Password"
              required
              @input="inputEvent()"
            ></v-text-field>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="submit">Submit</v-btn>
        </v-card-actions>
      </v-card>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      dialog: false,
      message: '',
        user: [],
      userDetails: {
        username: '',
        password: ''
      },
      valid: false,
      passwordRules: [
        v => !!v || 'Password is required',
        v => v && v.length <= 100 || 'Password must be less than 100 characters'
      ],
      emailRules: [
        v => !!v || 'E-mail is required',
        v => v && v.length <= 200 || 'E-mail must be less than 200 characters',
        v => /.+@.+/.test(v) || 'E-mail must be valid'
      ]
    }
  },
  props: {

  },
  methods: {
    submit() {
      if (this.$refs.form.validate()) {
        axios.post('/rest/login', {
          username: this.userDetails.username,
          password: this.userDetails.password
        }).then(() => {
          return axios.get('/rest/user/self').then(response => {
            global.user = response.data
            global.app.eventLogin(global.user);
            this.$router.push('/map')
          })
        }).catch(err => {
          if (err.response.status === 401 || err.response.status === 403) this.message = 'Invalid username or password!'
          else this.message = err.response.data && err.response.data.message ? 'Error: ' + err.response.data.message : err.message
        })
      }
    },
    inputEvent() {
      if (this.message) this.message = ''
    },
    checkIfLoggedIn() {
        if (this.user) {
            this.$router.push('/map')
        }
    },
      fetchUser() {
          axios.get('/rest/user/self').then(response => {
              this.user = response.data
              global.user = this.user
          }).catch(() => {
              this.user = null
          }).then(
              this.checkIfLoggedIn()
          )
      },
  },
    mounted() {
        this.fetchUser()
    }
}
</script>

<style>
.signup {
  width: 100%;
  display: block;
}
</style>
