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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String mode = request.getParameter("mode");
        String empNo = request.getParameter("empNo");

        try {
            // 操作別処理
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
                // 検索時に渡されたempNoをそのまま扱う（"all"なら一覧取得スキップも可）
                request.setAttribute("selectedEmpNo", empNo);
                request.setAttribute("selectedDept", request.getParameter("dept"));
                // 氏名取得処理など追加するならここ
            }

            // 有給・特別・代休 すべての残数を算出
            int paid = dao.fetchTotalPaidLeave(empNo);
            int used = dao.countUsedPaidLeave(empNo);
            int special = dao.fetchTotalSpecialLeave(empNo);
            int comp = dao.fetchTotalCompLeave(empNo); // ← 実装していない場合は省略可

            request.setAttribute("totalPaidLeave", paid);
            request.setAttribute("usedPaidLeave", used);
            request.setAttribute("remainingPaidLeave", paid - used);
            request.setAttribute("remainingSpecialLeave", special);
            request.setAttribute("remainingCompLeave", comp);

            // 申請一覧取得
            List<LeaveRequest> leaveList = dao.getLeaveList(empNo);
            request.setAttribute("leaveList", leaveList);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "処理中にエラーが発生しました: " + e.getMessage());
        }

        request.getRequestDispatcher("leave_main.jsp").forward(request, response);
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