package kintai;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/leaveGrant")
public class LeaveGrantServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private EmpDao empDao;
    private LeaveGrantDao grantDao;

    @Override
    public void init() throws ServletException {
        empDao = new EmpDao();
        grantDao = new LeaveGrantDao();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        int annualGranted = 0;   // 年次有給
        int initialGranted = 0;  // 初回（3・6か月）
        int specialGranted = 0;  // 特別休暇

        // 今日の日付取得
        LocalDate today = LocalDate.now();

        // 対象社員を取得（正社員に限定）
        List<EmpBean> empList = empDao.findAllFullTimeEmployees();

        // 各従業員に対して付与処理を実施
        for (EmpBean emp : empList) {

            // 初回 3か月（stage=1）・6か月（stage=2）
            if (grantDao.grantInitialAnnualLeave(emp, 1)) {
                initialGranted++;
            }
            if (grantDao.grantInitialAnnualLeave(emp, 2)) {
                initialGranted++;
            }

            // 年次有給（7月1日付与）
            if (grantDao.grantAnnualLeave(emp)) {
                annualGranted++;
            }

            // 特別休暇（7月1日）
            if (grantDao.grantSpecialLeave(emp)) {
                specialGranted++;
            }
        }

        // 結果を JSP に転送
        request.setAttribute("annualGranted", annualGranted);
        request.setAttribute("initialGranted", initialGranted);
        request.setAttribute("specialGranted", specialGranted);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/leave_grant.jsp");
        dispatcher.forward(request, response);
    }
}