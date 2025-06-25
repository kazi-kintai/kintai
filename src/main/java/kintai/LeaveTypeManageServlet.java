package kintai;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 休暇種別管理機能のサーブレット
 */
@WebServlet("/leaveTypeManage")
public class LeaveTypeManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private LeaveTypeDao leaveTypeDao = new LeaveTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // セッションと権限チェックは必要に応じて追加してください

        List<LeaveTypeBean> leaveTypeList = leaveTypeDao.findAll();
        request.setAttribute("leaveTypeList", leaveTypeList);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/leave_manage.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // セッションと権限チェックは必要に応じて追加してください

        String action = request.getParameter("action");
        String message = "";
        boolean success = false;

        try {
            switch (action) {
                case "add":
                    int addId = Integer.parseInt(request.getParameter("leaveTypeId"));
                    String addName = request.getParameter("leaveTypeName");
                    boolean addIsPaid = request.getParameter("isPaid") != null;
                    if (addName == null || addName.trim().isEmpty()) {
                        message = "休暇種別名は必須です";
                        break;
                    }
                    if (leaveTypeDao.exists(addId)) {
                        message = "休暇種別ID「" + addId + "」は既に存在します";
                        break;
                    }

                    LeaveTypeBean addBean = new LeaveTypeBean(addId, addName, addIsPaid);
                    success = leaveTypeDao.insert(addBean);
                    message = success ? "休暇種別を追加しました" : "休暇種別の追加に失敗しました";
                    break;

                case "update":
                    int updateId = Integer.parseInt(request.getParameter("leaveTypeId"));
                    String updateName = request.getParameter("leaveTypeName");
                    boolean updateIsPaid = "true".equals(request.getParameter("isPaid"));

                    if (updateName == null || updateName.trim().isEmpty()) {
                        message = "休暇種別名は必須です";
                        break;
                    }

                    LeaveTypeBean updateBean = new LeaveTypeBean(updateId, updateName, updateIsPaid);
                    success = leaveTypeDao.update(updateBean);
                    message = success ? "休暇種別を更新しました" : "休暇種別の更新に失敗しました";
                    break;

                case "delete":
                    int deleteId = Integer.parseInt(request.getParameter("leaveTypeId"));
                    success = leaveTypeDao.delete(deleteId);
                    message = success ? "休暇種別を削除しました" : "休暇種別の削除に失敗しました";
                    break;

                default:
                    message = "不正な操作です";
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "処理中にエラーが発生しました";
        }

        request.setAttribute("message", message);
        request.setAttribute("success", success);

        doGet(request, response);
    }
}
