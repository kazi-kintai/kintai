<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.ProjectBean" %>
<%@ page import="java.text.NumberFormat" %>

<%
    List<ProjectBean> projectmanagelist = (List<ProjectBean>) request.getAttribute("projectmanagelist");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>プロジェクト一覧</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            max-width: 900px;
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
            margin-top: 0;
        }
        
        /* テーブルスタイル */
        .project-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            background-color: white;
        }
        
        .project-table th, .project-table td {
            border: 1px solid #dee2e6;
            padding: 10px 8px;
            text-align: center;
            vertical-align: middle;
            font-size: 12px;
        }
        
        .project-table th {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            font-weight: 600;
            color: #495057;
            border-bottom: 2px solid #dee2e6;
        }
        
        .project-table tr:nth-child(even) {
            background-color: rgba(0,123,255,0.02);
        }
        
        .project-table tr:hover {
            background-color: rgba(0,123,255,0.05);
            transition: background-color 0.2s;
        }
        
        .close-button {
            margin-top: 20px;
            text-align: center;
        }
        
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            margin-right: 10px;
            transition: all 0.2s;
        }
        
        .btn-secondary {
            background: linear-gradient(135deg, #6c757d 0%, #545b62 100%);
            color: white;
            box-shadow: 0 2px 4px rgba(108,117,125,0.2);
        }
        
        .btn-secondary:hover {
            background: linear-gradient(135deg, #545b62 0%, #495057 100%);
            transform: translateY(-1px);
            box-shadow: 0 3px 6px rgba(108,117,125,0.3);
        }
    </style>
    <script>
        function closeWindow() {
            window.close();
        }
    </script>
</head>
<body>
    <div class="container">
        <h1>プロジェクト一覧</h1>
        
        <table class="project-table">
            <thead>
                <tr>
                    <th>プロジェクトID</th>
                    <th>プロジェクト名</th>
                    <th>予算</th>
                    <th>開始日</th>
                    <th>終了日</th>
                </tr>
            </thead>
            <tbody>
                <% if (projectmanagelist != null && !projectmanagelist.isEmpty()) { %>
                    <% 
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(true); // 3桁区切りを有効にする
                    %>
                    <% for (ProjectBean project : projectmanagelist) { %>
                        <tr>
                            <td><%= project.getProjectId() %></td>
                            <td><%= (project.getProjectName() != null) ? project.getProjectName() : "情報なし" %></td>
                            <td style="text-align: right;">
                                <%
                                    int budget = project.getBudgetAmount();
                                    out.print(budget != 0 ? nf.format(budget) + "円" : "情報なし");
                                %>
                            </td>
                            <td><%= (project.getStartDate() != null) ? project.getStartDate() : "---" %></td>
                            <td><%= (project.getEndDate() != null) ? project.getEndDate(): "---" %></td>
                        </tr>
                    <% } %>
                <% } else { %>
                    <tr>
                        <td colspan="5" style="text-align: center;">プロジェクトデータがありません</td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        
        <div class="close-button">
            <button class="btn btn-secondary" onclick="closeWindow()">閉じる</button>
        </div>
    </div>
</body>
</html>