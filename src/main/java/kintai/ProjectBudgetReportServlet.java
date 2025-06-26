package kintai;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ProjectBudgetReportServlet")
public class ProjectBudgetReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProjectDao projectDao = new ProjectDao();
    private ProjectBudgetReportDao budgetReportDao = new ProjectBudgetReportDao();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // セッションチェック
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }
        
        // 管理者権限チェック
        UserBean user = (UserBean) session.getAttribute("user");
        if (user.getRoleId() != 1) {
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("getProjectMembers".equals(action)) {
            getProjectMembers(request, response);
        } else {
            // プロジェクト一覧を取得
            List<ProjectBean> projectList = projectDao.findAll();
            request.setAttribute("projectList", projectList);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/web/project_budget_report.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    private void getProjectMembers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String projectIdStr = request.getParameter("projectId");
        String month = request.getParameter("month");
        
        if (projectIdStr == null || month == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "パラメータが不足しています");
            return;
        }
        
        try {
            int projectId = Integer.parseInt(projectIdStr);
            
            // プロジェクト情報を取得
            ProjectBean project = projectDao.findById(projectId);
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "プロジェクトが見つかりません");
                return;
            }
            
            // プロジェクトメンバーの実績データを取得
            List<ProjectMemberReportBean> memberReports = budgetReportDao.getProjectMemberReports(projectId, month);
            
            // 総集計を計算
            BigDecimal totalActual = BigDecimal.ZERO;
            for (ProjectMemberReportBean member : memberReports) {
                if (member.getActualAmount() != null) {
                    totalActual = totalActual.add(member.getActualAmount());
                }
            }
            
            // 予算実績差異を計算
            BigDecimal budgetVariance = null;
            if (project.getBudget() != null && project.getBudget() > 0) {
                budgetVariance = new BigDecimal(project.getBudget()).subtract(totalActual);
            }
            
            request.setAttribute("project", project);
            request.setAttribute("memberReports", memberReports);
            request.setAttribute("totalActual", totalActual);
            request.setAttribute("budgetVariance", budgetVariance);
            request.setAttribute("selectedMonth", month);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/web/project_member_detail.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なプロジェクトIDです");
        }
    }
}