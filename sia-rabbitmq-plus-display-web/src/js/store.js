import Vuex from 'vuex';
import {util} from './modules/util.js';

Vue.use(Vuex);

let a;
const  store = new Vuex.Store({
    state:{
        indexNoteLists: [],
        curClassifyId : '1',
        curParentClassifyId : '',
        curClassifyName : ''
    },
    getters : {
    	
    },
    mutations:{
        setCurClassifyName (state, name) {
            state.curClassifyName= name;
        },
        setNotelists (state, noteLists) {
        	state.indexNoteLists= noteLists;
        },
        setCurClassifyId (state, id) {
            state.curClassifyId= id;
        },
        setCurParentClassifyId (state, id) {
            id = util.isNull(id) ? '' : id;
            state.curParentClassifyId= id;
        }
    }
});

export default store;