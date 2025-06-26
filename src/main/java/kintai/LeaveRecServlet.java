package kintai;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/LeaveRecServlet")
public class LeaveRecServlet extends HttpServlet {

    private final LeaveRecDAO dao = new LeaveRecDAO();
    private final EmpDao empDao = new EmpDao();       // 社員リスト取得用
    private final DeptDao deptDao = new DeptDao();     // 部署リスト取得用
    private final LeaveTypeDao typeDao = new LeaveTypeDao(); // 休暇種別取得用

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            setCommonAttributes(request);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "初期表示時にエラーが発生しました: " + e.getMessage());
        }
        request.getRequestDispatcher("/web/leave_main.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String mode = request.getParameter("mode");
        String empNo = request.getParameter("empNo");

        try {
            if ("add".equals(mode)) {
                LeaveRecBean bean = buildBean(request, false);
                dao.insertLeave(bean);
                request.setAttribute("message", "休暇申請を登録しました。");

            } else if ("update".equals(mode)) {
                LeaveRecBean bean = buildBean(request, true);
                dao.updateLeave(bean);
                request.setAttribute("message", "休暇申請を更新しました。");

            } else if ("delete".equals(mode)) {
                int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                dao.deleteLeave(leaveId);
                request.setAttribute("message", "休暇申請を削除しました。");

            } else if ("search".equals(mode)) {
                request.setAttribute("selectedEmpNo", empNo);
                request.setAttribute("selectedDept", request.getParameter("dept"));
            }

            // 残日数を再取得
            int paid = dao.fetchTotalPaidLeave(empNo);
            int special = dao.fetchTotalSpecialLeave(empNo);
            int comp = dao.fetchTotalCompLeave(empNo);

            request.setAttribute("remainingPaidLeave", paid);
            request.setAttribute("remainingSpecialLeave", special);
            request.setAttribute("remainingCompLeave", comp);

            // 休暇申請一覧
            List<LeaveRecBean> leaveList = dao.getLeaveList(empNo);
            request.setAttribute("leaveList", leaveList);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "処理中にエラーが発生しました: " + e.getMessage());
        }

        doGet(request, response); // 共通情報を再設定
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
        leave.setApprovedBy(req.getParameter("approvedBy"));
        return leave;
    }

    private void setCommonAttributes(HttpServletRequest request) throws Exception {
        request.setAttribute("empList", empDao.findAll());
        request.setAttribute("deptList", deptDao.findAll());
        request.setAttribute("leaveTypeList", typeDao.findAll());
    }
}