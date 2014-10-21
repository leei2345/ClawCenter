<%@ page language="java" import="java.util.*" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<html>

<head>
	<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
	<script type="text/javascript" src="js/highcharts.js"></script>
	<script type="text/javascript" src="js/exporting.js"></script>
	<script>
  
	Date.prototype.Format = function(fmt) {  
		var o = {   
		  "M+" : this.getMonth()+1,                 
		  "d+" : this.getDate(),               
		  "h+" : this.getHours(),               
		  "m+" : this.getMinutes(),           
		  "s+" : this.getSeconds(),             
		  "q+" : Math.floor((this.getMonth()+3)/3), 
		  "S"  : this.getMilliseconds()               
		};   
		if(/(y+)/.test(fmt))   
		  fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
		for(var k in o)   
		  if(new RegExp("("+ k +")").test(fmt))   
		fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
		return fmt;   
	}
	
	function restart (identidy) {
		var day = document.getElementById("day_" + identidy).value;
		var hour = document.getElementById("hour_" + identidy).value;
		var min = document.getElementById("min_" + identidy).value;
		var sec = document.getElementById("sec_" + identidy).value;
		var inv = document.getElementById("inv_" + identidy).value;
		var conf = day + ";" + hour + ";" + min + ";" + sec + ";" + inv;
		$.ajax({url:"scheduled?type=" + identidy + "&conf=" + conf + "&status=1", async:false});
		var url = location.search;
		location.href = url;
	}
	
	function stop (identidy) {
		$.ajax({url:"scheduled?type=" + identidy + "&status=2", async:false});
		var url = location.search;
		location.href = url;
	}
	
	/*
	*全局日期梯度
	*/
	var grads = 0;
	$(document).ready(function(){
		var url = location.search;
	 	if (url.indexOf("grads") != 0) {
	 		var str = url.substr(1);
	 		grads = str.substr(6);
	 	}
		/*
		*全局日期
		*/
		var nowDate = new Date();
		var showDate = new Date(nowDate.getTime() + (grads*24*60*60*1000)).Format("yyyy-MM-dd");
		
		var scheduledList = null;
		scheduledList=$.ajax({url:"scheduled?type=0", async:false});
		var scheduledText = scheduledList.responseText;
		var scheduledJson = $.parseJSON(scheduledText); 
		var scheduledSize = scheduledJson.length;
		for (var index = 0; index < scheduledSize; index++) {
			var scheduledNode = scheduledJson[index];
			var identidy = scheduledNode.identidy;
			var status = scheduledNode.status;
			var runTime = "";
			if (status == 0) {
				runTime = "休息中";
			} else if (status == 1) {
				runTime = "运行中";
			} else {
				runTime = "停止中";
			}
			var conf = scheduledNode.conf;

			$("#schedu").append("<tr><td id=\"type_" + identidy + "\">" + identidy + "<td>" + runTime + "</td><td>在本周第<input style = \"width:20px;\"  id=\"day_" + identidy + "\" type=\"text\" value=\"" + conf[0] + "\" />天</td><td><input style = \"width:20px;\" type=\"text\" id=\"hour_" + identidy + "\" value=\"" + conf[1] + "\"  />点</td><td><input style = \"width:20px;\" type=\"text\" id=\"min_" + identidy + "\" value=\"" + conf[2] + "\" />分</td><td><input style = \"width:20px;\" type=\"text\" id=\"sec_" + identidy + "\" value=\"" + conf[3] + "\" />秒启动</td><td>间隔<input style = \"width:20px;\" type=\"text\" id=\"inv_" + identidy + "\"  value=\"" + conf[4] + "\" />小时</td><td><input type=\"button\" onclick=\"restart('" + identidy + "')\" value=\"重启\" /></td><td><input type=\"button\" onclick=\"stop('" + identidy + "')\" value=\"停止\" /></td></tr>");
		}
		
		/*
		*Task获取时间展示
		*/
		var TaskhighChartsEntity = null;
		var beforeNum = grads - 1;
		var afterNum = grads*1 + 1;
		$("#beforeDay").attr('href','index.jsp?grads=' + beforeNum);
		$('#afterDay').attr("href","index.jsp?grads=" + afterNum);
		/*
	  	*Clawer平均获取时间和获取成功率展示
	  	*/
	 	var nodesListStr = null;
		nodesListStr=$.ajax({url:"mornitor?type=0", async:false});
		var nodesText = nodesListStr.responseText;
		var nodesJson = eval("(" + nodesText + ")");
		var size = nodesJson.length;
		for (var index = 0; index < size; index++) {
			var type = nodesJson[index];
			$('#mornitor').append('<tr><td id="' + type + '" style="min-width:1200px;height:400px"></td></tr>');
			var clawerHighChartsEntity=$.ajax({url:"mornitor?type=" + type + "&grads=" + grads, async:false});
			var clawerText = clawerHighChartsEntity.responseText;
			var clawerTextJson = eval("(" + clawerText + ")");
		    $('#' + type + '').highcharts({
		        chart: {
		            zoomType: 'xy'
		        },
		        title: {
		            text: '' + type + '获取耗时与成功率'
		        },
		        subtitle: {
		            text: '日期:' + showDate + ''
		        },
		        xAxis: [{
		            categories: clawerTextJson.categories
		        }],
		        yAxis: [{ // Primary yAxis
		            labels: {
		                format: '{value} %',
		                style: {
		                    color: '#89A54E'
		                }
		            },
		            title: {
		                text: '成功率',
		                style: {
		                    color: '#89A54E'
		                }
		            }
		        }, { // Secondary yAxis
		            title: {
		                text: '耗时',
		                style: {
		                    color: '#4572A7'
		                }
		            },
		            labels: {
		                format: '{value} min',
		                style: {
		                    color: '#4572A7'
		                }
		            },
		            opposite: true
		        }],
		        tooltip: {
		            shared: true
		        },
		        legend: {
		            layout: 'vertical',
		            align: 'left',
		            x: 120,
		            verticalAlign: 'top',
		            y: 100,
		            floating: true,
		            backgroundColor: '#FFFFFF'
		        },
		        series: clawerTextJson.series
		    });
			
		}
		
		
	});
	
   
  </script>
</head>
<style>
		/* 设置边框 */
		table, th, td{
		    border: 1px solid blue;
		}
		/* 设置table位置 */
		table{
		    margin:0px auto; /* 效果等同 <tabel align="center">*/
		    width:80% /* 必须制定宽度 */
		}
		/* 单元格对齐 */
		td{
		    text-align:center;
		}
</style>

<body>
	<div align="center"><a id='beforeDay' href=''>查看前一天</a>|<a id='afterDay' href=''>查看后一天</a>|<a href="index.jsp">返回主页</a></div>
	<table  border="1" id="schedu">
	</table>
	<table  border="1" id="mornitor">
	</table>
</body>
</html>

