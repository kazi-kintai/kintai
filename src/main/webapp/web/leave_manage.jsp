<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.LeaveTypeBean" %>
<%@ page import="kintai.UserBean" %>
<%
    // ログインチェック（管理者のみアクセス可能）
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRoleId() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }

    List<LeaveTypeBean> leaveTypeList = (List<LeaveTypeBean>) request.getAttribute("leaveTypeList");
    String message = (String) request.getAttribute("message");
    Boolean success = (Boolean) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>休暇種別管理</title>
    <style>
        /* 基本スタイルは部署管理JSPと同様に調整してください */
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0; padding: 20px;
        }
        .container {
            max-width: 900px;
            margin: auto;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1, h2 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
        }
        .message {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
            text-align: center;
        }
        .success-message {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error-message {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: inline-block;
            width: 130px;
            font-weight: bold;
        }
        input[type="text"], select {
            width: 200px;
            padding: 5px;
            border: 1px solid #ced4da;
            border-radius: 4px;
            
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
        }
        th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #495057;
        }
        tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        tr:hover {
            background-color: #e9ecef;
        }
        .add-form {
            background-color: #f8f9fa;
            padding: 20px;
            margin-bottom: 30px;
            border-radius: 4px;
            border: 1px solid #dee2e6;
       } .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            margin-right: 5px;
            color: white;
        }
        .btn-primary { background-color: #007bff; }
        .btn-primary:hover { background-color: #0056b3; }
        .btn-success { background-color: #28a745; }
        .btn-success:hover { background-color: #218838; }
        .btn-danger { background-color: #dc3545; }
        .btn-danger:hover { background-color: #c82333; }
        .btn-secondary { background-color: #6c757d; }
        .btn-secondary:hover { background-color: #545b62; }
        .edit-form { display: none; }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 8px 16px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }
        .back-link:hover {
            background-color: #545b62;
        }
    </style>
    <script>
        // 編集フォームの表示切替
        function toggleEdit(leaveTypeId) {
            var displaySpan = document.getElementById('display-' + leaveTypeId);
            var editForm = document.getElementById('edit-' + leaveTypeId);
            if (editForm.style.display === 'inline') {
                displaySpan.style.display = 'inline';
                editForm.style.display = 'none';
            } else {
                displaySpan.style.display = 'none';
                editForm.style.display = 'inline';
            }
        }

        // 削除確認
        function confirmDelete(leaveTypeId, leaveTypeName) {
            if (confirm('休暇種別「' + leaveTypeName + '」を削除してもよろしいですか？')) {
                document.getElementById('deleteForm-' + leaveTypeId).submit();
            }
        }

        // 追加確認
        function confirmAdd(form) {
            var name = form.leaveTypeName.value;
            if (name.trim() === '') {
                alert('休暇種別名を入力してください。');
                return false;
            }
            return confirm('休暇種別「' + name + '」を追加してもよろしいですか？');
        }

        // 更新確認
        function confirmUpdate(form, leaveTypeId) {
            var name = form.leaveTypeName.value;
            if (name.trim() === '') {
                alert('休暇種別名を入力してください。');
                return false;
            }
            return confirm('休暇種別ID「' + leaveTypeId + '」の名称を「' + name + '」に更新してもよろしいですか？');
        }
    </script>
</head>
<body>
<div class="container">
    <h1>休暇種別管理</h1>

    <% if (message != null && !message.isEmpty()) { %>
        <div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
            <%= message %>
        </div>
    <% } %>

    <!-- 新規追加フォーム -->
    <div class="add-form">
        <h2>新規休暇種別追加</h2>
        <form method="post" action="<%= request.getContextPath() %>/leaveTypeManage" onsubmit="return confirmAdd(this)">
            <input type="hidden" name="action" value="add">
            <div class="form-group">
    <label for="leaveTypeId">休暇種別ID：</label>
    <input type="text" id="leaveTypeId" name="leaveTypeId" maxlength="5" required>
</div><div class="form-group">
                <label for="leaveTypeName">休暇種別名：</label>
                <input type="text" id="leaveTypeName" name="leaveTypeName" maxlength="50" required>
            </div>
            <div class="form-group">
                <label for="isPaid">有給フラグ：</label>
                <select id="isPaid" name="isPaid" required>
                    <option value="true">有給</option>
                    <option value="false">無給</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">追加</button>
        </form>
    </div>

    <!-- 休暇種別一覧テーブル -->
    <h2>休暇種別一覧</h2>
    <table>
        <thead>
            <tr>
                <th>休暇種別ID</th>
                <th>休暇種別名</th>
                <th>有給フラグ</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <% if (leaveTypeList != null && !leaveTypeList.isEmpty()) { %>
                <% for (LeaveTypeBean leaveType : leaveTypeList) { %>
                    <tr>
                        <td><%= leaveType.getLeaveTypeId() %></td>
                        <td>
                            <span id="display-<%= leaveType.getLeaveTypeId() %>">
                                <%= leaveType.getLeaveTypeName() %>
                            </span>

                            <!-- 編集フォーム -->
                            <form id="edit-<%= leaveType.getLeaveTypeId() %>" method="post" action="<%= request.getContextPath() %>/leaveTypeManage" class="edit-form" style="display:none;" onsubmit="return confirmUpdate(this, '<%= leaveType.getLeaveTypeId() %>')">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="leaveTypeId" value="<%= leaveType.getLeaveTypeId() %>">
                                <input type="text" name="leaveTypeName" value="<%= leaveType.getLeaveTypeName() %>" maxlength="50" required>
                                <select name="isPaid" required>
                                    <option value="true" <%= leaveType.isPaid() ? "selected" : "" %>>有給</option>
                                    <option value="false" <%= !leaveType.isPaid() ? "selected" : "" %>>無給</option>
                                </select>
                                <button type="submit" class="btn btn-primary">保存</button>
                                <button type="button" class="btn btn-secondary" onclick="toggleEdit('<%= leaveType.getLeaveTypeId() %>')">キャンセル</button>
                            </form>
                        </td>
                        <td><%= leaveType.isPaid() ? "有給" : "無給" %></td>
                        <td>
                            <button class="btn btn-success" onclick="toggleEdit('<%= leaveType.getLeaveTypeId() %>')">編集</button>
                            <button class="btn btn-danger" onclick="confirmDelete('<%= leaveType.getLeaveTypeId() %>', '<%= leaveType.getLeaveTypeName() %>')">削除</button>

                            <form id="deleteForm-<%= leaveType.getLeaveTypeId() %>" method="post" action="<%= request.getContextPath() %>/leaveTypeManage" style="display:none;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="leaveTypeId" value="<%= leaveType.getLeaveTypeId() %>">
                            </form>
                        </td>
                    </tr>
                <% } %>
            <% } else { %>
                <tr>
                    <td colspan="4" style="text-align:center;">休暇種別データがありません</td>
                </tr>
            <% } %>
        </tbody>
    </table>

    <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理部基本メニューへ戻る</a>
</div>
</body>
</html>
