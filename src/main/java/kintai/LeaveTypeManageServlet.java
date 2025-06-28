package kintai;

import java.io.IOException;
import java.util.Arrays;
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

    // 編集・削除不可ID
    private static final List<Integer> FIXED_IDS = Arrays.asList(1, 2, 3, 11, 12);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<LeaveTypeBean> leaveTypeList = leaveTypeDao.findAll();
        request.setAttribute("leaveTypeList", leaveTypeList);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/leave_manage.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String message = "";
        boolean success = false;

        try {
            switch (action) {
                case "add": {
                    int id = Integer.parseInt(request.getParameter("leaveTypeId"));
                    String name = request.getParameter("leaveTypeName");
                    boolean isPaid = "true".equals(request.getParameter("isPaid"));

                    if (name == null || name.trim().isEmpty()) {
                        message = "休暇種別名は必須です";
                        break;
                    }
                    if (leaveTypeDao.exists(id)) {
                        message = "休暇種別ID「" + id + "」は既に存在します";
                        break;
                    }

                    LeaveTypeBean newBean = new LeaveTypeBean(id, name, isPaid);
                    success = leaveTypeDao.insert(newBean);
                    message = success ? "休暇種別を追加しました" : "休暇種別の追加に失敗しました";
                    break;
                }

                case "update": {
                    int originalId = Integer.parseInt(request.getParameter("originalLeaveTypeId"));
                    int newId = Integer.parseInt(request.getParameter("leaveTypeId"));
                    String name = request.getParameter("leaveTypeName");
                    boolean isPaid = "true".equals(request.getParameter("isPaid"));

                    if (FIXED_IDS.contains(originalId)) {
                        message = "この休暇種別は編集できません（ID: " + originalId + "）";
                        break;
                    }

                    if (name == null || name.trim().isEmpty()) {
                        message = "休暇種別名は必須です";
                        break;
                    }

                    LeaveTypeBean updatedBean = new LeaveTypeBean(newId, name, isPaid);
                    success = leaveTypeDao.update(originalId, updatedBean);
                    message = success ? "休暇種別を更新しました" : "休暇種別の更新に失敗しました";
                    break;
                }

                case "delete": {
                    int deleteId = Integer.parseInt(request.getParameter("leaveTypeId"));
                    if (FIXED_IDS.contains(deleteId)) {
                        message = "この休暇種別は削除できません（ID: " + deleteId + "）";
                        break;
                    }

                    success = leaveTypeDao.delete(deleteId);
                    message = success ? "休暇種別を削除しました" : "休暇種別の削除に失敗しました";
                    break;
                }

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
