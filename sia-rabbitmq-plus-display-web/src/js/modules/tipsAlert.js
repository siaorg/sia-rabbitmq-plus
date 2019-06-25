import tips_alert from '../../components/tipsAlert.vue';

const tipsAlert = opt => {
    $("#tips-alert").empty().html(`<tips_alert :dlg="tipsdata"></tips_alert>`);
    new Vue({
        el: '#tips-alert',
        data(){
            return {
                tipsdata: {
                    show: true,
                    msg: opt.msg,
                    ready: opt.ready,
                    callback : opt.callback,
                    title : opt.title
                }
            };
        },
        components: {
            tips_alert : tips_alert
        }
    });
};

export default tipsAlert;