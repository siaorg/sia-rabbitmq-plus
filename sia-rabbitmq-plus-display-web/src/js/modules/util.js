let util = {
    query: function(query) { //url查找对应字段数据
        var subUrl = location.search.slice(location.search.indexOf('?') + 1),
            subArr = subUrl.split('&') || [],
            querystr = '';

        return subArr.forEach(function(v) {
            v.indexOf(query + '=') === 0 && (querystr = v.slice(query.length + 1))
        }), querystr;
    },
    templeteReplace: function(tmplStr, dataJson){//简单模板替换,tmplStr要替换的内容为{xxx},dataJson为对象
        return tmplStr.replace(/\{([^\}]+)\}/g, function(k, v) {
            return dataJson[v] ? dataJson[v] : dataJson;
        });
    },
    isNull: function(v) { //判断是否为空
        return (v == '' || v == undefined || v == null);
    },
    verifycard: function(str){ //校验银行卡号
        if (!str) {
            return 0;
        }
        var strArr = str.split('').reverse(),
            oddArr = [],
            evenArr = [],
            oddRes = 0,
            evenRes = 0;

        $.each(strArr, function(i) {
            if ((i + 1) % 2 == 0) {
                evenArr.push(strArr[i] * 2)
            } else {
                oddArr.push(strArr[i] * 1);
            }
        });

        $.each(oddArr.join('').split(''), function() {
            oddRes += this * 1;
        });
        $.each(evenArr.join('').split(''), function() {
            evenRes += this * 1;
        });
        if ((oddRes + evenRes) % 10 == 0) {
            return 1;
        } else {
            return 0;
        }
    },
    verifyID: function(str){ //校验身份证号
        var thisVal = str.replace(/x/g,'X'),
            thisArr = thisVal.split(''),
            len = thisVal.length,
            wiNum = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2],
            lastwiNum = [1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2],
            sumNum = 0,
            modNum = 0,
            res = 0;

        if (len != 18) {
            return 0;
        }

        $.each(thisArr, function(i, k) {
            if (i < 17) {
                sumNum += parseInt((k * wiNum[i]), 10);
            }
        });

        modNum = sumNum % 11;
        res = lastwiNum[modNum];

        if (res == thisArr[17]) {
            return 1;
        } else {
            return 0;
        }
    },
    //placeholder 兼容
    placeholder : function () {
        var JPlaceHolder = {
            //检测
            _check : function(){
                return 'placeholder' in document.createElement('input');
            },
            //初始化
            init : function(){
                if(!this._check()){
                    this.fix();
                }
            },
            //修复
            fix : function(){
                $(':input[placeholder]').each(function(index, element) {
                    var self = $(this), txt = self.attr('placeholder');
                    self.wrap($('<div></div>').css({position:'relative', zoom:'1', border:'none', background:'none', padding:'none', margin:'none'}));
                    var pos = self.position(), h = 20, paddingleft = self.css('padding-left');
                    var holder = $('<span></span>').text(txt).css({'fontSize' : '14px', position:'absolute', left:pos.left+20, top:pos.top+10, height:h, lienHeight:h, color:'#aaa'}).appendTo(self.parent());
                    self.focusin(function(e) {
                        holder.hide();
                    }).focusout(function(e) {
                        if(!self.val()){
                            holder.show();
                        }
                    });
                    holder.click(function(e) {
                        holder.hide();
                        self.focus();
                    });
                });
            }
        };
        JPlaceHolder.init();
    },
    reg: {
        email : /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[a-zA-Z]{2,3}$/,
        phone : /^1[3|4|5|7|8]\d{9}$/,
        addr : /(((^https?:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)$/g
    },
    unique(arr){
        return Array.from(new Set(arr));
    },
    extend (destination, source) {
        // 利用动态语言的特性, 通过赋值动态添加属性与方法
        for (var property in source) {
            destination[property] = source[property];
        }
        // 返回扩展后的对象
        return destination;
    }
};

export {util};