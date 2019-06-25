
import mainTable from '../components/table.vue';

const routes = [
	{
    	path: '/',
    	name: 'mainTable',
    	component: mainTable,
	}
];

Vue.use(VueRouter);

// 3. 创建 router 实例，然后传 `routes` 配置
// 你还可以传别的配置参数, 不过先这么简单着吧。
const router = new VueRouter({
    routes
});

export default router;
