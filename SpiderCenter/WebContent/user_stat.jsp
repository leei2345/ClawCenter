<%@page import="java.util.Map.Entry"%>
<%@page import="com.aizhizu.service.house.ganji.UserCenter"%>
<%@page import="com.aizhizu.service.house.ganji.UserStat"%>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<html>

<head>
<title>用户状态视图</title>
</head>
<style>
		/* 设置边框 */
		th, td{
		    border: 1px solid blue;
		}
		/* 设置table位置 */
		table{
		    margin:0px auto; /* 效果等同 <tabel align="center">*/
		    width:40% /* 必须制定宽度 */
		}
		/* 单元格对齐 */
		td{
		    text-align:center;
		}
</style>

<body>
	<br/>
	<div align="center"><a href="index.jsp">返回主页</a></div>
	<br/>
	<br/>
	<br/>
	<table  border="1" id="ganjiloginstat">
<%
	Map<String, UserStat> userStat = UserCenter.userStatusMap;
	Map<Integer, List<String[]>> sortMap = new HashMap<Integer, List<String[]>>();
	for (Entry<String, UserStat> entry : userStat.entrySet()) {
		String name = entry.getKey();
		UserStat stat = entry.getValue();
		String message = stat.getStatus();
		int statCode = stat.getStatusCode();
		String color = "";
		switch (statCode) {
			case 0:
			color = "#99FF33";break;
			case 1:
			color = "#FF3333"; break;
			case 2:
			color = "#FF00CC";break;
			case 3:
			color = "#99CCFF";break;
			case 4:
			color = "#FFFF33";break;
			default: break;
		}
		String[] arr = new String[]{name, color, message};
		List<String[]> mapInner = sortMap.get(statCode);
		if (mapInner == null) {
			mapInner = new ArrayList<String[]>();
		}
		mapInner.add(arr);
		sortMap.put(statCode, mapInner);
	}
		for (Entry<Integer, List<String[]>> sortEntry : sortMap.entrySet()) {
			List<String[]> sortList = sortEntry.getValue();
			for (String[] info : sortList) {
				out.println("<tr><td>" + info[0] + "</td><td bgcolor='" + info[1] + "'>" + info[2] + "</td></tr>");
			}
		}
	
%>	
	</table>
</body>
</html>

