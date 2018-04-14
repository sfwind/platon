<!doctype html>
<%@ page import="com.iquanwai.platon.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>圈外同学</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
</head>
<body>
<script>
    window.ENV = {
        reactMountPoint: "react-app",
        showExplore:"${showExplore}",
        configUrl:'',
        sensorsProject:"${sensorsProject}",
    }
</script>
<script src="//res.wx.qq.com/open/js/jweixin-1.2.0.js"></script>
<div id="loading"><img src="//static.iqycamp.com/images/dribz.gif" style="width:300px;display: block;margin: 0 auto;"/></div>
<div id="react-app"></div>
<!-- 业务代码-->
<%--<script src="${vendorResource}"></script>--%>
<script src="${resource}" onload="document.getElementById('loading').style.display = 'none'"></script>
<script type='text/javascript'>
    (function(m, ei, q, i, a, j, s) {
        m[i] = m[i] || function() {
                    (m[i].a = m[i].a || []).push(arguments)
                };
        j = ei.createElement(q),
                s = ei.getElementsByTagName(q)[0];
        j.async = true;
        j.charset = 'UTF-8';
        j.src = 'https://static.meiqia.com/dist/meiqia.js?_=t';
        s.parentNode.insertBefore(j, s);
    })(window, document, 'script', '_MEIQIA');
    _MEIQIA('entId', 80143);
    _MEIQIA('withoutBtn');
    _MEIQIA('init');
</script>
<script>
    var display = '<%=ConfigUtils.domainName()%>'
    if(display === 'http://www.iquanwai.com' || display === 'https://www.iquanwai.com') {
        var _hmt = _hmt || [];
        (function () {
            <%--百度统计--%>
            var hm = document.createElement("script");
            hm.src = "https://hm.baidu.com/hm.js?64c8a6d40ec075c726072cd243d008a3";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
            <%--apm请求--%>
//            !(function(c,b,d,a){c[a]||(c[a]={});c[a].config={pid:"ejanb9v34d@8168fdf953444ea",imgUrl:"https://arms-retcode.aliyuncs.com/r.png?",enableSPA:true,autoSendPv:false};
//                with(b)with(body)with(insertBefore(createElement("script"),firstChild))setAttribute("crossorigin","",src=d)
//            })(window,document,"https://retcode.alicdn.com/retcode/bl.js","__bl");
        })();
    }
</script>

</body>
</html>