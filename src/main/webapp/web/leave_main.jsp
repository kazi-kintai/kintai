<%@ page import="java.util.*, kintai.*, kintai.dao.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    LeaveRec editing = (LeaveRec) request.getAttribute("editingRec");
    boolean isEditing = editing != null;
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>休暇申請</title>
    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background-color: #f4f7f9;
            margin: 0;
            padding: 20px;
            color: #333;
        }

        h2, h3 {
            color: #2b4a6f;
            border-bottom: 2px solid #d0d7e4;
            padding-bottom: 5px;
            margin-top: 30px;
        }

        form {
            background-color: #ffffff;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        }

        label {
            display: block;
            margin: 12px 0 6px;
            font-weight: 600;
        }

        select, input[type="date"], button {
            font-size: 14px;
            padding: 6px 10px;
            margin-top: 4px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        select, input[type="date"] {
            width: 200px;
        }

        button {
            background-color: #2b79c2;
            color: white;
            border: none;
            cursor: pointer;
            transition: background-color 0.2s ease;
        }

        button:hover {
            background-color: #1d5f9d;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background-color: #fff;
            margin-top: 10px;
            box-shadow: 0 1px 4px rgba(0,0,0,0.1);
        }

        table th, table td {
            padding: 10px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        table th {
            background-color: #e1e9f0;
            color: #2b4a6f;
        }

        p strong {
            color: #2b4a6f;
        }

        p {
            margin: 6px 0;
        }

        p[style*="color:red"] {
            font-weight: bold;
            margin-top: 10px;
        }
    </style>
</head>
<body>
<!-- The rest of your JSP content goes here -->
<body>

<form method="post" action="LeaveRecServlet">
    <input type="hidden" name="mode" value="search" />
    
    <label>従業員番号 / 氏名:
        <select name="empNo">
            <option value="">全ての従業員</option>
            <% for (Employee e : (List<Employee>) request.getAttribute("employeeList")) { %>
                <option value="<%= e.getEmpNo() %>" 
                    <%= e.getEmpNo().equals(request.getAttribute("selectedEmpNo")) ? "selected" : "" %>>
                    <%= e.getEmpNo() %> - <%= e.getEmpName() %>
                </option>
            <% } %>
        </select>
    </label>

    <label>部署:
        <select name="deptNo">
            <option value="">全ての部署</option>
            <% for (String dept : (List<String>) request.getAttribute("departmentList")) { %>
                <option value="<%= dept %>"><%= dept %></option>
            <% } %>
        </select>
    </label>

    <button type="submit">検索</button>
</form>

<h2>休暇登録フォーム</h2>
<%
    Employee emp = (Employee) request.getAttribute("employee");
    LeaveBalance balance = (LeaveBalance) request.getAttribute("balance");
    List<LeaveType> leaveTypeList = (List<LeaveType>) request.getAttribute("leaveTypeList");
    List<LeaveRec> leaveList = (List<LeaveRec>) request.getAttribute("leaveList");
    String error = (String) request.getAttribute("error");
%>

<% if (emp != null) { %>
    <p>部署: <strong><%= emp.getDeptNo() %></strong>　氏名: <strong><%= emp.getEmpName() %></strong></p>
    <p>残有給休暇: <strong><%= request.getAttribute("remainingPaidLeave") %></strong> 日</p>
    <p>残特別休暇: <strong><%= request.getAttribute("remainingSpecialLeave") %></strong> 日</p>
    <p>残代休: <strong><%= request.getAttribute("remainingCompLeave") %></strong> 日</p>
<% } %>

<% if (error != null) { %>
    <p style="color:red;"><%= error %></p>
<% } %>

<!-- 登録 / 更新フォーム -->
<form action="LeaveRecServlet" method="post">
    <input type="hidden" name="mode" value="<%= isEditing ? "update" : "add" %>" />
    <input type="hidden" name="empNo" value="<%= emp != null ? emp.getEmpNo() : "" %>" />
    <% if (isEditing) { %>
        <input type="hidden" name="leaveId" value="<%= editing.getLeaveId() %>" />
    <% } %>

    <label>開始日: 
        <input type="date" name="startDate" value="<%= isEditing ? editing.getStartDate() : "" %>" required />
    </label><br/>
    <label>終了日: 
        <input type="date" name="endDate" value="<%= isEditing ? editing.getEndDate() : "" %>" required />
    </label><br/>
    <label>休日種別:
        <select name="leaveTypeId">
            <% for (LeaveType type : leaveTypeList) { %>
                <option value="<%= type.getLeaveTypeId() %>"
                    <%= isEditing && type.getLeaveTypeId() == editing.getLeaveTypeId() ? "selected" : "" %>>
                    <%= type.getLeaveTypeName() %>
                </option>
            <% } %>
        </select>
    </label><br/>

    <button type="submit"><%= isEditing ? "更新" : "追加" %></button>
</form>

<% if (isEditing) { %>
    <form action="LeaveRecServlet" method="post" style="display:inline;">
        <input type="hidden" name="mode" value="search" />
        <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>" />
        <button type="submit">キャンセル</button>
    </form>
<% } %>

<hr/>

<h3>申請休暇一覧</h3>
<table border="1">
<tr>
    <th>休暇種別</th>
    <th>作成日付</th>
    <th>開始日</th>
    <th>終了日</th>
    <th>操作</th>
</tr>
<% for (LeaveRec rec : leaveList) {
     boolean isEditingRow = isEditing && (editing.getLeaveId() == rec.getLeaveId());
%>
<tr>
    <% if (isEditingRow) { %>
    <form action="LeaveRecServlet" method="post">
        <input type="hidden" name="mode" value="update" />
        <input type="hidden" name="leaveId" value="<%= rec.getLeaveId() %>" />
        <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
        <td>
            <select name="leaveTypeId">
                <% for (LeaveType type : leaveTypeList) { %>
                    <option value="<%= type.getLeaveTypeId() %>" 
                        <%= rec.getLeaveTypeId() == type.getLeaveTypeId() ? "selected" : "" %>>
                        <%= type.getLeaveTypeName() %>
                    </option>
                <% } %>
            </select>
        </td>
        <td><%= rec.getCreatedAt() %></td>
        <td><input type="date" name="startDate" value="<%= rec.getStartDate() %>" required /></td>
        <td><input type="date" name="endDate" value="<%= rec.getEndDate() %>" required /></td>
        <td>
            <button type="submit">保存</button>
            <form action="LeaveRecServlet" method="post" style="display:inline;">
                <input type="hidden" name="mode" value="search" />
                <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
                <button type="submit">キャンセル</button>
            </form>
        </td>
    </form>
    <% } else { %>
        <td><%= rec.getLeaveTypeId() %></td>
        <td><%= rec.getCreatedAt() %></td>
        <td><%= rec.getStartDate() %></td>
        <td><%= rec.getEndDate() %></td>
        <td>
            <form action="LeaveRecServlet" method="post" style="display:inline;">
                <input type="hidden" name="mode" value="editForm" />
                <input type="hidden" name="leaveId" value="<%= rec.getLeaveId() %>" />
                <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
                <button type="submit">編集</button>
            </form>
            <form action="LeaveRecServlet" method="post" style="display:inline;">
                <input type="hidden" name="mode" value="delete" />
                <input type="hidden" name="leaveId" value="<%= rec.getLeaveId() %>" />
                <input type="hidden" name="empNo" value="<%= rec.getEmpNo() %>" />
                <button type="submit" onclick="return confirm('削除しますか？')">削除</button>
            </form>
        </td>
    <% } %>
</tr>
<% } %>
</table>

</body>
</html>