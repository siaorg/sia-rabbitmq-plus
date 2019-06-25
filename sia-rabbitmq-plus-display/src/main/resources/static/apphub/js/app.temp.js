/**
 * x: 上限
 * y: 下限
 */
function uavRandom(x,y){
    var rand = parseInt(Math.random() * (x - y + 1) + y);
    return rand;
}

/*加载侧边栏菜单*/
function LoadSidebarMenu(appInfo){
	var sidebar=document.getElementById("menu");
	
	for(var j=0;j<appInfo.menu.length;j++){
		var para=document.createElement("li");
		var paraA=document.createElement("a");
		paraA.innerHTML=appInfo.menu[j].functions;
		paraA.setAttribute("href","javascript:jumpUrl('"+appInfo.menu[j].url+"')");
		paraA.setAttribute("class","appMenu");
		para.appendChild(paraA);
		sidebar.appendChild(para);
	}

}

/*加载导航栏菜单*/
function LoadNavbarMenu(appInfo){
	var navbar=document.getElementById("nav-menu");
	for(var j=0;j<appInfo.menu.length;j++){
		
		var navbar_para=document.createElement("li");
		navbar_para.setAttribute("class","hidden-sm hidden-md hidden-lg");

		var paraA=document.createElement("a");
		paraA.innerHTML=appInfo.menu[j].functions;
		paraA.setAttribute("href","javascript:jumpUrl('"+appInfo.menu[j].url+"')");
		paraA.setAttribute("class","appMenu");

		navbar_para.appendChild(paraA);
		navbar.appendChild(navbar_para);
	}
}

function jumpUrl(url){
	//guiPing_RSClient(jumpUrlCallBack(url));
	jumpUrlCallBack(url);
}

function jumpUrlCallBack(url){
	setContentHeight();
	url += "?"+uavRandom(9999,1); /*刷新，处理缓存*/
	$("#appContent").attr("src",url);
	$("#navbar").removeClass("in");
	
}

function setContentHeight(){
	var ifm= document.getElementById("appContent");
	if (document.body.clientWidth<768) {
		ifm.style.height =  document.body.clientHeight-52+"px";
	}
	else {
		ifm.style.height =  document.body.clientHeight+"px";
	}
}


/*加载导航栏标题*/
function LoadNote(appInfo){
	$("#note").text(appInfo.title);
	$("#appIco").attr("src",appInfo.url+"/appIco.png");

}

function appMenuInit(){
	var uInfo = window["cachemgr"].get(uavGuiCKey + "user.manage.info");
	uInfo = eval("("+uInfo+")");
	var appIdParam = window["cachemgr"].get(uavGuiCKey + "junmpApp");
	var appInfo = eval("("+uInfo[appIdParam]+")");
	LoadSidebarMenu(appInfo);	//加载侧边栏菜单
	LoadNavbarMenu(appInfo);	//加载顶部菜单栏
	LoadNote(appInfo);   //标题
}

function appJumpBackMain(){
	window.location.href="rs/gui/jumpMainPage";
}

$("document").ready(
	appMenuInit()
);

 //展示区自动适应高度
window.onresize = function() {
	setContentHeight();
};