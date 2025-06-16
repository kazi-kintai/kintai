<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.DeptBean" %>
<%@ page import="kintai.UserBean" %>
<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRole() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    // 部署リストを取得
    List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
    String message = (String) request.getAttribute("message");
    Boolean success = (Boolean) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>部署管理</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            max-width: 1000px;
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
        }
        
        .form-group label {
            display: inline-block;
            width: 100px;
            font-weight: bold;
        }
        
        .form-group input[type="text"] {
            width: 200px;
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
        .dept-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        .dept-table th, .dept-table td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
        }
        
        .dept-table th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #495057;
        }
        
        .dept-table tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        
        .dept-table tr:hover {
            background-color: #e9ecef;
        }
        
        /* 編集フォーム */
        .edit-row {
            display: none;
        }
        
        .edit-form input[type="text"] {
            width: 150px;
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
    </style>
    <script>
        // 編集モードの切り替え
        function toggleEdit(deptNo) {
            // 表示行を取得
            var displayRow = document.getElementById('display-' + deptNo);
            // 編集行を取得
            var editRow = document.getElementById('edit-' + deptNo);
            
            if (editRow.style.display === 'table-row') {
                // 編集モードから表示モードに戻る
                displayRow.style.display = 'table-row';
                editRow.style.display = 'none';
            } else {
                // 表示モードから編集モードに切り替え
                displayRow.style.display = 'none';
                editRow.style.display = 'table-row';
            }
        }
        
        // 削除確認
        function confirmDelete(deptNo, deptName) {
            if (confirm('部署「' + deptName + '」を削除してもよろしいですか？')) {
                document.getElementById('deleteForm-' + deptNo).submit();
            }
        }
    </script>
</head>
<body>
    <div class="container">
        <h1>部署管理</h1>
        
        <%-- メッセージ表示 --%>
        <% if (message != null && !message.isEmpty()) { %>
            <div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
                <%= message %>
            </div>
        <% } %>
        
        <%-- 新規追加フォーム --%>
        <div class="add-form">
            <h2>新規部署追加</h2>
            <form method="post" action="<%= request.getContextPath() %>/deptManage">
                <input type="hidden" name="action" value="add">
                <div class="form-group">
                    <label for="newDeptNo">部署番号：</label>
                    <input type="text" id="newDeptNo" name="deptNo" maxlength="5" required>
                </div>
                <div class="form-group">
                    <label for="newDeptName">部署名：</label>
                    <input type="text" id="newDeptName" name="deptName" maxlength="50" required>
                </div>
                <button type="submit" class="btn btn-primary">追加</button>
            </form>
        </div>
        
        <%-- 部署一覧テーブル --%>
        <h2>部署一覧</h2>
        <table class="dept-table">
            <thead>
                <tr>
                    <th>部署番号</th>
                    <th>部署名</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% if (deptList != null && !deptList.isEmpty()) { %>
                    <% for (DeptBean dept : deptList) { %>
                        <%-- 表示行 --%>
                        <tr id="display-<%= dept.getDeptNo() %>">
                            <td><%= dept.getDeptNo() %></td>
                            <td><%= dept.getDeptName() %></td>
                            <td>
                                <button class="btn btn-success" onclick="toggleEdit('<%= dept.getDeptNo() %>')">編集</button>
                                <button class="btn btn-danger" onclick="confirmDelete('<%= dept.getDeptNo() %>', '<%= dept.getDeptName() %>')">削除</button>
                                
                                <%-- 削除用フォーム（非表示） --%>
                                <form id="deleteForm-<%= dept.getDeptNo() %>" method="post" 
                                      action="<%= request.getContextPath() %>/deptManage" style="display: none;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="deptNo" value="<%= dept.getDeptNo() %>">
                                </form>
                            </td>
                        </tr>
                        
                        <%-- 編集行（初期状態では非表示） --%>
                        <tr id="edit-<%= dept.getDeptNo() %>" class="edit-row" style="display: none;">
                            <td><%= dept.getDeptNo() %></td>
                            <td colspan="2">
                                <form method="post" action="<%= request.getContextPath() %>/deptManage" style="display: inline;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="deptNo" value="<%= dept.getDeptNo() %>">
                                    <input type="text" name="deptName" value="<%= dept.getDeptName() %>" maxlength="50" required>
                                    <button type="submit" class="btn btn-primary">保存</button>
                                    <button type="button" class="btn btn-secondary" onclick="toggleEdit('<%= dept.getDeptNo() %>')">キャンセル</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                <% } else { %>
                    <tr>
                        <td colspan="3" style="text-align: center;">部署データがありません</td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        
        <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理者メニューへ戻る</a>
    </div>
</body>
</html>