/**
 * 页面数据
 * datas：所有请求结果数据
 * newDatas:所有分页后结果数据
 * pageData：当前分页结果
 * pageinit:是否第一次进入功能
 * listObj：列表操作对象
 */
var datas = new Array(), newDatas = [], pageData = [], pageinit = true, listObj;

/**
 * 页面模板
 */
var tempStart = "<span class='spanUrl' onclick='javascript:jumpChartsUrl(this)'>";
var tempEnd = "</span>";


/**
 * 窗体初始化
 */
window.winmgr.build({
    id: "msgList",
    height: "auto",
    "overflow-y": "auto",
    order: 999,
    theme: "ListBG"
});

window.winmgr.build({
    id: "chartsDiv",
    height: "auto",
    "overflow-y": "auto",
    order: 999,
    theme: "ListBG"
});


window.winmgr.show("msgList");

/**
 * 列表配置
 */
var listConfig = {
    id: "msgrecordTableada",
    pid: "msgList",
    pagesize: 20,
    caption: '<div class="tableCaption">' +
    '队列总数：' + '<span id="spanQueueName"></span>' +
    '发送总数：' + '<span id="spanPublishMessageNum"></span>' +
    '接收总数：' + '<span id="spanDeliverMessageNum"></span>' +
    '</div>',
    key: "queueName",
    pagerSwitchThreshold: 600,
    head: {
        queueName: ['队列名称', '16%'],
        projectName: ['项目名称', '9%'],
        unConsumeMessageNum: ['累计条数', '10%'],
        unConsumeMessageGrowthTimes: ['增长次数', '8%'],
        publishMessageNum: ['发送条数', '8%'],
        deliverMessageNum: ['接收条数', '8%'],
        queueConsumers: ['消费者数', '8%'],
        emailReceviers: ['报警Email', '13%'],
        deliverIps: ['接收IP及时间', '19%'],
        publishIps: ['发送IP及时间', '19%']
    },
    cloHideStrategy: {
        1100: [0, 1, 2, 3, 4, 5],
        1000: [0, 2, 4, 5],
        800: [0, 1, 2],
        500: [0, 2],
        400: [0, 2]
    },
    events: {

        /**
         * 改变表格样式
         */
        appendRowClass: function (rowData) {

            // 红色：累计条数>报警条数 或者 增长次数>报警增长次数时；优先级最高
            // 黄色/灰色：消费者为0且累计条数为0时置灰；消费者为0且累计条数不为0时置黄；优先级其次
            // 绿色：发送条数和接收条数只要有一个不为0时；优先级第三
            if ((rowData["unConsumeMessageNum"] > rowData["unConsumeMessageAlarmNum"])
                || (rowData["unConsumeMessageGrowthTimes"] > rowData["unConsumeMessageAlarmGrowthTimes"])) {
                return "RowCssRed"; // appendClass

            } else if (rowData["queueConsumers"] == 0) {

                if(rowData["unConsumeMessageNum"] == 0){
                    return "RowCssGRay";
                }else{
                    return "RowCssYellow";
                }

            } else if (!((rowData["publishMessageNum"] == 0) && (rowData["deliverMessageNum"] == 0))) {
                return "RowCssGreen";

            } else {
                return "";
            }
        }
    }
};


/**
 * 第一个chart的配置
 */

var chartsSendAndReceiveConfig = {
    id: "testChartSendAndReceive",
    type: "spline",
    cid: "chartsSendAndReceive",
    title: "",
    width: "auto",
    height: 300,
    inverted: false, /* 轴反转曲线 ： 默认：false*/
    legend: {
        enabled: true,
        verticalAlign: "top",
        align: "left",
        x: 50
    },
    yAxis: {
        title: "条数"
    },
    xAxis: {
        title: "时间",
        type: "datetime"
    },
    spline: {
        timeformat: "FS"
    },
    series: [
        {
            name: "发送条数",
            color: "#EEEE00",
            data: []
        }, {
            name: "接收条数",
            color: "#EE2200",
            data: []
        }
    ]
};


/**
 * 第二个chart的配置
 */
var a;
var chartsTotalConfig = {
    id: "testChartTotal",
    type: "spline",
    cid: "chartsTotal",
    title: "",
    width: "auto",
    height: 300,
    inverted: false, /* 轴反转曲线 ： 默认：false*/
    legend: {
        enabled: true,
        verticalAlign: "top",
        align: "left",
        x: 50
    },
    yAxis: {
        title: "条数"
    },
    xAxis: {
        title: "时间",
        type: "datetime"
    },
    spline: {
        timeformat: "FS"
    },
    series: [
        {
            name: "累计条数",
            color: "#563624",
            data: []
        }
    ]
};


/**
 * 初始化头部
 */
function initHeadDiv() {
    var divContent = "<div class=\"AppHubMVCSearchBar\" >"
        + "<input id=\"searchInput\" class=\"form-control AppHubMVCSearchBarInputText\""
        + "type=\"text\" placeholder=\"以队列名称作模糊检索\" value=\"\" onkeyup=\"loadSiaMonitorList(true)\"></input>"
        + "</div>";

    HtmlHelper.id("msgList").innerHTML += divContent;
}

function loadData() {

    // 是否第一次进入功能
    if (pageinit) {

        pageinit = false;
        ajaxGData();
        setInterval(ajaxGData, 60000);  // 一分钟刷新一次

    } else {
        loadSiaMonitorList();
    }


    function ajaxGData() {

        $.ajax({
            type: "get",
            async: false,
            url: "/doweb/queues",
            dataType: "json",
            success: function (data) {

                datas = data;

                datas.map(function (item) {
                    item.originQueueName = item.queueName;
                    return item;
                });

                loadSiaMonitorList();
            },
            error: function (e) {
                console.log("error:", e)
            }
        });
    }
}


/**
 * 分页处理
 * 处理过滤
 * 数据渲染
 */
function loadSiaMonitorList(pageInit) {

    var inputValue = $("#searchInput").val();
    var getPagingInfo = listObj.getPagingInfo();

    // 当是input触发的事件时，始终显示第一页
    if (pageInit) {
        listObj.setPageNum(1);
    }

    var pageindex = getPagingInfo.pageNum; // 当前页页码
    var pagesize = getPagingInfo.pageSize; // 每一页的数据条数

    newDatas = []; // 将newDatas重新置为空，否则不能进行过滤，而是将搜索的字段排在列表前面
    /**
     * filter过滤
     */
    var newIndex = 0;
    $.each(datas, function (index, obj) {

        var queueNameVal = obj.originQueueName;

        // indexOf()返回指定字符串值在字符串中首次出现的位置
        // 搜索不区分大小写
        if (queueNameVal.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0
            || queueNameVal.toUpperCase().indexOf(inputValue.toUpperCase()) >= 0
        ) {
            newDatas[newIndex++] = obj;
        }
    });

    /**
     * 排序
     */
    $("tr th:nth-child(1)").css("cursor", "pointer");
    $("tr th:nth-child(3)").css("cursor", "pointer");
    listSort(newDatas, headColumn, sortType);

    /**
     * 分页计算
     */
    var beginIndex = (pageindex - 1) * pagesize; // 包含

    var endIndex;
    if (newDatas.length == 0) { // 数据为0时(原始数据或过滤数据)
        endIndex = 0;

    } else if (newDatas.length % pagesize == 0) { // 数据长度为pagesize的倍数时
        endIndex = beginIndex + pagesize;

    } else { // endIndex的值取决于是否是最后一页
        endIndex = pageindex == Math.ceil(newDatas.length / pagesize) ? (beginIndex + newDatas.length % pagesize) : (beginIndex + pagesize); // 不包含
    }

    var countData = [];  // 当前页面的表格数据

    for (var i = beginIndex; i < endIndex; i++) {
        var rowData = newDatas[i];

        rowData["queueName"] = tempStart + rowData["originQueueName"] + tempEnd;

        /**
         * 字段格式化处理
         */
        columnFormat(rowData);

        countData.push(rowData);
    }
    pageData = countData;


    /**
     * 数据填充
     */
        //清空数据，否则分页其实是在页面下方累计
    listObj.clearTable();
    //显示分页
    listObj.setTotalRow(newDatas.length); //设置表格的总行数
    listObj.renderPagination();
    listObj.addRows(pageData);

    // 计算总数
    countSum(newDatas);

    // 隐藏common部分中的“X”删除按钮
    $('.clumdelete').hide();

    // table样式处理
    $("tr td:nth-child(1)").css("text-align", "left");
    $("tr td:nth-child(2)").css("text-align", "left");
    $("tr td:nth-child(8)").css("text-align", "left");

    $("tr td:nth-child(3)").css({textAlign: "right", paddingRight: "16px"});
    $("tr td:nth-child(4)").css({textAlign: "right", paddingRight: "12px"});
    $("tr td:nth-child(5n)").css({textAlign: "right", paddingRight: "12px"});
    $("tr td:nth-child(6)").css({textAlign: "right", paddingRight: "12px"});
    $("tr td:nth-child(7)").css({textAlign: "right", paddingRight: "12px"});
    $("tr td:nth-child(9)").css({textAlign: "right", paddingRight: "12px"});

    //$("#msgrecordTableada").css("borderRight","1px solid #dddddd");

}

/**
 * 多个Email、IP及时间时，换行显示
 */
function columnFormat(rowData) {

    ///------------------------------上线记得删除 begin-------------------------------------------------------
    //addTestInfo(rowData);
    ///------------------------------上线记得删除 end---------------------------------------------------------

    rowData.emailReceviers = rowData.projectInfo.emailReceviers.join("</br>");
    rowData.deliverIps = format(rowData.deliverIps);
    rowData.publishIps = format(rowData.publishIps);


    function format(jsonObj) {

        var result = new StringBuffer();

        var size = Object.keys(jsonObj).length;  // 获取键值对个数
        if (size > 0) {
            var ipsArr = [];  // 拼接之后的数组
            var timeArr = []; // 时间数组

            $.each(jsonObj, function (ip, time) {
                ipsArr.push(ip);
                timeArr.push(time);
            });

            // 按时间排序
            for (var m = 0; m < timeArr.length - 1; m++) {
                for (var n = 0; n < timeArr.length - m - 1; n++) {
                    if (timeArr[n] < timeArr[n + 1]) {
                        //time
                        var t = timeArr[n];
                        timeArr[n] = timeArr[n + 1];
                        timeArr[n + 1] = t;
                        //ip
                        var ip = ipsArr[n];
                        ipsArr[n] = ipsArr[n + 1];
                        ipsArr[n + 1] = ip;
                    }
                }
            }


            for (var m2 = 0; m2 < timeArr.length; m2++) {
                if (m2 > 0) {
                    result.append("<br/>");
                }
                result.append(ipsArr[m2]);
                result.append(" ");
                result.append(timeArr[m2]);
            }
        }

        return result.toString();
    }


    function addTestInfo(obj) { // 造测试数据
        obj["deliverIps"] = {
            "10.100.31.93": "2016-11-11 10:10:10",
            "10.100.31.34": "2016-11-13 10:10:10",
            "10.100.31.35": "2016-11-13 10:11:00"
        };
        obj["publishIps"] = {"10.100.31.66": "2016-11-11 10:10:10", "10.100.31.55": "2016-12-11 10:10:10"};
        obj.projectInfo.emailReceviers =["xinliang@creditease.cn","fuyingwang2@creditease.cn"];
    }
}


/**
 * 列表排序
 */
function listSort(nds, sortColumn, sortType) {

    var asc = true; //默认正序
    if ((sortColumn == "unConsumeMessageNum") && (sortType == 2)) {
        asc = false;
        $("tr th:nth-child(3)").text("累计条数▼");

    }else if((sortColumn == "unConsumeMessageNum") && (sortType == 1)){
        $("tr th:nth-child(3)").text("累计条数▲");

    }else if((sortColumn == "queueName") && (sortType == 1)){
        $("tr th:nth-child(3)").text("累计条数");
        sortColumn = "originQueueName";
    }

    /**
     *  冒泡排序
     *  注意：nds[j][sortColumn] 得值，nds[j].sortColumn 得属性
     */
    for (var i = 0; i < nds.length - 1; i++) {
        for (var j = 0; j < nds.length - i - 1; j++) {

            if(asc &&  (nds[j][sortColumn] > nds[j + 1][sortColumn])){
                datasChange(nds);
            }else if(!asc && (nds[j][sortColumn] < nds[j + 1][sortColumn])){
                datasChange(nds);
            }
        }
    }

    function datasChange(arrayObj) {
        var tmp = arrayObj[j];
        arrayObj[j] = arrayObj[j+1];
        arrayObj[j+1] = tmp;
    }
}

/**
 * 计算总数
 */
function countSum(newDatas) {

    var newDatasSum = newDatas.length;  // 队列总数
    var publishMessageNumSum = 0; // 发送总数
    var deliverMessageNumSum = 0; // 接收总数

    $("#spanQueueName").text(newDatasSum);

    if (newDatasSum == 0) {

        publishMessageNumSum = 0;
        deliverMessageNumSum = 0;

    } else {

        for (var i = 0; i < newDatasSum; i++) {

            publishMessageNumSum += parseInt(newDatas[i].publishMessageNum);
            deliverMessageNumSum += parseInt(newDatas[i].deliverMessageNum);
        }
    }

    $("#spanPublishMessageNum").text(publishMessageNumSum);
    $("#spanDeliverMessageNum").text(deliverMessageNumSum);
}


/**
 * 窗体二
 */
var chartsQueueName = "";

function jumpChartsUrl(obj) {

    var chartsContent = "<div class=\"topDiv\">" +
        "<span class=\"chartTitle\"></span>" +
        "<div id=\"clickout\" class=\"icon-signout\" onclick=\"javascript:closeWindow()\">" +
        "</div></div>" +

        "<div class=\"addWhite\">"+

        "<!--图表1的时间控件 begin-->"+
        "<div class=\"chartsSendAndReceiveSelectTimeDiv\">" +

        "<div class=\"widthLess\">" +

        "<select class=\"selectMethod\" id=\"pid\" onchange=\"initTimeControl(value);\">" +
        "<option value=\"0\" selected>分</option>" +
        "<option value=\"1\" >时</option>" +
        "<option value=\"2\" >日</option>" +
        "</select>" +

        "<div class=\"control-group chartsSendAndReceiveAddGroupDiv\">" +
        "<div class=\"controls input-append date form_datetime_start\" data-date-format=\"yyyy-mm-dd hh:ii:ss\">" +
        "<input id=\"sendRecevieStart\" size=\"19\" type=\"text\" placeholder=\"开始时间\" readonly>" +
        "<span class=\"add-on\"><i class=\"icon-th\"></i></span>" + " 至 " +
        "</div></div></div>" +

        "<div class=\"widthLess\">" +

        "<div class=\"control-group chartsSendAndReceiveAddEndGroupDiv\">" +
        "<div class=\"controls input-append date form_datetime_end\" data-date=\"2016-01-01T00:00:00Z\" data-date-format=\"yyyy-mm-dd hh:ii:ss\">" +
        "<input id=\"sendRecevieEnd\" id=\"\" size=\"19\" type=\"text\" placeholder=\"结束时间\" readonly>" +
        "<span class=\"add-on\"><i class=\"icon-th\"></i></span>" +
        "</div></div>" +

        "<button type=\"button\" class=\"btn btn-default btn-sm\" onclick=\"javascript:sendAndReceiveLook()\">查看</button>" +
        "</div>" +

        "<div class=\"widthLess\">" +
        "<span class=\"eMsg\" id=\"sendAndReceiveMsg\"></span>" +
        "</div></div>" + "<!--图表1的时间控件 end-->"+

        "<div id=\"chartsSendAndReceive\"></div>" +

        "<!--图表2的时间控件 begin-->"+
        "<div class=\"chartsTotalSelectTimeDiv\">" +

        "<div class=\"TotalWidthLess\">" +

        "<div class=\"control-group chartsTotalAddGroupDiv\">" +
        "<div class=\"controls input-append date form_datetime_start_1\" data-date-format=\"yyyy-mm-dd hh:ii:ss\">" +
        "<input id=\"totalStart\" size=\"19\" type=\"text\" placeholder=\"开始时间\" readonly>" +
        "<span class=\"add-on\"><i class=\"icon-th\"></i></span>" + " 至 " +
        "</div></div></div>" +

        "<div class=\"TotalWidthLess\">" +

        "<div class=\"control-group chartsTotalAdEnddGroupDiv\">" +
        "<div class=\"controls input-append date form_datetime_end_2\" data-date-format=\"yyyy-mm-dd hh:ii:ss\">" +
        "<input id=\"totalEnd\" size=\"19\" type=\"text\" placeholder=\"结束时间\" readonly>" +
        "<span class=\"add-on\"><i class=\"icon-th\"></i></span>" +
        "</div></div>" +

        "<button type=\"button\" class=\"btn btn-default btn-sm\" onclick=\"javascript:totalLook()\">查看</button>" +
        "</div>" +
        "<div class=\"TotalWidthLess\">" +

        "<span class=\"eMsg\" id=\"totalMsg\"></span>" +
        "</div></div>" +"<!--图表2的时间控件 end-->"+

        "<div id=\"chartsTotal\">" + "</div>"+"</div>";


    HtmlHelper.id("chartsDiv").innerHTML = chartsContent;

    window.winmgr.hide("msgList");
    window.winmgr.show("chartsDiv");

    initTimeControl(0); // 默认时间以分钟显示


    var key = obj.innerText;

    for (var i = 0; i < pageData.length; i++) {

        if (pageData[i].originQueueName == key) {

            chartsQueueName = pageData[i].originQueueName;
            $(".chartTitle").text("队列："+chartsQueueName);
        }
    }

    chartsSendAndReceiveOnload(chartsQueueName);
    chartsTotalOnload(chartsQueueName);


    /**
     * 适配不同的屏幕宽度
     * 注意：加载顺序
     */
    $(window).resize();
}

/**
 * 适配不同屏幕宽度
 */
$(window).resize(function () {
    var screenWidth = $(window).width();
    if (screenWidth < 1018) {
        $(".widthLess").css({display: "table-row"});
        $(".chartsSendAndReceiveSelectTimeDiv").css({
            width: "297px",
            position: "relative",
            left: "50%",
            marginLeft: "-148px"
        });
    }else {
        $(".widthLess").css({display: "block"});
        $(".chartsSendAndReceiveSelectTimeDiv").css({
            width: "494px",
            position: "absolute",
            left: "50%",
            marginLeft: "-247px"
        });
    }


    if (screenWidth < 794) {
        $(".TotalWidthLess").css({display: "table-row"});
        $(".chartsTotalSelectTimeDiv").css({
            width: "279px",
            position: "relative",
            left: "50%",
            marginLeft: "-140px"
        });
    } else {
        $(".TotalWidthLess").css({display: "block"});
        $(".chartsTotalSelectTimeDiv").css({
            width: "451px",
            position: "absolute",
            left: "50%",
            marginLeft: "-225px"
        });
    }

});

function closeWindow() {
    window.winmgr.hide("chartsDiv");
    window.winmgr.show("msgList");
}


/**
 * 初始化时间控件
 */
function initTimeControl(_minView) {

    $("#sendAndReceiveMsg").html("");
    $("#totalMsg").html("");
    //必须先清空
    $("#sendRecevieStart").val("");
    $("#sendRecevieEnd").val("");
    $('.form_datetime_start').datetimepicker('remove');
    $('.form_datetime_end').datetimepicker('remove');

    /**
     * 时间控件格式化设置
     * _minView(0-2:分、时、天)
     */
    var _format;
    if (_minView == 0) {
        //_format = "yyyy-mm-dd hh:ii:ss";

        $('.form_datetime_start').datetimepicker({
            format: "yyyy-mm-dd hh:ii:00",
            minView: _minView,
            language: 'zh-CN',
            autoclose: true,
            minuteStep: 1, // 控件分钟间隔设置
            todayBtn: true
        });

        $('.form_datetime_end').datetimepicker({
            format: "yyyy-mm-dd hh:ii:59",
            minView: _minView,
            language: 'zh-CN',
            autoclose: true,
            minuteStep: 1,
            todayBtn: true
        });

    } else if (_minView == 1) {
        _format = "yyyy-mm-dd hh";
    } else if (_minView == 2) {
        _format = "yyyy-mm-dd";
    }


    $('.form_datetime_start').datetimepicker({
        format: _format,
        minView: _minView,
        language: 'zh-CN',
        autoclose: true,
        minuteStep: 1, // 控件分钟间隔设置
        todayBtn: true
    });

    $('.form_datetime_end').datetimepicker({
        format: _format,
        minView: _minView,
        language: 'zh-CN',
        autoclose: true,
        minuteStep: 1,
        todayBtn: true
    });

    $('.form_datetime_start_1').datetimepicker({
        format: "yyyy-mm-dd hh:ii:00",
        minView: 0,
        language: 'zh-CN',
        autoclose: true,
        minuteStep: 1,
        todayBtn: true
    });

    $('.form_datetime_end_2').datetimepicker({
        format: "yyyy-mm-dd hh:ii:59",
        minView: 0,
        language: 'zh-CN',
        autoclose: true,
        minuteStep: 1,
        todayBtn: true
    });

    initTimeOptionValue(_minView);
}

var sTime, eTime;
function initTimeOptionValue(type) {

    sTime, eTime = new Date();

    if (type == 0) {
        sTime = new Date(new Date().setMinutes(eTime.getMinutes() - 4));
    } else if (type == 1) {
        sTime = new Date(new Date().setHours(eTime.getHours() - 4));
    } else if (type == 2) {
        sTime = new Date(new Date().setDate(eTime.getDate() - 4));
    } else {
        sTime = eTime;
    }

    $('.form_datetime_start').datetimepicker('update', sTime);
    $('.form_datetime_end').datetimepicker('update', eTime);
    $('.form_datetime_start_1').datetimepicker('update', new Date(new Date().setMinutes(eTime.getMinutes() - 4)));
    $('.form_datetime_end_2').datetimepicker('update', eTime);
}


/**
 * 适用所有的浏览器，包括pc端、手机端
 */
function getDateForStringDate(strDate){
    //切割年月日与时分秒称为数组
    var s = strDate.split(" ");
    var s1 = s[0].split("-");
    var s2 = s[1].split(":");
    if(s2.length==2){
        s2.push("00");
    }
    return new Date(s1[0],s1[1]-1,s1[2],s2[0],s2[1],s2[2]);
}

function sendAndReceiveLook() {

    var sendRecevieStartVal = $("#sendRecevieStart").val();
    var sendRecevieEndVal = $("#sendRecevieEnd").val();

    // 必须默认填充时间格式
    var dayLength = "yyyy-mm-dd".length, hourLength = "yyyy-mm-dd hh".length, minuteLength = "yyyy-mm-dd hh:ii:ss".length;
    var suffix;
    if (sendRecevieStartVal.length == dayLength) {
        suffix = " 00:00:00:000";
    } else if (sendRecevieStartVal.length == hourLength) {
        suffix = ":00:00:000";
    } else if (sendRecevieStartVal.length == minuteLength) {
        suffix = ":000";
    }
    sendRecevieStartVal += suffix;
    sendRecevieEndVal += suffix;

    var sendRecevieSTimeLong = new Date(this.getDateForStringDate(sendRecevieStartVal)).getTime();
    var sendRecevieETimeLong = new Date(this.getDateForStringDate(sendRecevieEndVal)).getTime();

    var objS = document.getElementById("pid");
    var optionVal = objS.options[objS.selectedIndex].value;

    var differenceTmp = sendRecevieETimeLong-sendRecevieSTimeLong;

    if (sendRecevieSTimeLong > sendRecevieETimeLong) {
        $("#sendAndReceiveMsg").html("开始时间必须小于结束时间");

    }else if((optionVal == 0) && (differenceTmp > 30*60*1000)){
        $("#sendAndReceiveMsg").html("开始与结束时间间隔不得大于30分钟");

    }else if((optionVal == 1) && (differenceTmp > 29*60*60*1000)){
        $("#sendAndReceiveMsg").html("开始与结束时间间隔不得大于30小时");

    }else if((optionVal == 2) && (differenceTmp > 29*24*60*60*1000)){
        $("#sendAndReceiveMsg").html("开始与结束时间间隔不得大于30天");

    }else {
        $("#sendAndReceiveMsg").html("");
        chartsSendAndReceiveOnload(chartsQueueName);   // 加载图表1
    }
}

function totalLook() {

    var totalStartVal = $("#totalStart").val();
    var totalEndVal = $("#totalEnd").val();

    var totalSTimeLong = new Date(this.getDateForStringDate(totalStartVal)).getTime();
    var totalETimeLong = new Date(this.getDateForStringDate(totalEndVal)).getTime();

    if (totalSTimeLong > totalETimeLong) {
        $("#totalMsg").html("开始时间必须小于结束时间");

    }else if((totalETimeLong-totalSTimeLong) > 30*60*1000){
        $("#totalMsg").html("开始与结束时间间隔不得大于30分钟");

    }else{
        $("#totalMsg").html("");
        chartsTotalOnload(chartsQueueName); // 加载图表2
    }
}


/**
 * 加载第一个chart
 */
function chartsSendAndReceiveOnload(chartsQueueName) {

    var params = [], categories = [];

    var sendRecevieStartVal = $("#sendRecevieStart").val();
    var sendRecevieEndVal = $("#sendRecevieEnd").val();

    var objS = document.getElementById("pid");
    var optionVal = objS.options[objS.selectedIndex].value;

    if (optionVal == 0) {  // 分

        $.ajax({
            type: "get",
            async: false,
            url: "/doweb/minutes",
            data: {
                queueName: chartsQueueName,
                startTime: sendRecevieStartVal,
                endTime: sendRecevieEndVal
            },
            dataType: "json",
            success: function (result) {

                // result是数组,数组的长度就是线条点的个数
                for (var i = 0; i < 2; i++) { // 有2组数据
                    params[i] = [];

                    for (var j = 0; j < result.length; j++) {

                        categories[j] = result[j].worktime;

                        if (i == 0) {
                            params[0][j] = result[j].publishMessageNum;
                        } else if (i == 1) {
                            params[1][j] = result[j].deliverMessageNum;
                        }
                    }
                }

                chartsSendAndReceiveConfig.xAxis["categories"] = categories;

                window["appcharts"].reset("testChartSendAndReceive");
                window["appcharts"].bulid(chartsSendAndReceiveConfig);
                window["appcharts"].run("testChartSendAndReceive", params);
            },
            error: function (e) {
                console.log("error:", e)
            }
        });


    } else if ((optionVal == 1) || (optionVal == 2)) { // 时、日

        ajaxFunc();
    }

    function ajaxFunc() {
        $.ajax({
            type: "get",
            async: false,
            url: "/doweb/stats",
            data: {
                queueName: chartsQueueName,
                startTime: sendRecevieStartVal,
                endTime: sendRecevieEndVal
            },
            dataType: "json",
            success: function (result) {

                for (var i = 0; i < 2; i++) {
                    params[i] = [];

                    for (var j = 0; j < result.length; j++) {

                        categories[j] = result[j].realtime;

                        if (i == 0) {
                            params[0][j] = result[j].publishMessageNumSum;
                        } else if (i == 1) {
                            params[1][j] = result[j].deliverMessageNumSum;
                        }
                    }
                }

                chartsSendAndReceiveConfig.xAxis["categories"] = categories;

                window["appcharts"].reset("testChartSendAndReceive");
                window["appcharts"].bulid(chartsSendAndReceiveConfig);
                window["appcharts"].run("testChartSendAndReceive", params);
            },
            error: function (e) {
                console.log("error:", e)
            }
        });
    }
}


/**
 * 加载第二个chart
 */
function chartsTotalOnload(chartsQueueName) {

    var params = [], categories = [];
    params[0] = [];

    /**
     * 有多少分钟就有多少个点
     */
    var totalStartVal = $("#totalStart").val();
    var totalEndVal = $("#totalEnd").val();

    $.ajax({
        type: "get",
        async: false,
        url: "/doweb/minutes",
        data: {
            queueName: chartsQueueName,
            startTime: totalStartVal,
            endTime: totalEndVal
        },
        dataType: "json",
        success: function (result) {

            for (var j = 0; j < result.length; j++) {

                categories[j] = result[j].worktime;
                params[0][j] = result[j].unConsumeMessageNum;
            }

            chartsTotalConfig.xAxis["categories"] = categories;

            window["appcharts"].reset("testChartTotal");
            window["appcharts"].bulid(chartsTotalConfig);
            window["appcharts"].run("testChartTotal", params);
        },
        error: function (e) {
            console.log("error:", e);
        }
    });
}


/**
 * 点击事件
 */
var headColumn;   // 点击过的字段
var sortType = 0; // 正序 倒序的类型
function getClickColumn(thisObj) {

    var clickColumn = thisObj.id.slice(0, -5); // 点击的字段

    var queueNameTh = document.getElementById("queueName_head");
    var unConsumeMessageNumTh = document.getElementById("unConsumeMessageNum_head");

    if ((clickColumn == "unConsumeMessageGrowthTimes")
        || (clickColumn == "publishMessageNum")
        || (clickColumn == "deliverMessageNum")
        || (clickColumn == "queueConsumers")
        || (clickColumn == "emailReceviers")
        || (clickColumn == "deliverIps")
        || (clickColumn == "publishIps")
        || (clickColumn == "projectName")
    ) {
        return; // 跳出点击事件

    } else if (clickColumn == "queueName") {

        if(queueNameTh.style.cursor == "pointer"){
            // 点击队列名称时，如果是第一次点击，则排序；如果不是第一次点，则不排序，return
            if(headColumn == "queueName"){
                return;
            }else{
                sortType = 1;
            }
        }else if(queueNameTh.style.cursor == "e-resize"){
            return;
        }

    } else if(unConsumeMessageNumTh.style.cursor == "pointer"){

        if(headColumn == "unConsumeMessageNum"){
            sortType = sortType == 1 ? 2 : 1;
        }else{
            sortType = 1;
        }

    }else if(unConsumeMessageNumTh.style.cursor == "e-resize"){
        return;
    }

    headColumn = clickColumn;
    listObj.setPageNum(1); // 点击排序后回到第一页
    loadSiaMonitorList(false);
}


var tableOffset = 20,tableMouserIsMove = false; // tableMouserIsMove 表格鼠标是否在移动计算
var tTD = null; // 用来存储当前更改宽度的Table Cell,避免快速移动鼠标的问题
function tableHeadMoveSet() {
    var table = document.getElementById("msgrecordTableada");
    for (var j = 0; j < table.rows[0].cells.length; j++) {

        table.rows[0].cells[j].onmousedown = function(_e) {

            var eX,eOffsetX,diffX;
            if(HtmlHelper.isFF()){ // Firefox
                eOffsetX = _e.layerX;
                eX = _e.pageX;
                diffX = this.offsetWidth + this.offsetLeft - tableOffset;
            }else{
                eOffsetX = window.event.offsetX;
                eX = window.event.x;
                diffX = this.offsetWidth - tableOffset;
            }

            window.getSelection().removeAllRanges();
            if (!tableMouserIsMove && tTD == null && eOffsetX > diffX) {
                // 记录单元格
                tTD = this;
                table.style.cursor = 'e-resize';
                tTD.oldX = eX;
                tTD.oldWidth = tTD.offsetWidth;
            }

            if(tableMouserIsMove){
                tableMouserIsMove=false;
            }
        };

        table.rows[0].cells[j].onmousemove = function(_e) {

            var eOffsetX,diffX;
            if(HtmlHelper.isFF()){
                eOffsetX = _e.layerX;
                diffX = this.offsetWidth + this.offsetLeft - tableOffset;
            }else{
                eOffsetX = window.event.offsetX;
                diffX = this.offsetWidth - tableOffset;
            }

            var defStyle = "default";
            if(this.innerHTML=="队列名称" || this.innerHTML.indexOf("累计条数")!=-1){ // 检索的字符串值如果没有出现，则返回-1
                defStyle = "pointer";
            }

            // 更改鼠标样式
            if(table.style.cursor == 'e-resize'){
                this.style.cursor = 'e-resize';
                tableMouserIsMove=true;
            }else if (eOffsetX > diffX){
                this.style.cursor = 'e-resize';
            }else{
                this.style.cursor = defStyle;
            }
        };

        table.rows[0].cells[j].onmouseout = function() {
            tableMouserIsMove=false;
        };
    }

    document.onmouseup = function() {
        if(tTD){
            tTD = null;
            table.style.cursor = 'default';
        }
    };

    document.onmousemove = function(_e) {

        if(!tTD){
            return;
        }

        // 调整宽度
        if (tTD != null) {

            var eX;
            if(HtmlHelper.isFF()){
                eX = _e.pageX;
            }else{
                eX = window.event.x;
            }

            window.getSelection().removeAllRanges();
            if (tTD.oldWidth + (eX - tTD.oldX) > 0){
                tTD.width = tTD.oldWidth + (eX - tTD.oldX);
            }
            // 调整列宽
            tTD.style.width = tTD.width;

            // 调整该列中的每个Cell
            table = tTD;
            while (table.tagName != 'TABLE'){
                table = table.parentElement;
            }

            for (j = 0; j < table.rows.length; j++) {
                table.rows[j].cells[tTD.cellIndex].width = tTD.width;
            }
        }
    };
}


$(document).ready(function () {

    listObj = new AppHubTable(listConfig);

    initHeadDiv();
    /**
     * 初始化列表
     **/
    listObj.sendRequest = loadData;
    listObj.headClickUser = getClickColumn;
    listObj.initTable();
    tableHeadMoveSet();
});




