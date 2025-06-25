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

    private LeaveRecDAO dao = new LeaveRecDAO();
    private EmpDao empDao = new EmpDao(); // 社員リスト取得用
    private DeptDao deptDao = new DeptDao(); // 部署リスト取得用
    private LeaveTypeDao typeDao = new LeaveTypeDao(); // 休暇種別取得用

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 初期表示：全社員・部署・休暇種別リストの準備
        try {
            List<EmpBean> empList = empDao.findAll(); // 全社員
            List<DeptBean> deptList = deptDao.findAll(); // 部署名一覧
            List<LeaveTypeBean> leaveTypeList = typeDao.findAll(); // 休暇種別

            request.setAttribute("empList", empList);
            request.setAttribute("deptList", deptList);
            request.setAttribute("leaveTypeList", leaveTypeList);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "初期表示時にエラーが発生しました: " + e.getMessage());
        }

        // JSPへフォワード
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
                LeaveRequest bean = buildBean(request, false);
                dao.insertLeave(bean);
                request.setAttribute("message", "休暇申請を登録しました。");

            } else if ("update".equals(mode)) {
                LeaveRequest bean = buildBean(request, true);
                dao.updateLeave(bean);
                request.setAttribute("message", "休暇申請を更新しました。");

            } else if ("delete".equals(mode)) {
                int id = Integer.parseInt(request.getParameter("leaveId"));
                dao.deleteLeave(id);
                request.setAttribute("message", "休暇申請を削除しました。");

            } else if ("search".equals(mode)) {
                request.setAttribute("selectedEmpNo", empNo);
                request.setAttribute("selectedDept", request.getParameter("dept"));
            }

            // 休暇残日数の再取得
            int paid = dao.fetchTotalPaidLeave(empNo);
            int special = dao.fetchTotalSpecialLeave(empNo);
            int comp = dao.fetchTotalCompLeave(empNo);

            request.setAttribute("remainingPaidLeave", paid); // ここで used 計算も可
            request.setAttribute("remainingSpecialLeave", special);
            request.setAttribute("remainingCompLeave", comp);

            // 検索対象者の休暇申請一覧
            List<LeaveRequest> leaveList = dao.getLeaveList(empNo);
            request.setAttribute("leaveList", leaveList);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "処理中にエラーが発生しました: " + e.getMessage());
        }

        // 画面表示に必要な共通情報（初期表示の情報）を再設定
        doGet(request, response);
    }

    private LeaveRequest buildBean(HttpServletRequest req, boolean includeId) {
        LeaveRequest leave = new LeaveRequest();
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
}