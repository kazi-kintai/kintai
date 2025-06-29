package kintai;

import java.io.IOException;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/leaveRec")
public class LeaveRecServlet extends HttpServlet {

    private final LeaveRecDao dao = new LeaveRecDao();
    private final LeaveBalanceDao balanceDao = new LeaveBalanceDao();
    private final EmpDao empDao = new EmpDao();
    private final DeptDao deptDao = new DeptDao();
    private final PostDao postDao = new PostDao();
    private final LeaveTypeDao typeDao = new LeaveTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        // --- PRG対応：セッションからメッセージを取得してリクエストに渡す ---
        if (session.getAttribute("message") != null) {
            request.setAttribute("message", session.getAttribute("message"));
            request.setAttribute("success", session.getAttribute("success"));
            session.removeAttribute("message");
            session.removeAttribute("success");
        }

        try {
            String deptId = request.getParameter("dept");
            String postId = request.getParameter("post");
            String empId = request.getParameter("empId");

            request.setAttribute("empList", empDao.findByFilters(deptId, postId));
            request.setAttribute("deptList", deptDao.findAll());
            request.setAttribute("postList", postDao.findAll());
            request.setAttribute("leaveTypeList", typeDao.findAll());
            request.setAttribute("selectedDept", deptId);
            request.setAttribute("selectedPost", postId);
            request.setAttribute("selectedEmpId", empId);

            if (empId != null && !empId.isEmpty()) {
                request.setAttribute("balanceList", balanceDao.getLeaveBalances(empId));
                
                // グループ化処理を追加
                Map<String, List<LeaveRecBean>> grouped = new LinkedHashMap<>();
                for (LeaveRecBean rec : dao.getLeaveList(empId)) {
                    String monthLabel = rec.getStartDate().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月"));
                    grouped.computeIfAbsent(monthLabel, k -> new java.util.ArrayList<>()).add(rec);
                }

                request.setAttribute("groupedLeaveList", grouped);
                
            }
        } catch (Exception e) {
            request.setAttribute("message", "初期表示に失敗しました: + print" );
            request.setAttribute("success", false);
        }

        request.getRequestDispatcher("/web/leave_rec_manage.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        String mode = request.getParameter("mode");
        String empId = request.getParameter("empId");
        if (empId == null) empId = "";

        try {
            UserBean loginUser = (UserBean) session.getAttribute("user");

            switch (mode) {
                case "add": {
                    LeaveRecBean addBean = buildBean(request, false);
                    int daysRequested = calcDays(addBean);
                    int remaining = balanceDao.calculateRemainingDays(empId, addBean.getLeaveTypeId());

                    if (daysRequested > remaining) {
                        session.setAttribute("message", "申請日数が残日数を超えています");
                        session.setAttribute("success", false);
                        break;
                    }

                    addBean.setCreatedBy(loginUser.getEmpId());
                    addBean.setUpdatedBy(loginUser.getEmpId());

                    if (dao.insertLeave(addBean)) {
                        balanceDao.consumeLeaveDays(empId, addBean.getLeaveTypeId(), daysRequested);
                        session.setAttribute("message", "休暇申請を登録しました");
                        session.setAttribute("success", true);
                    } else {
                        session.setAttribute("message", "休暇申請の登録に失敗しました");
                        session.setAttribute("success", false);
                    }
                    break;
                }

                case "update": {
                    LeaveRecBean updBean = buildBean(request, true);
                    updBean.setUpdatedBy(loginUser.getEmpId());
                    
                    if (dao.updateLeave(updBean)) {
                    	dao.recalculateUsedDays(updBean.getEmpId(), updBean.getLeaveTypeId());
                        session.setAttribute("message", "休暇申請を更新しました");
                        session.setAttribute("success", true);
                    } else {
                        session.setAttribute("message", "更新に失敗しました");
                        session.setAttribute("success", false);
                    }
                    break;
                }

                case "delete": {
                    int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                    LeaveRecBean deletedLeave = dao.findById(leaveId);

                    if (dao.logicalDeleteLeave(leaveId, loginUser.getEmpId())) {
                        dao.recalculateUsedDays(deletedLeave.getEmpId(), deletedLeave.getLeaveTypeId());
                        session.setAttribute("message", "休暇申請を削除しました");
                        session.setAttribute("success", true);
                        empId = deletedLeave.getEmpId();
                    } else {
                        session.setAttribute("message", "削除に失敗しました");
                        session.setAttribute("success", false);
                    }
                    break;
                }

                default:
                    session.setAttribute("message", "不正な操作が指定されました");
                    session.setAttribute("success", false);
                    break;
            }
        } catch (Exception e) {
            session.setAttribute("message", e.getMessage());
            session.setAttribute("success", false);
        }

        // --- PRG対応：リダイレクトでGETへ遷移（パラメータで状態を維持） ---
        response.sendRedirect(request.getContextPath() + "/leaveRec?empId=" + empId);
    }

    private LeaveRecBean buildBean(HttpServletRequest req, boolean includeId) {
        LeaveRecBean leave = new LeaveRecBean();
        if (includeId) {
            leave.setLeaveId(Integer.parseInt(req.getParameter("leaveId")));
        }
        leave.setEmpId(req.getParameter("empId"));
        leave.setLeaveTypeId(Integer.parseInt(req.getParameter("leaveTypeId")));
        leave.setStartDate(Date.valueOf(req.getParameter("startDate")));
        leave.setEndDate(Date.valueOf(req.getParameter("endDate")));
        leave.setReason(req.getParameter("reason"));
        leave.setApprovedBy(null); // 現時点では未使用
        return leave;
    }

    private int calcDays(LeaveRecBean bean) {
        return (int) (bean.getEndDate().toLocalDate().toEpochDay() - bean.getStartDate().toLocalDate().toEpochDay() + 1);
    }
}
