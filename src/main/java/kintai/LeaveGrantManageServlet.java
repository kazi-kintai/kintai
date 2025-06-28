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
import jakarta.servlet.http.HttpSession;

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
        if (leaveType == null) leaveType = "annual";

        LocalDate today = LocalDate.now();
        List<EmpBean> allEmp = empDao.findAllFullTimeEmployees();
        List<EmpBean> unissuedList = new ArrayList<>();

        int unissuedAnnual = 0;
        int unissuedInitial = 0;
        int unissuedSpecial = 0;

        for (EmpBean emp : allEmp) {
            boolean notGrantedAnnual = !grantDao.alreadyGranted(emp.getEmpId(), LocalDate.of(today.getYear(), 7, 1), LeaveGrantDao.LEAVE_TYPE_ANNUAL)
                    && grantDao.isEligible(emp, LocalDate.of(today.getYear(), 7, 1));
            if (notGrantedAnnual) unissuedAnnual++;

            LocalDate date3m = emp.getEmpDate().plusMonths(3);
            LocalDate date6m = emp.getEmpDate().plusMonths(6);

            boolean need3m = !grantDao.alreadyGranted(emp.getEmpId(), date3m, LeaveGrantDao.LEAVE_TYPE_INITIAL_3M)
                    && !today.isBefore(date3m) && grantDao.isEligible(emp, date3m);
            boolean need6m = !grantDao.alreadyGranted(emp.getEmpId(), date6m, LeaveGrantDao.LEAVE_TYPE_INITIAL_6M)
                    && !today.isBefore(date6m) && grantDao.isEligible(emp, date6m);
            if (need3m || need6m) unissuedInitial++;

            LocalDate specialDate = LocalDate.of(today.getYear(), 7, 1);
            boolean notGrantedSpecial = (today.isEqual(specialDate) || today.isAfter(specialDate))
                    && !grantDao.alreadyGranted(emp.getEmpId(), specialDate, LeaveGrantDao.LEAVE_TYPE_SPECIAL);
            if (notGrantedSpecial) unissuedSpecial++;

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

        request.setAttribute("unissuedAnnual", unissuedAnnual);
        request.setAttribute("unissuedInitial", unissuedInitial);
        request.setAttribute("unissuedSpecial", unissuedSpecial);
        request.setAttribute("leaveType", leaveType);

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

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");
        String loginUser = (user != null) ? user.getEmpId() : "system";

        List<EmpBean> empList = empDao.findAllFullTimeEmployees();

        for (EmpBean emp : empList) {
            switch (leaveType) {
                case "annual":
                    if (grantDao.grantAnnualLeave(emp, loginUser)) grantedCount++;
                    break;
                case "initial":
                    if (grantDao.grantInitialAnnualLeave(emp, 1, loginUser)) grantedCount++;
                    if (grantDao.grantInitialAnnualLeave(emp, 2, loginUser)) grantedCount++;
                    break;
                case "special":
                    LocalDate grantDate = LocalDate.of(today.getYear(), 7, 1);
                    if ((today.isEqual(grantDate) || today.isAfter(grantDate)) && !grantDao.alreadyGranted(emp.getEmpId(), grantDate, LeaveGrantDao.LEAVE_TYPE_SPECIAL)) {
                        if (grantDao.grantSpecialLeave(emp, loginUser)) grantedCount++;
                    }
                    break;
            }
        }

        if (grantedCount > 0) {
            request.setAttribute("message", grantedCount + "名に「" + leaveType + "」休暇を付与しました。");
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
