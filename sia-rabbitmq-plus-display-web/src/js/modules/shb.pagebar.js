/**
 * 自定义分页插件
 * @param opt
 */
$.fn.microMallPager = function(opt) {
    var _this = this;
    var opt = $.extend({
        //当前页码
        currPage: 1,
        //总页数
        totalPage: 10,
        //当前页码的class
        currNumClass: "active",
        //如果分页在此数之内，不显示省略号
        showNum: 5,
        //是否显示分页信息
        showPageInfo: true,
        //每页显示几条的候选项，如果为空则不显示此内容
        recodeNumList: [],
        //当前每页显示几条
        recodeNumSel: 10,
        //是否显示跳转按钮
        gotoStep: true,
        //未分页是否还要显示
        noPageShow: true,
        //是否要居中显示
        showCenter:false,
        //总共的条数
        totalNum: 200
    }, opt||{});

    //如果设置未分页不显示，当没有分页时则不生成分页
    if(!opt.noPageShow && opt.totalPage <= 1){
        _this.empty();
        return;
    }

    var pageCont = $('<div class="pageCont"></div>');

    var recodeNumSel = $('<div class="recodeNumSel fl"></div>');

    if(opt.recodeNumList.length > 0){
        var recodeNumSelTitle = $('<div class="recodeNumSelTitle fl">每页显示: </div>'),
            recodeNumSelCont = $('<div class="recodeNumSelCont fl"></div>'),
            recodeNumSelCls = $('<div class="clear"></div>'),
            recodeNumSelected = $('<div class="recodeNumSelected">' + opt.recodeNumSel + '</div>'),
            recodeNumSelList = $('<div class="recodeNumSelList"></div>');

        for (var i = opt.recodeNumList.length-1; i >= 0; i--) {
            var numItem = opt.recodeNumList[i];
            var optionItem = $('<div class="recNum" data-num="' + numItem + '">' + numItem + '</div>');
            recodeNumSelList.append(optionItem);
        }

        recodeNumSelCont.append(recodeNumSelected);
        recodeNumSelCont.append(recodeNumSelList);

        recodeNumSel.append(recodeNumSelTitle);
        recodeNumSel.append(recodeNumSelCont);
        recodeNumSel.append(recodeNumSelCls);
    }
    pageCont.append(recodeNumSel);

    var numList = $('<div class="numList fl"></div>'),
        numListUl = $('<ul class="numListUl"></ul>');
    //当前第一页，不显示上一页
    if(opt.currPage >= 1){
        var prevPageItem = $('<li class="numItem fl"></li>'),
            prevPageNum = $('<span class="numTxt prevStep" data-pageNum="' + (opt.currPage - 1) + '">上一页</span>');
        prevPageItem.append(prevPageNum);
        numListUl.append(prevPageItem);
    }
    if(opt.totalPage <= opt.showNum){
        //如果数量不多，则不用显示省略号
        for(var pNum=1; pNum<=opt.totalPage; pNum++){
            var currPageItem = $('<li class="numItem fl"></li>'),
                currPageNum = $('<span class="numTxt" data-pageNum="' + pNum + '">' + pNum + '</span>');
            if(pNum == opt.currPage){
                currPageNum.addClass(opt.currNumClass);
            }
            currPageItem.append(currPageNum);
            numListUl.append(currPageItem);
        }
    }else{

        var sideNum = parseInt(opt.showNum/2);
        if(opt.currPage - sideNum <= 2){
            for(var pNum=1; pNum<= opt.currPage-1; pNum++){
                var currPageItem = $('<li class="numItem fl"></li>'),
                    currPageNum = $('<span class="numTxt" data-pageNum="' + pNum + '">' + pNum + '</span>');
                currPageItem.append(currPageNum);
                numListUl.append(currPageItem);
            }
        }else{
            var firstPageItem = $('<li class="numItem fl"></li>'),
                firstPageNum = $('<span class="numTxt" data-pageNum="1">1</span>');
            firstPageItem.append(firstPageNum);
            numListUl.append(firstPageItem);
            numListUl.append($('<li class="numItem fl"><span class="numTxt etcTxt">...</span></li>'));

            for(var pNum=opt.currPage-sideNum; pNum<= opt.currPage-1; pNum++){
                var currPageItem = $('<li class="numItem fl"></li>'),
                    currPageNum = $('<span class="numTxt" data-pageNum="' + pNum + '">' + pNum + '</span>');
                currPageItem.append(currPageNum);
                numListUl.append(currPageItem);
            }
        }

        //添加当前页面
        var nowPageItem = $('<li class="numItem fl"></li>'),
            nowPageNum = $('<span class="numTxt ' + opt.currNumClass + '" data-pageNum="' + opt.currPage + '">' + opt.currPage + '</span>');
        nowPageItem.append(nowPageNum);
        numListUl.append(nowPageItem);

        if(opt.currPage + sideNum >= opt.totalPage - 1){
            for(var pNum=opt.currPage+1; pNum<= opt.totalPage; pNum++){
                var currPageItem = $('<li class="numItem fl"></li>'),
                    currPageNum = $('<span class="numTxt" data-pageNum="' + pNum + '">' + pNum + '</span>');
                currPageItem.append(currPageNum);
                numListUl.append(currPageItem);
            }
        }else{
            for(var pNum=opt.currPage+1; pNum<= opt.currPage+sideNum; pNum++){
                var currPageItem = $('<li class="numItem fl"></li>'),
                    currPageNum = $('<span class="numTxt" data-pageNum="' + pNum + '">' + pNum + '</span>');
                currPageItem.append(currPageNum);
                numListUl.append(currPageItem);
            }
            numListUl.append($('<li class="numItem fl"><span class="numTxt etcTxt">...</span></li>'));
            var lastPageItem = $('<li class="numItem fl"></li>'),
                lastPageNum = $('<span class="numTxt" data-pageNum="' + opt.totalPage + '">' + opt.totalPage + '</span>');
            lastPageItem.append(lastPageNum);
            numListUl.append(lastPageItem);
        }
    }

    //当前最后一页，不显示下一页
    if(opt.currPage <= opt.totalPage){
        var nextPageItem = $('<li class="numItem fl"></li>'),
            nextPageNum = $('<span class="numTxt nextStep" data-pageNum="' + (opt.currPage + 1) + '">下一页</span>');
        nextPageItem.append(nextPageNum);
        numListUl.append(nextPageItem);
    }

    numList.append(numListUl);
    pageCont.append(numList);

    if (opt.showPageInfo){
        var pageInfo = $('<div class="pageInfo fl">共' + opt.totalPage + '页</div>');
        pageCont.append(pageInfo);
    }

    if (opt.gotoStep) {
        var goStep = $('<div class="goStep fl"></div>'),
            goStep_input = $('<input class="stepInput" type="text" value="' + opt.currPage + '" />'),
            goStep_btn = $('<input class="goBtn" type="button" value="跳转" data-totalpage="' + opt.totalPage + '" />');

        goStep.append(goStep_input);
        goStep.append(goStep_btn);
        pageCont.append(goStep);
    }
    var clear_pageCont = $('<div class="clear"></div>');
    pageCont.append(clear_pageCont);

    _this.empty();
    _this.append(pageCont);

    if (opt.showCenter){
        var contWidth = pageCont.width(),
            recNumWidth = recodeNumSel.width(),
            pageWidth = numList.width(),
            pageInfo = $(".pageInfo").width(),
            goStep = $(".goStep ").width(),
            pageConWidth = parseInt(pageWidth+pageInfo+goStep);

        var leftSpaceWidth = parseInt((contWidth - pageConWidth)/2 - recNumWidth);
        console.log(leftSpaceWidth);
        numList.css("margin-left", leftSpaceWidth);
    }


    recodeNumSel.find(".recodeNumSelected").click(function(){
        recodeNumSelList.toggle();
    });

    recodeNumSel.find(".recodeNumSelList .recNum").click(function(){
        recodeNumSelected.html($(this).data("num"));
        recodeNumSelList.hide();
    });
};