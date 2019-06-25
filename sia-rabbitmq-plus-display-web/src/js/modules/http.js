import tips from './tips.js';

const http = opt => {

    $.ajax({
        url: opt.url,
        data: opt.data,
        type: "get",
        dataType: "jsonp",
        success: function (data) {  

            if (data.resp_code && data.resp_code == '0000') { // 请求成功
                opt.success && opt.success(data);
            } else if (data.resp_code && data.resp_code == '1001') { // 请求服务端错误
                console.log('请求参数不能为空！');
                opt.error && opt.error(data);
            } else if (data.resp_code && data.resp_code == '1002') {
                console.log('请求参数不合法！');
                opt.error && opt.error(data)
            } else if (data.resp_code && data.resp_code == '9999') {
                console.log('未知异常！');
                opt.error && opt.error(data)
            }
            
            opt.done && opt.done(data);

        },
        error: function (res) {
            opt.fail && opt.fail(res);
        }
    });
};

export default http;