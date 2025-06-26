package kintai;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/ProjectManageServlet")
public class ProjectManageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private ProjectDao ProjectDao = new ProjectDao();
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse 
			response) throws ServletException, IOException {
		
		List<ProjectBean> projectmanagelist = ProjectDao.findAll();
		request.setAttribute("projectmanagelist", projectmanagelist);
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/web/projectmanage.jsp");
        dispatcher.forward(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse 
			response) throws ServletException, IOException {
		
		 // セッションチェック
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }
        
        // 管理者権限チェック (UserBeanのgetRole()からgetRoleId()へ変更)
        UserBean user = (UserBean) session.getAttribute("user");
        if (user.getRoleId() != 1) { // ROLEIDが1が管理者
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            return;
        }
        
        // アクションを取得
        String action = request.getParameter("action");
        
        boolean success = false;
        String message = "";
        
        try {
            switch (action) {
                case "add":
                    // 新規追加処理
                    String newProjectName = request.getParameter("ProjectName");
                    String newStartDate = request.getParameter("StartDate");
                    String newEndDate = request.getParameter("EndDate");
                    String newBudget = request.getParameter("Budget");
                  
                    // 入力チェック (最低限のチェック、詳細なビジネスロジックはDAOやサービス層で)
                    if (newProjectName == null || newProjectName.trim().isEmpty() ||
                        newStartDate == null || newStartDate.trim().isEmpty() ||
                        newEndDate == null || newEndDate.trim().isEmpty() ||
                        newBudget == null || newBudget.trim().isEmpty()) {
//                        newRoleIdStr == null || newRoleIdStr.trim().isEmpty() ||
//                        newGradeNoStr == null || newGradeNoStr.trim().isEmpty() ||
//                        newPass == null || newPass.trim().isEmpty()
                        message = "必須項目をすべて入力してください";
                        break;
                    }

                    // PROJECT_IDは自動生成されるため重複チェック不要
                    
                   
//                    java.util.LocalDate newStartdateDate = null;
//                    java.util.LocalDate newEnddateDate = null;
//              
////                        String dateStr = request.getParameter("date");  // 例: "2025-06-24"
//                        try {
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                            newStartdateDate = sdf.parse(newStartDate);
//                            newEnddateDate = sdf.parse(newEndDate);
//                            // 処理例: データベースに登録など
//                            // PreparedStatementで setDate(new java.sql.Date(date.getTime())) として渡す
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            // エラー処理
//                        }
//             
                    
                    ProjectBean newProject = new ProjectBean();
                    // PROJECT_IDは自動生成されるため設定しない
                    newProject.setProjectName(newProjectName);
//                    newProject.setStartDate(newStartdateDate);
//                    newProject.setEndDate(newEnddateDate);
//                    newEmp.setRoleId(Integer.parseInt(newRoleIdStr)); // 旧setRoleから変更
//                    newEmp.setGradeNo(Integer.parseInt(newGradeNoStr)); // 新規追加
//                    newEmp.setPass(newPass);
//                    newEmp.setMail(newMail); // 新規追加
                    // EMPDATEはnull許容として、JSPからの入力がない場合はnull
//                    if (newEmpDateStr != null && !newEmpDateStr.trim().isEmpty()) {
//                        newEmp.setEmpDate(java.time.LocalDate.parse(newEmpDateStr));
//                    }
                    
                    // EMPDATEはnull許容として、JSPからの入力がない場合はnull
                    if (newStartDate != null && !newStartDate.trim().isEmpty()) {
                        newProject.setStartDate(java.time.LocalDate.parse(newStartDate));
                    }
                    
                    
                    if (newEndDate != null && !newEndDate.trim().isEmpty()) {
                        newProject.setEndDate(java.time.LocalDate.parse(newEndDate));
                    }
                    
                    newProject.setBudget(Integer.parseInt(newBudget));
                    
                    success = ProjectDao.insert(newProject);
                    message = success ? "プロジェクトを追加しました" : "プロジェクトの追加に失敗しました";
                    break;
                    
                case "update":
                    // 更新処理
                    String updateProjectId = request.getParameter("ProjectId");
                    String updateProjectName = request.getParameter("ProjectName");
                    String updateStartDate = request.getParameter("StartDate");
                    String updateEndDate = request.getParameter("EndDate");
                    String updateBudget = request.getParameter("Budget");
//                    String updateRoleIdStr = request.getParameter("roleId"); // 旧roleから変更
//                    String updateGradeNoStr = request.getParameter("gradeNo"); // 新規追加
//                    String updatePass = request.getParameter("pass"); // パスワードは更新時も入力させる想定
//                    String updateMail = request.getParameter("mail"); // 新規追加
//                    String updateEmpDateStr = request.getParameter("empDate"); // 新規追加
                    
                    // 入力チェック
                    if (updateProjectName == null || updateProjectName.trim().isEmpty() ||
                        updateStartDate == null || updateStartDate.trim().isEmpty() ||
                        updateEndDate == null || updateEndDate.trim().isEmpty() ||
                        updateBudget == null || updateBudget.trim().isEmpty()) {
//                        updateRoleIdStr == null || updateRoleIdStr.trim().isEmpty() ||
//                        updateGradeNoStr == null || updateGradeNoStr.trim().isEmpty() ||
//                        updatePass == null || updatePass.trim().isEmpty()
                        // パスワードも必須
                        message = "必須項目をすべて入力してください";
                        break;
                    }
                    
                    // 更新対象のプロジェクトが存在するかチェック
                    if (!ProjectDao.exists(Integer.parseInt(updateProjectId))) {
                        message = "プロジェクト：「" + updateProjectId + "」は存在しません";
                        break;
                    }
                    
                    java.util.Date updateStartdateDate = null;
                    java.util.Date updateEnddateDate = null;
              
//                        String dateStr = request.getParameter("date");  // 例: "2025-06-24"
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            updateStartdateDate = sdf.parse(updateStartDate);
                            updateEnddateDate = sdf.parse(updateEndDate);
                            // 処理例: データベースに登録など
                            // PreparedStatementで setDate(new java.sql.Date(date.getTime())) として渡す
                        } catch (ParseException e) {
                            e.printStackTrace();
                            // エラー処理
                        }
                    
                    ProjectBean updateProject = new ProjectBean();
                    updateProject.setProjectId(Integer.parseInt(updateProjectId));
                    updateProject.setProjectName(updateProjectName);
//                    updateProject.setStartDate(updateStartdateDate);
//                    updateProject.setEndDate(updateEnddateDate);
//                    updateEmp.setRoleId(Integer.parseInt(updateRoleIdStr)); // 旧setRoleから変更
//                    updateEmp.setGradeNo(Integer.parseInt(updateGradeNoStr)); // 新規追加
//                    updateEmp.setPass(updatePass);
//                    updateEmp.setMail(updateMail); // 新規追加
//                    if (updateEmpDateStr != null && !updateEmpDateStr.trim().isEmpty()) {
//                        updateEmp.setEmpDate(java.time.LocalDate.parse(updateEmpDateStr));
//                    } else {
//                        updateEmp.setEmpDate(null); // 入力がない場合はnull
//                    }
                    
                 // EMPDATEはnull許容として、JSPからの入力がない場合はnull
                    if (updateStartDate != null && !updateStartDate.trim().isEmpty()) {
                        updateProject.setStartDate(java.time.LocalDate.parse(updateStartDate));
                    }
                    
                    
                    if (updateEndDate != null && !updateEndDate.trim().isEmpty()) {
                        updateProject.setEndDate(java.time.LocalDate.parse(updateEndDate));
                    }
                    
                    updateProject.setBudget(Integer.parseInt(updateBudget));
                    
                    success = ProjectDao.update(updateProject);
                    message = success ? "プロジェクト名を更新しました" : "プロジェクト情報の更新に失敗しました";
                    break;
                    
                case "delete":
                    // 削除処理
                    String deleteProjectId = request.getParameter("ProjectId");
                    success = ProjectDao.delete(deleteProjectId);
                    
                    if (success) {
                        message = "プロジェクトを削除しました";
                    } else {
                        message = "プロジェクトの削除に失敗しました。この従業員に関連するデータが存在する可能性があります。"; // メッセージを一般化
                    }
                    break;
                    
                default:
                    message = "不正な操作です";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            message = "入力された数値（ロールID、等級番号）が不正です";
        } catch (java.time.format.DateTimeParseException e) {
            e.printStackTrace();
            message = "入社年月日の形式が不正です。YYYY-MM-DD形式で入力してください。";
        } catch (Exception e) {
            e.printStackTrace();
            message = "処理中にエラーが発生しました";
        }
        
        // 処理結果をリクエスト属性に設定
        request.setAttribute("message", message);
        request.setAttribute("success", success);
        
		doGet(request, response);
	}
	
	

}
