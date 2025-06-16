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
            if (confirm('社員「' + empName + '」を削除してもよろしいです