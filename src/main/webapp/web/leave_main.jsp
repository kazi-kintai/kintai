<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, kintai.*" %>
<%
  String deptNoFilter = (String) request.getAttribute("deptNoFilter");
  List<EmpBean> empList = (List<EmpBean>) request.getAttribute("empList");
  List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
  List<LeaveTypeBean> leaveTypeList = (List<LeaveTypeBean>) request.getAttribute("leaveTypeList");
  String selectedEmpNo = (String) request.getAttribute("selectedEmpNo");
  String selectedDept = (String) request.getAttribute("selectedDept");
%>
<html>
<head>
  <title>休暇申請・付与管理</title>
</head>
<body>

<!-- ログアウト -->
<form action="LogoutServlet" method="post" style="text-align:right;">
  <button type="submit">ログアウト</button>
</form>

<h2>休暇申請・付与管理画面</h2>

<!-- 休暇付与トリガー -->
<form action="leaveGrant" method="post">
  <button type="submit">年次有給・特別休暇を付与する</button>
</form>

<!-- 結果表示 -->
<%
  Integer annualGranted = (Integer) request.getAttribute("annualGranted");
  Integer specialGranted = (Integer) request.getAttribute("specialGranted");
  Integer initialGranted = (Integer) request.getAttribute("initialGranted");

  if (annualGranted != null || specialGranted != null || initialGranted != null) {
%>
<p style="color:green;">
  年次付与: <%= annualGranted != null ? annualGranted : 0 %>人　
  初回付与: <%= initialGranted != null ? initialGranted : 0 %>人　
  特別休暇: <%= specialGranted != null ? specialGranted : 0 %>人
</p>
<%
  }
%>

<hr/>

<!-- 検索フォーム -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="search" />
  従業員番号:
  <select name="empNo">
    <% if (empList != null) {
         for (EmpBean emp : empList) {
           String selected = emp.getEmpNo().equals(selectedEmpNo) ? "selected" : "";
    %>
      <option value="<%= emp.getEmpNo() %>" <%= selected %>><%= emp.getEmpNo() %> - <%= emp.getEmpName() %></option>
    <%   }
       }
    %>
  </select>

  部署:
  <select name="dept">
    <% if (deptList != null) {
         for (DeptBean dept : deptList) {
           String selected = dept.getDeptNo().equals(selectedDept) ? "selected" : "";
    %>
      <option value="<%= dept.getDeptNo() %>" <%= selected %>><%= dept.getDeptName() %></option>
    <%   }
       }
    %>
  </select>
  <button type="submit">検索</button>
</form>

<hr/>

<!-- 残日数表示 -->
<p>残有給休暇: <%= request.getAttribute("remainingPaidLeave") %> 日</p>
<p>残特別休暇: <%= request.getAttribute("remainingSpecialLeave") %> 日</p>
<p>残代休: <%= request.getAttribute("remainingCompLeave") %> 日</p>

<!-- 新規申請 -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="add" />
  <input type="hidden" name="empNo" value="<%= selectedEmpNo != null ? selectedEmpNo : "" %>" />
  開始日: <input type="date" name="startDate" required />
  終了日: <input type="date" name="endDate" required />
  休日種別:
  <select name="leaveTypeId">
    <% if (leaveTypeList != null) {
         for (LeaveTypeBean type : leaveTypeList) {
    %>
      <option value="<%= type.getLeaveTypeId() %>"><%= type.getLeaveTypeName() %></option>
    <%   }
       }
    %>
  </select>
  理由: <input type="text" name="reason" />
  承認者: <input type="text" name="approvedBy" />
  <button type="submit">追加</button>
</form>

<hr/>

<h3>休暇申請一覧</h3>
<table border="1">
  <tr>
    <th>開始日</th><th>終了日</th><th>休日種別</th><th>理由</th><th>承認者</th><th>操作</th>
  </tr>
<%
  List<LeaveRecBean> leaveList = (List<LeaveRecBean>) request.getAttribute("leaveList");
  if (leaveList != null) {
    for (LeaveRecBean leave : leaveList) {
%>
  <tr>
    <td colspan="6">
      <form action="LeaveRecServlet" method="post" style="display:flex; gap:10px; align-items:center;">
        <input type="hidden" name="mode" value="update" />
        <input type="hidden" name="leaveId" value="<%= leave.getLeaveId() %>" />
        <input type="hidden" name="empNo" value="<%= leave.getEmpNo() %>" />
        <input type="date" name="startDate" value="<%= leave.getStartDate() != null ? leave.getStartDate().toString() : "" %>" />
        <input type="date" name="endDate" value="<%= leave.getEndDate() != null ? leave.getEndDate().toString() : "" %>" />
        <select name="leaveTypeId">
<%
        for (LeaveTypeBean type : leaveTypeList) {
            String selected = (type.getLeaveTypeId() == leave.getLeaveTypeId()) ? "selected" : "";
%>
          <option value="<%= type.getLeaveTypeId() %>" <%= selected %>><%= type.getLeaveTypeName() %></option>
<%
        }
%>
        </select>
        <input type="text" name="reason" value="<%= leave.getReason() != null ? leave.getReason() : "" %>" />
        <input type="text" name="approvedBy" value="<%= leave.getApprovedBy() != null ? leave.getApprovedBy() : "" %>" />
        <button type="submit">保存</button>
      </form>
      <form action="LeaveRecServlet" method="post" style="display:inline;">
        <input type="hidden" name="mode" value="delete" />
        <input type="hidden" name="leaveId" value="<%= leave.getLeaveId() %>" />
        <input type="hidden" name="empNo" value="<%= leave.getEmpNo() %>" />
        <button type="submit" onclick="return confirm('削除しますか？')">削除</button>
      </form>
    </td>
  </tr>
<%
    }
  }
%>
</table>

<br/>
<form action="web/admin_menu.jsp" method="get">
  <button type="submit">管理メニューに戻る</button>
</form>

</body>
</html>