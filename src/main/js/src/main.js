import '@babel/polyfill'
import Vue from 'vue'
import Vuetify from 'vuetify'
import App from './App.vue'
import 'vuetify/dist/vuetify.min.css'
import router from './router'

Vue.config.productionTip = false

Vue.use(Vuetify, {
  customProperties: true,
  iconfont: 'md',
})

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
