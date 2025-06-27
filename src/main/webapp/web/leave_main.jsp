<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, kintai.*" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRoleId() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }

    List<EmpBean> empList = (List<EmpBean>) request.getAttribute("empList");
    List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
    List<LeaveTypeBean> leaveTypeList = (List<LeaveTypeBean>) request.getAttribute("leaveTypeList");
    List<LeaveRecBean> leaveList = (List<LeaveRecBean>) request.getAttribute("leaveList");

    String selectedEmpNo = (String) request.getAttribute("selectedEmpNo");
    String selectedDept = (String) request.getAttribute("selectedDept");
    Integer remainingPaidLeave = (Integer) request.getAttribute("remainingPaidLeave");
    Integer remainingSpecialLeave = (Integer) request.getAttribute("remainingSpecialLeave");
    Integer remainingCompLeave = (Integer) request.getAttribute("remainingCompLeave");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>休暇申請管理</title>
	<style>
    body {
      font-family: 'Arial', sans-serif;
      background-color: #f5f5f5;
      margin: 0;
      padding: 20px;
    }

    .container {
      max-width: 1200px;
      margin: auto;
      background: #ffffff;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }

    h1, h2 {
      border-bottom: 2px solid #ccc;
      padding-bottom: 5px;
      margin-bottom: 20px;
    }

    .form-inline {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 20px;
      align-items: center;
    }

    label {
      font-weight: bold;
      margin-right: 5px;
    }

    input[type="text"],
    input[type="date"],
    select {
      padding: 6px;
      font-size: 14px;
      border: 1px solid #ccc;
      border-radius: 4px;
    }

    button {
      padding: 6px 12px;
      font-size: 14px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-primary {
      background-color: #1976d2;
      color: white;
    }

    .btn-success {
      background-color: #388e3c;
      color: white;
    }

    .btn-danger {
      background-color: #d32f2f;
      color: white;
    }

    .emp-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 20px;
    }

    .emp-table th,
    .emp-table td {
      border: 1px solid #ccc;
      padding: 8px;
      text-align: left;
    }

    .emp-table th {
      background-color: #eeeeee;
    }

    .message {
      margin: 10px 0;
      padding: 10px;
      border-radius: 5px;
    }

    .success-message {
      background-color: #e8f5e9;
      color: #2e7d32;
      border: 1px solid #c8e6c9;
    }

    .error-message {
      background-color: #ffebee;
      color: #c62828;
      border: 1px solid #ef9a9a;
    }

    .section {
      margin-bottom: 30px;
    }

    .back-link {
      display: inline-block;
      margin-top: 20px;
      text-decoration: none;
      color: #1976d2;
      font-weight: bold;
    }

    .back-link:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
<div class="container">
    <h1>休暇申請・管理</h1>

    <form method="post" action="LeaveRecServlet" class="form-inline">
        <input type="hidden" name="mode" value="search" />
        <label>従業員：</label>
        <select name="empNo">
            <% for (EmpBean emp : empList) { %>
                <option value="<%= emp.getEmpNo() %>" <%= emp.getEmpNo().equals(selectedEmpNo) ? "selected" : "" %>>
                    <%= emp.getEmpNo() %> - <%= emp.getEmpName() %>
                </option>
            <% } %>
        </select>

        <label>部署：</label>
        <select name="dept">
            <% for (DeptBean dept : deptList) { %>
                <option value="<%= dept.getDeptNo() %>" <%= dept.getDeptNo().equals(selectedDept) ? "selected" : "" %>>
                    <%= dept.getDeptName() %>
                </option>
            <% } %>
        </select>
        <button class="btn btn-primary" type="submit">検索</button>
    </form>

    <div class="section">
        <h2>残日数</h2>
        <p>有給：<%= remainingPaidLeave %> 日　特別：<%= remainingSpecialLeave %> 日　代休：<%= remainingCompLeave %> 日</p>
    </div>

    <div class="section">
        <h2>休暇申請の追加</h2>
        <form method="post" action="LeaveRecServlet" class="form-inline">
            <input type="hidden" name="mode" value="add" />
            <input type="hidden" name="empNo" value="<%= selectedEmpNo %>" />
            <label>開始日：</label><input type="date" name="startDate" required />
            <label>終了日：</label><input type="date" name="endDate" required />
            <label>種別：</label>
            <select name="leaveTypeId">
                <% for (LeaveTypeBean type : leaveTypeList) { %>
                    <option value="<%= type.getLeaveTypeId() %>"><%= type.getLeaveTypeName() %></option>
                <% } %>
            </select>
            <label>理由：</label><input type="text" name="reason" />
            <!-- <label>承認者：</label><input type="text" name="approvedBy" /> -->
            <button class="btn btn-success" type="submit">申請</button>
        </form>
    </div>

    <div class="section">
        <h2>申請済み一覧</h2>
        <table class="emp-table">
            <thead>
            <tr><th>開始日</th><th>終了日</th><th>種別</th><th>理由</th><!-- <th>承認者</th> --><th>操作</th></tr>
            </thead>
            <tbody>
            <% for (LeaveRecBean rec : leaveList) { %>
                <tr>
                    <form method="post" action="LeaveRecServlet">
                        <input type="hidden" name="mode" value="update" />
                        <input type="hidden" name="leaveId" value="<%= rec.getLeaveId() %>" />
                        <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
                        <td><input type="date" name="startDate" value="<%= rec.getStartDate() %>" /></td>
                        <td><input type="date" name="endDate" value="<%= rec.getEndDate() %>" /></td>
                        <td>
                            <select name="leaveTypeId">
                                <% for (LeaveTypeBean type : leaveTypeList) {
                                    String sel = (type.getLeaveTypeId() == rec.getLeaveTypeId()) ? "selected" : "";
                                %>
                                    <option value="<%= type.getLeaveTypeId() %>" <%= sel %>><%= type.getLeaveTypeName() %></option>
                                <% } %>
                            </select>
                        </td>
                        <td><input type="text" name="reason" value="<%= rec.getReason() %>" /></td>
                        <!-- <td><input type="text" name="approvedBy" value="<%= rec.getApprovedBy() %>" /></td> -->
                        <td>
                            <button class="btn btn-primary" type="submit">保存</button>
                    </form>
                    <form method="post" action="LeaveRecServlet" style="display:inline;">
                        <input type="hidden" name="mode" value="delete" />
                        <input type="hidden" name="leaveId" value="<%= rec.getLeaveId() %>" />
                        <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
                        <button class="btn btn-danger" onclick="return confirm('削除しますか？')">削除</button>
                    </form>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理メニューに戻る</a>
</div>
</body>
</html>