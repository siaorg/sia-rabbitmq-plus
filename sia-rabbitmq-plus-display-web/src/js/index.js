/**
 * Created by zhongmengchen on 2017/3/21.
 */
// 4. 创建和挂载根实例。
// 记得要通过 router 配置参数注入路由，
// 从而让整个应用都有路由功能
import router from './router.js';
import store from './store.js';
import '../scss/index.scss';


const app = new Vue({
    router,
    store
}).$mount('#app');