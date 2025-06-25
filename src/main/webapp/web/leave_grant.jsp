<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<html>
<head>
  <title>休暇付与結果</title>
</head>
<body>

<h2>休暇付与処理結果</h2>

<p>本日：<%= LocalDate.now() %></p>

<%
  Integer annualGranted = (Integer) request.getAttribute("annualGranted");
  Integer initialGranted = (Integer) request.getAttribute("initialGranted");
  Integer specialGranted = (Integer) request.getAttribute("specialGranted");
%>

<table border="1" cellpadding="5" cellspacing="0">
  <tr><th>付与種別</th><th>対象人数</th></tr>
  <tr><td>年次有給休暇</td><td><%= annualGranted != null ? annualGranted : 0 %> 人</td></tr>
  <tr><td>初回付与（3か月＋6か月）</td><td><%= initialGranted != null ? initialGranted : 0 %> 人</td></tr>
  <tr><td>特別休暇</td><td><%= specialGranted != null ? specialGranted : 0 %> 人</td></tr>
</table>

<br/>

<form action="LeaveRecServlet" method="get">
  <button type="submit">休暇管理画面へ戻る</button>
</form>

<form action="/web/admin_menu.jsp" method="get">
  <button type="submit">管理者メニューに戻る</button>
</form>

</body>
</html>