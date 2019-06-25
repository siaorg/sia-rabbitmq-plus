// 4. 创建和挂载根实例。
// 记得要通过 router 配置参数注入路由，
// 从而让整个应用都有路由功能

import 'babel-polyfill';
import App from './App.vue';
import router from './js/router.js';
import store from './js/store.js';

import './scss/reset.scss'
import './scss/index.scss';
import VueResource from 'vue-resource'

import moment from 'moment';

/*定义全局(日期格式化)过滤器*/
Vue.filter( 'formatTime' , function(value,type) {
  return moment(value).format(type);
});

Vue.use(VueResource);
Vue.config.productionTip = false;
const app = new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App }
});
