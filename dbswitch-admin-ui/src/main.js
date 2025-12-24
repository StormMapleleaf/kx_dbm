import Vue from 'vue'
import App from './App'
import router from './router'
import axios from './assets/axios.js';
import ElementUI from 'element-ui';
import './assets/iconfont/iconfont.css'
import 'element-ui/lib/theme-chalk/index.css';
import VueCron from 'vue-cron'
import echarts from 'echarts'

Vue.use(axios)
Vue.use(ElementUI)
Vue.use(VueCron)

Vue.prototype.$http = axios
Vue.config.productionTip = false
Vue.prototype.$echarts = echarts

axios.interceptors.request.use(config => {

  let token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }

  return config;
}, function (error) {
  return Promise.reject(error)
})

axios.interceptors.response.use(res => {
  if (res.data && (res.data.code === 401 || res.data.code === 403 || res.data.code === 404)) {
    router.push({
      path: "/login"
    })
  }

  return res
}, error => {
  console.log(error);
  return Promise.reject(error.response)
})

new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
