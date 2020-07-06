//判断是否已经是最后一页的标志,0表示尚未到达最后一页，1表示已经到达最后一页
var pageBool = '0';

//向前分页
function previousPage(formAction, curPageNum ){
	toPage(formAction,  curPageNum );
}

//向前分页
function nextPage(formAction,  curPageNum  ){
	//如果已经到达最后一页了，则不再执行。
	if(pageBool=='1'){
		return false;
	}
	toPage(formAction,  curPageNum);
}

//跳转到第几页
function toPage(formAction,  curPageNum ){
	if( !checkPage() ){
		return;
	}
	document.all.curPageNum.value=curPageNum;
	if(formAction!=""){
		document.forms[0].action=formAction;
	}
	document.forms[0].submit();
}
//将列表补全
function initList( perPageSize ){
	var rowLength = listTable.rows.length-1;
	if( rowLength<=perPageSize*1 ){
		document.all.nextpage.disabled="true";
		document.all.endpage.disabled="true";
		//若需要补全列表，则表名已经到达最后一页了。
		pageBool='1';
		for( var i=0 ; i<perPageSize*1-rowLength+1 ; i++ ){
			var rowObj = listTable.insertRow();
			rowObj.className="listdata";
			for( var j=0 ; j<listTable.rows[ 0 ].cells.length ; j++ ){
				var cellObj=rowObj.insertCell();
			}
		}
	}
	<!--将$per_Page_Size+1条记录，隐藏显示-->
	var rowObj=listTable.rows;
	if(rowObj.length==12){
		rowObj[11].style.display="none";
	}
}

/**
 * 弹出模式窗体
 * @param url 地址
 * @param obj 参数
 * @param dialogWidth IE7宽度
 * @param dialogHeight IE7高度
 * @return 返回结果
 */
function modalDialog(url,obj,dialogWidth,dialogHeight){
	dialogWidth=ieX(dialogWidth);
	dialogHeight=ieY(dialogHeight);
	return window.showModalDialog(url,obj,"dialogWidth:" + dialogWidth + "px; dialogHeight:" + dialogHeight + "px; help:no; status:0");
}
/**
 * 弹出窗口功能
 * @param url 弹出窗口的url
 * @param name 弹出窗口的名称
 * @return 返回当前浏览器版本所需要的宽度
 */
function openWin(url,name,iWidth,iHeight){
	var url;
	var name;
    if (iWidth==null){
		iWidth=400;
	}
	if (iHeight==null){
		iHeight=300;
	}
	_open(url,name,iWidth,iHeight);
}

/*
alwaysLowered | yes/no | 指定窗口隐藏在所有窗口之后 
alwaysRaised | yes/no | 指定窗口悬浮在所有窗口之上 
depended | yes/no | 是否和父窗口同时关闭 
directories | yes/no | Nav2和3的目录栏是否可见 
height | pixel value | 窗口高度 
hotkeys | yes/no | 在没菜单栏的窗口中设安全退出热键 
innerHeight | pixel value | 窗口中文档的像素高度 
innerWidth | pixel value | 窗口中文档的像素宽度 
location | yes/no | 位置栏是否可见 
menubar | yes/no | 菜单栏是否可见 
outerHeight | pixel value | 设定窗口(包括装饰边框)的像素高度 
outerWidth | pixel value | 设定窗口(包括装饰边框)的像素宽度 
resizable | yes/no | 窗口大小是否可调整 
screenX | pixel value | 窗口距屏幕左边界的像素长度 
screenY | pixel value | 窗口距屏幕上边界的像素长度 
scrollbars | yes/no | 窗口是否可有滚动栏 
titlebar | yes/no | 窗口题目栏是否可见 
toolbar | yes/no | 窗口工具栏是否可见 
Width | pixel value | 窗口的像素宽度 
z-look | yes/no | 窗口被激活后是否浮在其它窗口之上
*/
function _open(url,name,iWidth,iHeight,iLeft,iTop){
  var url;                                 //转向网页的地址;
  var name;                           //网页名称，可为空;
  iWidth = ieX(iWidth);                          //弹出窗口的宽度;
  iHeight = ieY(iHeight);                     //弹出窗口的高度;
  if (iTop==null)
  iTop = (window.screen.availHeight-30-iHeight)/2;       //获得窗口的垂直位置;
  if (iLeft==null)
  iLeft = (window.screen.availWidth-10-iWidth)/2;           //获得窗口的水平位置;
  window.open(url,name,'height='+iHeight+',,innerHeight='+iHeight+',width='+iWidth+',innerWidth='+iWidth+',top='+iTop+',left='+iLeft+',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no');
 }
/**
 * 处理弹出窗口的宽度
 * @param n IE7宽度
 * @return 返回当前浏览器版本所需要的宽度
 */
function ieX(n){
	/**if (ieVer<7){
		n = n+10
	}
	if (n<0){
		n=0;
	}**/
	return n;
}
/**
 * 处理弹出窗口的高度
 * @param n IE7高度
 * @return 返回当前浏览器版本所需要的高度
 */
function ieY(n){
	/**if (ieVer<7){
		n = n+10
	}
	if (n<0){
		n=0;
	}**/
	return n;
}