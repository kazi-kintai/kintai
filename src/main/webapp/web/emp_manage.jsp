<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.EmpBean" %>
<%@ page import="kintai.DeptBean" %>
<%@ page import="kintai.PostBean" %>
<%@ page import="kintai.UserBean" %>
<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRole() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    // リストを取得
    List<EmpBean> empList = (List<EmpBean>) request.getAttribute("empList");
    List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
    List<PostBean> postList = (List<PostBean>) request.getAttribute("postList");
    String message = (String) request.getAttribute("message");
    Boolean success = (Boolean) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>社員管理</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        h1 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
        }
        
        /* メッセージ表示エリア */
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
        
        /* 新規追加フォーム */
        .add-form {
            background-color: #f8f9fa;
            padding: 20px;
            margin-bottom: 30px;
            border-radius: 4px;
            border: 1px solid #dee2e6;
        }
        
        .add-form h2 {
            margin-top: 0;
            color: #495057;
        }
        
        .form-group {
            margin-bottom: 15px;
            display: inline-block;
            margin-right: 20px;
        }
        
        .form-group label {
            display: inline-block;
            width: 100px;
            font-weight: bold;
        }
        
        .form-group input[type="text"],
        .form-group input[type="password"],
        .form-group select {
            width: 150px;
            padding: 5px;
            border: 1px solid #ced4da;
            border-radius: 4px;
        }
        
        /* ボタンスタイル */
        .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            margin-right: 5px;
        }
        
        .btn-primary {
            background-color: #007bff;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #0056b3;
        }
        
        .btn-success {
            background-color: #28a745;
            color: white;
        }
        
        .btn-success:hover {
            background-color: #218838;
        }
        
        .btn-danger {
            background-color: #dc3545;
            color: white;
        }
        
        .btn-danger:hover {
            background-color: #c82333;
        }
        
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #545b62;
        }
        
        /* テーブルスタイル */
        .emp-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        .emp-table th, .emp-table td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
        }
        
        .emp-table th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #495057;
        }
        
        .emp-table tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        
        .emp-table tr:hover {
            background-color: #e9ecef;
        }
        
        /* 編集フォーム */
        .edit-row {
            display: none;
        }
        
        .edit-row input[type="text"],
        .edit-row input[type="password"],
        .edit-row select {
            width: 120px;
            padding: 3px;
            border: 1px solid #ced4da;
            border-radius: 4px;
        }
        
        /* 戻るボタン */
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
        
        .form-inline {
            display: inline;
        }
    </style>
    <script>
        // 編集モードの切り替え
        function toggleEdit(empNo) {
            var displayRow = document.getElementById('display-' + empNo);
            var editRow = document.getElementById('edit-' + empNo);
            
            if (editRow.style.display === 'table-row') {
                displayRow.style.display = 'table-row';
                editRow.style.display = 'none';
            } else {
                displayRow.style.display = 'none';
                editRow.style.display = 'table-row';
            }
        }
        
        // 削除確認
        function confirmDelete(empNo, empName) {
            if (confirm('社員「' + empName + '」を削除してもよろしいですか？')) {
                document.getElementById('deleteForm-' + empNo).submit();
            }
        }
    </script>
</head>
<body>
    <div class="container">
        <h1>社員管理</h1>
        
        <%-- メッセージ表示 --%>
        <% if (message != null && !message.isEmpty()) { %>
            <div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
                <%= message %>
            </div>
        <% } %>
        
        <%-- 新規追加フォーム --%>
        <div class="add-form">
            <h2>新規社員追加</h2>
            <form method="post" action="<%= request.getContextPath() %>/empManage">
                <input type="hidden" name="action" value="add">
                <div class="form-group">
                    <label for="newEmpNo">社員番号：</label>
                    <input type="text" id="newEmpNo" name="empNo" maxlength="10" required>
                </div>
                <div class="form-group">
                    <label for="newEmpName">社員名：</label>
                    <input type="text" id="newEmpName" name="empName" maxlength="50" required>
                </div>
                <div class="form-group">
                    <label for="newDeptNo">部署：</label>
                    <select id="newDeptNo" name="deptNo" required>
                        <option value="">選択してください</option>
                        <% if (deptList != null) { 
                            for (DeptBean dept : deptList) { %>
                                <option value="<%= dept.getDeptNo() %>"><%= dept.getDeptName() %></option>
                        <% }} %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="newPostNo">役職：</label>
                    <select id="newPostNo" name="postNo" required>
                        <option value="">選択してください</option>
                        <% if (postList != null) { 
                            for (PostBean post : postList) { %>
                                <option value="<%= post.getPostNo() %>"><%= post.getPostName() %></option>
                        <% }} %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="newPass">パスワード：</label>
                    <input type="password" id="newPass" name="pass" maxlength="20" required>
                </div>
                <div class="form-group">
                    <label for="newRole">権限：</label>
                    <select id="newRole" name="role" required>
                        <option value="0">一般社員</option>
                        <option value="1">管理者</option>
                    </select>
                </div>
                <div style="margin-top: 15px;">
                    <button type="submit" class="btn btn-primary">追加</button>
                </div>
            </form>
        </div>
        
        <%-- 社員一覧テーブル --%>
        <h2>社員一覧</h2>
        <table class="emp-table">
            <thead>
                <tr>
                    <th>社員番号</th>
                    <th>社員名</th>
                    <th>部署</th>
                    <th>役職</th>
                    <th>権限</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% if (empList != null && !empList.isEmpty()) { %>
                    <% for (EmpBean emp : empList) { %>
                        <%-- 表示行 --%>
                        <tr id="display-<%= emp.getEmpNo() %>">
                            <td><%= emp.getEmpNo() %></td>
                            <td><%= emp.getEmpName() %></td>
                            <td><%= emp.getDeptName() != null ? emp.getDeptName() : emp.getDeptNo() %></td>
                            <td><%= emp.getPostName() != null ? emp.getPostName() : emp.getPostNo() %></td>
                            <td><%= emp.getRole() == 1 ? "管理者" : "一般社員" %></td>
                            <td>
                                <button class="btn btn-success" onclick="toggleEdit('<%= emp.getEmpNo() %>')">編集</button>
                                <button class="btn btn-danger" onclick="confirmDelete('<%= emp.getEmpNo() %>', '<%= emp.getEmpName() %>')">削除</button>
                                
                                <%-- 削除用フォーム（非表示） --%>
                                <form id="deleteForm-<%= emp.getEmpNo() %>" method="post" 
                                      action="<%= request.getContextPath() %>/empManage" style="display: none;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
                                </form>
                            </td>
                        </tr>
                        
                        <%-- 編集行（初期状態では非表示） --%>
                        <tr id="edit-<%= emp.getEmpNo() %>" class="edit-row" style="display: none;">
                            <td><%= emp.getEmpNo() %></td>
                            <td colspan="5">
                                <form method="post" action="<%= request.getContextPath() %>/empManage" class="form-inline">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
                                    
                                    社員名：<input type="text" name="empName" value="<%= emp.getEmpName() %>" maxlength="50" required>
                                    
                                    部署：
                                    <select name="deptNo" required>
                                        <% if (deptList != null) { 
                                            for (DeptBean dept : deptList) { %>
                                                <option value="<%= dept.getDeptNo() %>" 
                                                    <%= dept.getDeptNo().equals(emp.getDeptNo()) ? "selected" : "" %>>
                                                    <%= dept.getDeptName() %>
                                                </option>
                                        <% }} %>
                                    </select>
                                    
                                    役職：
                                    <select name="postNo" required>
                                        <% if (postList != null) { 
                                            for (PostBean post : postList) { %>
                                                <option value="<%= post.getPostNo() %>" 
                                                    <%= post.getPostNo().equals(emp.getPostNo()) ? "selected" : "" %>>
                                                    <%= post.getPostName() %>
                                                </option>
                                        <% }} %>
                                    </select>
                                    
                                    パスワード：<input type="password" name="pass" value="<%= emp.getPass() %>" maxlength="20" required>
                                    
                                    権限：
                                    <select name="role" required>
                                        <option value="0" <%= emp.getRole() == 0 ? "selected" : "" %>>一般社員</option>
                                        <option value="1" <%= emp.getRole() == 1 ? "selected" : "" %>>管理者</option>
                                    </select>
                                    
                                    <button type="submit" class="btn btn-primary">保存</button>
                                    <button type="button" class="btn btn-secondary" onclick="toggleEdit('<%= emp.getEmpNo() %>')">キャンセル</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                <% } else { %>
                    <tr>
                        <td colspan="6" style="text-align: center;">社員データがありません</td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        
        <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理者メニューへ戻る</a>
    </div>
</body>
</html>