package kintai;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/leaveGrantManage")
public class LeaveGrantManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private LeaveGrantDao grantDao;
    private EmpDao empDao;

    @Override
    public void init() throws ServletException {
        grantDao = new LeaveGrantDao();
        empDao = new EmpDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String mode = request.getParameter("mode");
        String leaveType = request.getParameter("leaveType");
        if (leaveType == null) leaveType = "annual"; // デフォルト

        LocalDate today = LocalDate.now();
        List<EmpBean> allEmp = empDao.findAllFullTimeEmployees();
        List<EmpBean> unissuedList = new ArrayList<>();

        int unissuedAnnual = 0;
        int unissuedInitial = 0;
        int unissuedSpecial = 0;

        for (EmpBean emp : allEmp) {
            // 年次有給休暇
            boolean notGrantedAnnual = !grantDao.alreadyGranted(emp.getEmpNo(), LocalDate.of(today.getYear(), 7, 1), LeaveGrantDao.LEAVE_TYPE_ANNUAL)
                    && grantDao.isEligible(emp, LocalDate.of(today.getYear(), 7, 1));
            if (notGrantedAnnual) unissuedAnnual++;

            // 初回付与（3ヶ月・6ヶ月）
            LocalDate date3m = emp.getEmpDate().plusMonths(3);
            LocalDate date6m = emp.getEmpDate().plusMonths(6);

            boolean need3m = !grantDao.alreadyGranted(emp.getEmpNo(), date3m, LeaveGrantDao.LEAVE_TYPE_INITIAL_3M)
                    && !today.isBefore(date3m) && grantDao.isEligible(emp, date3m);
            boolean need6m = !grantDao.alreadyGranted(emp.getEmpNo(), date6m, LeaveGrantDao.LEAVE_TYPE_INITIAL_6M)
                    && !today.isBefore(date6m) && grantDao.isEligible(emp, date6m);
            if (need3m || need6m) unissuedInitial++;

            // 特別休暇
            LocalDate specialDate = LocalDate.of(today.getYear(), 7, 1);
            boolean notGrantedSpecial = today.equals(specialDate)
                    && !grantDao.alreadyGranted(emp.getEmpNo(), specialDate, LeaveGrantDao.LEAVE_TYPE_SPECIAL);
            if (notGrantedSpecial) unissuedSpecial++;

            // プレビュー表示対象のみにリスト化
            if ("preview".equals(mode)) {
                switch (leaveType) {
                    case "annual":
                        if (notGrantedAnnual) unissuedList.add(emp);
                        break;
                    case "initial":
                        if (need3m || need6m) unissuedList.add(emp);
                        break;
                    case "special":
                        if (notGrantedSpecial) unissuedList.add(emp);
                        break;
                }
            }
        }

        // JSPに渡す共通データ
        request.setAttribute("unissuedAnnual", unissuedAnnual);
        request.setAttribute("unissuedInitial", unissuedInitial);
        request.setAttribute("unissuedSpecial", unissuedSpecial);
        request.setAttribute("leaveType", leaveType); // プルダウン選択維持

        if ("preview".equals(mode)) {
            request.setAttribute("unissuedList", unissuedList);
            request.setAttribute("mode", "preview");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/leave_grant.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String leaveType = request.getParameter("leaveType");
        LocalDate today = LocalDate.now();
        int grantedCount = 0;

        List<EmpBean> empList = empDao.findAllFullTimeEmployees();

        String source; // 追加：source文字列
        switch (leaveType) {
            case "annual":
                source = "auto";
                break;
            case "initial":
                // initial-3m, initial-6mは後段で分けて指定
                source = "initial";
                break;
            case "special":
                source = "special";
                break;
            default:
                source = "manual";
        }

        for (EmpBean emp : empList) {
            switch (leaveType) {
                case "annual":
                    if (grantDao.grantAnnualLeave(emp, "auto")) grantedCount++;
                    break;
                case "initial":
                    if (grantDao.grantInitialAnnualLeave(emp, 1, "auto")) grantedCount++;
                    if (grantDao.grantInitialAnnualLeave(emp, 2, "auto")) grantedCount++;
                    break;
                case "special":	
                    if (grantDao.grantSpecialLeave(emp, "auto")) grantedCount++;
                    break;
            }
        }

        // メッセージ設定
        if (grantedCount > 0) {
            request.setAttribute("message", grantedCount + "人に「" + leaveType + "」休暇を付与しました。");
            request.setAttribute("success", true);
        } else {
            request.setAttribute("message", "該当する従業員がいなかったか、すでに付与済みです。");
            request.setAttribute("success", false);
        }

        request.setAttribute("grantedCount", grantedCount);
        request.setAttribute("mode", "execute");
        request.setAttribute("leaveType", leaveType);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/leave_grant.jsp");
        dispatcher.forward(request, response);
    }
}