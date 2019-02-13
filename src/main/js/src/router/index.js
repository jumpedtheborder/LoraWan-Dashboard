import Vue from 'vue'
import Router from 'vue-router'
import Home from '../views/Home.vue'
import About from '../views/About.vue'
import Login from '../views/Login.vue'
import Map from '../views/Map.vue'
import Register from '../views/Register.vue'
import RegisterDevice from '../views/RegisterDevice.vue'
import CandidateConsists from '../views/CandidateConsists.vue'
import CalculatedConsists from '../views/CalculatedConsists.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/home',
      name: 'Home',
      component: Home
    },
    {
      path: '/about',
      name: 'About',
      component: About
    },
    {
      path: '/',
      name: 'Login',
      component: Login
    },
    {
      path: '/map',
      name: 'Map',
      component: Map
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/registerDevice',
      name: 'RegisterDevice',
      component: RegisterDevice
    },
    {
      path: '/candidateConsists',
      name: 'CandidateConsists',
      component: CandidateConsists
    },
    {
      path: '/calculatedConsists',
      name: 'CalculatedConsists',
      component: CalculatedConsists
    }
  ]
})
