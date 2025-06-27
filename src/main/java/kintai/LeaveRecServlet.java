package kintai;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/leaveRec")
public class LeaveRecServlet extends HttpServlet {

    private final LeaveRecDao dao = new LeaveRecDao(); // 申請処理
    private final LeaveBalanceDao balanceDao = new LeaveBalanceDao(); // 残日数・期限一覧
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

        try {
            // 部署・役職パラメータを取得（検索フォームから）
            String deptId = request.getParameter("dept");
            String postId = request.getParameter("post");

            // 部署・役職でフィルターした社員リスト取得
            List<EmpBean> empList = empDao.findByFilters(deptId, postId);

            // 共通属性セット（部署一覧、役職一覧、休暇種別一覧）
            request.setAttribute("empList", empList);
            request.setAttribute("deptList", deptDao.findAll());
            request.setAttribute("postList", postDao.findAll());
            request.setAttribute("leaveTypeList", typeDao.findAll());

            // 選択された部署・役職をJSPへ保持
            request.setAttribute("selectedDept", deptId);
            request.setAttribute("selectedPost", postId);

            // 従業員選択
            String empNo = (String) request.getAttribute("selectedEmpNo");
            if (empNo == null) empNo = "";

            if (!empNo.isEmpty()) {
                request.setAttribute("remainingPaidLeave", dao.fetchRemainingLeave(empNo, LeaveRecDao.LEAVE_TYPE_PAID));
                request.setAttribute("remainingSpecialLeave", dao.fetchRemainingLeave(empNo, LeaveRecDao.LEAVE_TYPE_SPECIAL));
                request.setAttribute("remainingCompLeave", dao.fetchRemainingLeave(empNo, LeaveRecDao.LEAVE_TYPE_COMP));

                List<LeaveBalanceBean> balanceList = balanceDao.getLeaveBalances(empNo);
                request.setAttribute("balanceList", balanceList);

                List<LeaveRecBean> leaveList = dao.getLeaveList(empNo);
                request.setAttribute("leaveList", leaveList);
            } else {
                // 初期表示用
                request.setAttribute("remainingPaidLeave", 0);
                request.setAttribute("remainingSpecialLeave", 0);
                request.setAttribute("remainingCompLeave", 0);
                request.setAttribute("balanceList", null);
                request.setAttribute("leaveList", null);
            }

        } catch (Exception e) {
            request.setAttribute("errorMessage", "初期表示に失敗しました: " + e.getMessage());
        }

        request.getRequestDispatcher("/web/leave_rec.jsp").forward(request, response);
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
        String empNo = request.getParameter("empNo");
        if (empNo == null) empNo = "";

        try {
            switch (mode) {
                case "add":
                    LeaveRecBean addBean = buildBean(request, false);
                    UserBean addUser = (UserBean) session.getAttribute("user");
                    addBean.setCreatedBy(addUser.getEmpId());
                    addBean.setUpdatedBy(addUser.getEmpId());
                    boolean inserted = dao.insertLeave(addBean);
                    request.setAttribute(inserted ? "message" : "errorMessage",
                            inserted ? "休暇申請を登録しました。" : "休暇申請の登録に失敗しました。");
                    break;

                case "update":
                    LeaveRecBean updBean = buildBean(request, true);
                    UserBean updUser = (UserBean) session.getAttribute("user");
                    updBean.setUpdatedBy(updUser.getEmpId());
                    boolean updated = dao.updateLeave(updBean);
                    request.setAttribute(updated ? "message" : "errorMessage",
                            updated ? "休暇申請を更新しました。" : "休暇申請の更新に失敗しました。");
                    break;

                case "delete":
                    int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                    UserBean delUser = (UserBean) session.getAttribute("user");
                    boolean deleted = dao.logicalDeleteLeave(leaveId, delUser.getEmpId());
                    request.setAttribute(deleted ? "message" : "errorMessage",
                            deleted ? "休暇申請を削除しました。" : "休暇申請の削除に失敗しました。");
                    break;

                case "search":
                    request.setAttribute("selectedEmpNo", empNo);
                    request.setAttribute("selectedDept", request.getParameter("dept"));
                    break;

                default:
                    request.setAttribute("errorMessage", "不正な操作が指定されました。");
                    break;
            }

        } catch (Exception e) {
            request.setAttribute("errorMessage", "処理中にエラーが発生しました: " + e.getMessage());
        }

        doGet(request, response); // すべての最新情報を再取得・表示
    }

    private LeaveRecBean buildBean(HttpServletRequest req, boolean includeId) {
        LeaveRecBean leave = new LeaveRecBean();
        if (includeId) {
            leave.setLeaveId(Integer.parseInt(req.getParameter("leaveId")));
        }
        leave.setEmpNo(req.getParameter("empNo"));
        leave.setLeaveTypeId(Integer.parseInt(req.getParameter("leaveTypeId")));
        leave.setStartDate(Date.valueOf(req.getParameter("startDate")));
        leave.setEndDate(Date.valueOf(req.getParameter("endDate")));
        leave.setReason(req.getParameter("reason"));
        leave.setApprovedBy(null); // 承認処理は未実装
        return leave;
    }

    private void setCommonAttributes(HttpServletRequest request) throws Exception {
        request.setAttribute("empList", empDao.findAll());
        request.setAttribute("deptList", deptDao.findAll());
        request.setAttribute("leaveTypeList", typeDao.findAll());
    }
}
