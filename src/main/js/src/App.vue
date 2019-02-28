<template>
  <v-app id="inspire">
    <v-navigation-drawer id="side"
      clipped
      fixed
      v-model="drawer"
      app
    >
      <div v-if="user">
      <v-list v-for="item in problemDevices" dense>
        <v-list-tile @click="handleRoute(item.deviceName)">
          <v-list-tile-content>
            <v-list-tile-title>Device Name: {{item.deviceName}}</v-list-tile-title>
            <v-list-tile-sub-title>Battery Level: {{item.batteryLevel}}%</v-list-tile-sub-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
      </div>
    </v-navigation-drawer>
    <v-toolbar app fixed clipped-left>
      <v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
      <v-toolbar-title>Device Dashboard</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items class="hidden-sm-and-down">
        <v-menu v-if="user" offset-y>
          <v-btn slot="activator" flat><v-icon medium class="mr-1">person</v-icon>My Account</v-btn>
          <v-list>
            <v-list-tile @click="$router.push('/registerdevice')">
              <v-list-tile-title>Create a device</v-list-tile-title>
            </v-list-tile>
            <v-list-tile v-if="user.admin == true" @click="$router.push('/createuser')">
              <v-list-tile-title>Add a user</v-list-tile-title>
            </v-list-tile>
            <v-list-tile @click="$router.push('/deleteDevice')">
              <v-list-tile-title>Delete a device</v-list-tile-title>
            </v-list-tile>
            <v-list-tile @click="handleLogout()">
              <v-list-tile-title>Logout</v-list-tile-title>
            </v-list-tile>
          </v-list>
        </v-menu>
      </v-toolbar-items>
    </v-toolbar>
    <v-content>
        <router-view></router-view>
    </v-content>
    <v-footer app fixed>
      <span>&copy; 2019</span>
    </v-footer>
  </v-app>
</template>

<script>
    import axios from 'axios'

    export default {
        data () {
            return {
                user: null,
                fetching: true,
                drawer: true,
                markers: [],
                topBatteryReports: [],
                problemDevices: []

            }
        },
        methods: {
            handleRoute(where) {
                alert('handleRoute' + where)
            },
            fetchUser() {
                axios.get('/rest/user/self').then(response => {
                  this.user = response.data
                  global.user = this.user
            }).catch(() => {
                    this.user = null
            }).then(
                this.findMostRecentBatteryLevel
                ).finally(() => {
                    this.fetching = false
            })
            },
            // This is called when user logs in via login dialog
            eventLogin(user) {
                this.user = user
            },
            handleLogout() {
                axios.post('/rest/logout', {}).then(() => {
                    // void
                }).catch(() => {
                    // void
                }).finally(() => {
                 this.user = null
                global.user = null
                this.$router.push('/')
            })
            },
            getAllDevices() {
                axios.get('/rest/devices').then((response) => {
                    this.markers = response.data
                }).catch(err => {
                })
            },
            findMostRecentBatteryLevel() {
                if (this.user) {
                    axios.get('/rest/topBatteryReports').then((response) => {
                        this.topBatteryReports = response.data;
                    }).then(this.findProblemDevices).catch(err => {

                    })
                }
                },
            findProblemDevices() {
                if (this.user) {
                    axios.get('/rest/problemDevices').then((response) => {
                        this.problemDevices = response.data
                    }).catch()
                }
            }
        },
        mounted: function () {
            this.$nextTick(function () {
                this.fetchUser()
                global.app = this
            })
        }
    }
</script>

<style scoped>
  .toolbar-title {
    cursor: pointer;
  }
  .alerts {
    position: absolute;
    width: 100%;
    z-index: 99;
  }

  @import "../node_modules/leaflet/dist/leaflet.css";

  .leaflet-fake-icon-image-2x {
    background-image: url(../node_modules/leaflet/dist/images/marker-icon-2x.png);
  }
  .leaflet-fake-icon-shadow {
    background-image: url(../node_modules/leaflet/dist/images/marker-shadow.png);
  }
  body {
    margin: 0px;
    font-family: Helvetica, Verdana, sans-serif;
  }
  #side {
    float:left;
    width:208px;
  }
  #full_div {
    position: absolute;
    overflow-x: auto;
    top: 0;
    right: 0;
    left: 0px;
    bottom: 0;
    padding-left: 8px;
    border-left: 1px solid #ccc;
  }
  ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
  }
  li {
    font: 200 15px/1.5 Helvetica, Verdana, sans-serif;
    border-bottom: 1px solid #ccc;
  }
  li:last-child {
    border: none;
  }
  li a {
    font-size: 15px;
    padding-left: 8px;
    text-decoration: none;
    color: #000;
    display: block;
    -webkit-transition: font-size 0.3s ease, background-color 0.3s ease;
    -moz-transition: font-size 0.3s ease, background-color 0.3s ease;
    -o-transition: font-size 0.3s ease, background-color 0.3s ease;
    -ms-transition: font-size 0.3s ease, background-color 0.3s ease;
    transition: font-size 0.3s ease, background-color 0.3s ease;
  }
  li a:hover {
    font-size: 20px;
    background: #f6f6f6;
  }
</style>