<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, kintai.*" %>
<html>
<head>
  <title>休暇申請管理</title>
</head>
<body>

<!-- ログアウト -->
<form action="LogoutServlet" method="post" style="text-align:right;">
  <button type="submit">ログアウト</button>
</form>

<h2>休暇申請管理画面</h2>

<!-- 検索フォーム -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="search" />
  従業員番号:
  <select name="empNo">
<%
  List<Employee> empList = (List<Employee>) request.getAttribute("empList");
  String selectedEmpNo = (String) request.getAttribute("selectedEmpNo");
  if (empList != null) {
    for (Employee emp : empList) {
      String selected = (emp.getEmpNo().equals(selectedEmpNo)) ? "selected" : "";
%>
    <option value="<%= emp.getEmpNo() %>" <%= selected %>><%= emp.getEmpNo() %> - <%= emp.getName() %></option>
<%
    }
  }
%>
  </select>

  部署:
  <select name="dept">
<%
  List<String> deptList = (List<String>) request.getAttribute("deptList");
  String selectedDept = (String) request.getAttribute("selectedDept");
  if (deptList != null) {
    for (String dept : deptList) {
      String selected = dept.equals(selectedDept) ? "selected" : "";
%>
    <option value="<%= dept %>" <%= selected %>><%= dept %></option>
<%
    }
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

<!-- 新規申請フォーム -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="add" />
  <input type="hidden" name="empNo" value="<%= selectedEmpNo != null ? selectedEmpNo : "" %>" />
  開始日: <input type="date" name="startDate" required />
  終了日: <input type="date" name="endDate" required />
  休日種別:
  <select name="leaveTypeId">
<%
  List<LeaveType> leaveTypeList = (List<LeaveType>) request.getAttribute("leaveTypeList");
  if (leaveTypeList != null) {
    for (LeaveType type : leaveTypeList) {
%>
    <option value="<%= type.getLeaveTypeId() %>"><%= type.getLeaveTypeName() %></option>
<%
    }
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
  List<LeaveRequest> leaveList = (List<LeaveRequest>) request.getAttribute("leaveList");
  if (leaveList != null) {
    for (LeaveRequest leave : leaveList) {
%>
  <tr>
    <form action="LeaveRecServlet" method="post">
      <input type="hidden" name="mode" value="update" />
      <input type="hidden" name="leaveId" value="<%= leave.getLeaveId() %>" />
      <input type="hidden" name="empNo" value="<%= leave.getEmpNo() %>" />
      <td><input type="date" name="startDate" value="<%= leave.getStartDate() %>" /></td>
      <td><input type="date" name="endDate" value="<%= leave.getEndDate() %>" /></td>
      <td>
        <select name="leaveTypeId">
<%
  for (LeaveType type : leaveTypeList) {
    String selected = (type.getLeaveTypeId() == leave.getLeaveTypeId()) ? "selected" : "";
%>
          <option value="<%= type.getLeaveTypeId() %>" <%= selected %>><%= type.getLeaveTypeName() %></option>
<%
  }
%>
        </select>
      </td>
      <td><input type="text" name="reason" value="<%= leave.getReason() %>" /></td>
      <td><input type="text" name="approvedBy" value="<%= leave.getApprovedBy() %>" /></td>
      <td>
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

<!-- 戻る -->
<form action="AdminMenu.jsp" method="get">
  <button type="submit">管理部基本メニューへ戻る</