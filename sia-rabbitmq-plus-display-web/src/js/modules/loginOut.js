import http from './http.js';
//import tips from './tips.js';

function loginOut  ()  {
    let _this = this;

    console.log(_this, '--this')

    let logout = () => {
        var outUrl = base_url + "/account/logout";
        if(confirm('请确认,您要退出登录吗？')){
            doLogoutAjax(outUrl);
        }
    }

    let doLogoutAjax = (url) => {

        http({
            url: url,
            data: {},
            success: function (res) {
            	if (res && res.returnCode == '200') {
            		_this.$router.push({'name': 'index'});
            	}
            },
            error: function (xhr, ajaxOptions, thrownError) {

            }
        });
    }

    logout();
}

export default loginOut;
