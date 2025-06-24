<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>休暇申請管理</title>
</head>
<body>

<!--  ログアウト -->
<form action="LogoutServlet" method="post" style="text-align:right;">
  <button type="submit">ログアウト</button>
</form>

<h2>休暇申請管理画面</h2>

<!--  検索フォーム（社員番号・部署） -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="search" />
  <label>従業員番号:
    <select name="empNo">
      <option value="E001"></option>
      
    </select>
  </label>
  <label>部署:
    <select name="dept">
      <option value="HR">人事部</option> 　修正
    </select>
  </label>
  <button type="submit">検索</button>
</form>

<hr/>

<!-- 🔹 残日数表示 -->
<p>残有給休暇: ${remainingPaidLeave} 日</p>
<p>残特別休暇: ${remainingSpecialLeave} 日</p>
<p>残代休: ${remainingCompLeave} 日</p>

<!-- 🔹 新規休暇申請フォーム -->
<form action="LeaveRecServlet" method="post">
  <input type="hidden" name="mode" value="add" />
  <label>社員番号:
    <select name="empNo">
      <option value="E001">E001 - 山田太郎</option>
      <option value="E002">E002 - 佐藤花子</option>
    </select>
  </label>
  <label>開始日:
    <input type="date" name="startDate" required />
  </label>
  <label>終了日:
    <input type="date" name="endDate" required />
  </label>
  <label>休日種別:
    <select name="leaveTypeId">
      <option value="1">有給休暇</option>
      <option value="2">代休</option>
      <option value="3">特別休暇</option>
    </select>
  </label>
  <label>理由:
    <input type="text" name="reason" />
  </label>
  <label>承認者:
    <input type="text" name="approvedBy" />
  </label>
  <button type="submit">追加</button>
</form>

<hr/>

<!-- 🔹 申請履歴（編集・削除可能） -->
<h3>休暇申請一覧</h3>
<table border="1">
  <tr>
    <th>開始日</th>
    <th>終了日</th>
    <th>休日種別</th>
    <th>理由</th>
    <th>承認者</th>
    <th>操作</th>
  </tr>

  <c:forEach var="leave" items="${leaveList}">
    <tr>
      <!-- 編集フォーム -->
      <form action="LeaveRecServlet" method="post">
        <input type="hidden" name="mode" value="update" />
        <input type="hidden" name="leaveId" value="${leave.leaveId}" />
        <input type="hidden" name="empNo" value="${leave.empNo}" />
        <td><input type="date" name="startDate" value="${leave.startDate}" /></td>
        <td><input type="date" name="endDate" value="${leave.endDate}" /></td>
        <td>
          <select name="leaveTypeId">
            <option value="1" ${leave.leaveTypeId == 1 ? "selected" : ""}>有給休暇</option>
            <option value="2" ${leave.leaveTypeId == 2 ? "selected" : ""}>代休</option>
            <option value="3" ${leave.leaveTypeId == 3 ? "selected" : ""}>特別休暇</option>
          </select>
        </td>
        <td><input type="text" name="reason" value="${leave.reason}" /></td>
        <td><input type="text" name="approvedBy" value="${leave.approvedBy}" /></td>
        <td>
          <button type="submit">保存</button>
      </form>

      <!-- 削除フォーム -->
      <form action="LeaveRecServlet" method="post" style="display:inline;">
        <input type="hidden" name="mode" value="delete" />
        <input type="hidden" name="leaveId" value="${leave.leaveId}" />
        <input type="hidden" name="empNo" value="${leave.empNo}" />
        <button type="submit" onclick="return confirm('削除しますか？')">削除</button>
      </form>
      </td>
    </tr>
  </c:forEach>
</table>

<!-- 🔹 管理メニューへ戻る -->
<br/>
<form action="修正が必要です" method="get">
  <button type="submit">管理部基本メニューへ戻る</button>
</form>

</body>
</html>