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
        userName: "${userName}",
        headImage:"${headImage}",
        showForum:"${showForum}",
        configUrl:'',
    }
</script>
<script src="//res.wx.qq.com/open/js/jweixin-1.2.0.js"></script>
<div id="loading"><img src="//static.iqycamp.com/images/dribz.gif" style="width:300px;display: block;margin: 0 auto;"/></div>
<div id="react-app"></div>
<!-- 业务代码-->
<%--<script src="${vendorResource}"></script>--%>
<script src="${resource}" onload="document.getElementById('loading').style.display = 'none'"></script>

<script>
    var display = '<%=ConfigUtils.domainName()%>'
    if(display === 'http://www.iquanwai.com' || display === 'https://www.iquanwai.com') {
        var _hmt = _hmt || [];
        (function () {

            var hm = document.createElement("script");
            hm.src = "https://hm.baidu.com/hm.js?64c8a6d40ec075c726072cd243d008a3";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
        })();
    }

</script>
<%--性能数据收集脚本--%>
<script>
    (function(window, mta) {
        window.MeituanAnalyticsObject = mta;
        window[mta] = window[mta] || function() {
                (window[mta].q = window[mta].q || []).push(arguments);
            };
    }(window, 'mta'));
    window.onload = function () {
        window.ENV.configUrl=window.location.href;
        //页面名称
        mta('create', 'risePage');
        //上报接口
        mta('config', 'beaconImage', '/performance/report');
        (function sendTime(){
            var timing = performance.timing;
            var loadTime = timing.loadEventEnd - timing.navigationStart;//过早获取时,loadEventEnd有时会是0
            if(loadTime <= 0) {
                // 未加载完，延迟200ms后继续times方法，直到成功
                setTimeout(function(){
                    sendTime();
                }, 200);

            } else {
                mta('send', 'page');
            }
        })()
    };
</script>
<%--性能数据js资源--%>
<script src="//www.iqycamp.com/script/mta.min.js"></script>
</body>
</html>