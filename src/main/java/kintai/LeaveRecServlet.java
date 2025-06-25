package kintai;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import kintai.Employee;
import kintai.LeaveBalance;
import kintai.LeaveRec;
import kintai.LeaveType;
import kintai.dao.DBManager;
import kintai.dao.EmployeeDAO;
import kintai.dao.LeaveBalanceDAO;
import kintai.dao.LeaveRecDAO;
import kintai.dao.LeaveTypeDAO;

@WebServlet("/LeaveRecServlet")
public class LeaveRecServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LeaveRecServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection conn = DBManager.getConnection()) {
            LeaveTypeDAO typeDAO = new LeaveTypeDAO(conn);
            EmployeeDAO empDAO = new EmployeeDAO(conn);

            List<LeaveType> leaveTypeList = typeDAO.findAll();
            List<Employee> employeeList = empDAO.findAll();

            Set<String> deptSet = new HashSet<>();
            for (Employee e : employeeList) {
                if (e.getDeptNo() != null) {
                    deptSet.add(e.getDeptNo());
                }
            }
            List<String> departmentList = new ArrayList<>(deptSet);

            request.setAttribute("leaveTypeList", leaveTypeList);
            request.setAttribute("employeeList", employeeList);
            request.setAttribute("departmentList", departmentList);
            request.setAttribute("employee", null);
            request.setAttribute("leaveList", new ArrayList<LeaveRec>());
            request.setAttribute("balance", null);
            request.setAttribute("remainingPaidLeave", 0);
            request.setAttribute("remainingSpecialLeave", 0);
            request.setAttribute("remainingCompLeave", 0);

            request.getRequestDispatcher("web/leave_main.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String mode = request.getParameter("mode");
        String empNo = request.getParameter("empNo");
        String deptNo = request.getParameter("deptNo");

        try (Connection conn = DBManager.getConnection()) {
            LeaveRecDAO leaveRecDAO = new LeaveRecDAO(conn);
            LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO(conn);
            LeaveTypeDAO typeDAO = new LeaveTypeDAO(conn);
            EmployeeDAO empDAO = new EmployeeDAO(conn);

            LeaveRec editingRec = null;

            if ("editForm".equals(mode)) {
                int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                editingRec = leaveRecDAO.findById(leaveId);
                request.setAttribute("editingRec", editingRec);
                empNo = editingRec.getEmpNo();
            }

            if ("add".equals(mode)) {
                LeaveRec rec = new LeaveRec();
                rec.setEmpNo(empNo);
                rec.setLeaveTypeId(Integer.parseInt(request.getParameter("leaveTypeId")));
                rec.setStartDate(Date.valueOf(request.getParameter("startDate")));
                rec.setEndDate(Date.valueOf(request.getParameter("endDate")));
                rec.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                int days = (int) (rec.getEndDate().toLocalDate().toEpochDay() - rec.getStartDate().toLocalDate().toEpochDay()) + 1;
                LeaveBalance balance = balanceDAO.getBalance(empNo, rec.getLeaveTypeId());

                if (balance != null && balance.getRemainingDays() >= days) {
                    leaveRecDAO.insert(rec);
                    balanceDAO.updateUsedDays(empNo, rec.getLeaveTypeId(), days);
                } else {
                    request.setAttribute("error", "有給残日数が不足しています");
                }
            }

            if ("update".equals(mode)) {
                int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                LeaveRec old = leaveRecDAO.findById(leaveId);

                LeaveRec rec = new LeaveRec();
                rec.setLeaveId(leaveId);
                rec.setEmpNo(old.getEmpNo()); // consistent empNo
                rec.setLeaveTypeId(Integer.parseInt(request.getParameter("leaveTypeId")));
                rec.setStartDate(Date.valueOf(request.getParameter("startDate")));
                rec.setEndDate(Date.valueOf(request.getParameter("endDate")));

                int oldDays = (int) (old.getEndDate().toLocalDate().toEpochDay() - old.getStartDate().toLocalDate().toEpochDay()) + 1;
                int newDays = (int) (rec.getEndDate().toLocalDate().toEpochDay() - rec.getStartDate().toLocalDate().toEpochDay()) + 1;

                if (old.getLeaveTypeId() == rec.getLeaveTypeId()) {
                    int delta = newDays - oldDays;
                    LeaveBalance balance = balanceDAO.getBalance(rec.getEmpNo(), rec.getLeaveTypeId());

                    if (balance != null && balance.getRemainingDays() >= delta) {
                        leaveRecDAO.update(rec);
                        balanceDAO.updateUsedDays(rec.getEmpNo(), rec.getLeaveTypeId(), delta);
                    } else {
                        request.setAttribute("error", "更新できません（残日数が不足しています）");
                    }
                } else {
                    balanceDAO.updateUsedDays(rec.getEmpNo(), old.getLeaveTypeId(), -oldDays);
                    LeaveBalance newBal = balanceDAO.getBalance(rec.getEmpNo(), rec.getLeaveTypeId());

                    if (newBal != null && newBal.getRemainingDays() >= newDays) {
                        leaveRecDAO.update(rec);
                        balanceDAO.updateUsedDays(rec.getEmpNo(), rec.getLeaveTypeId(), newDays);
                    } else {
                        request.setAttribute("error", "更新できません（新しい休暇種別の残日数が不足しています）");
                    }
                }

                editingRec = null; // Exit edit mode after updating
            }

            if ("delete".equals(mode)) {
                int leaveId = Integer.parseInt(request.getParameter("leaveId"));
                leaveRecDAO.delete(leaveId);
            }

            List<LeaveRec> leaveList = leaveRecDAO.findByEmpNo(empNo);
            List<LeaveType> leaveTypeList = typeDAO.findAll();
            List<Employee> employeeList = empDAO.findAll();
            Employee employee = empDAO.findByEmpNo(empNo);

            Set<String> deptSet = new HashSet<>();
            for (Employee e : employeeList) {
                if (e.getDeptNo() != null) {
                    deptSet.add(e.getDeptNo());
                }
            }
            List<String> departmentList = new ArrayList<>(deptSet);

            List<LeaveBalance> balances = balanceDAO.findAllByEmpNo(empNo);
            int paid = 0, special = 0, comp = 0;
            for (LeaveBalance b : balances) {
                switch (b.getLeaveTypeId()) {
                    case 1: paid = b.getRemainingDays(); break;
                    case 2: special = b.getRemainingDays(); break;
                    case 3: comp = b.getRemainingDays(); break;
                }
            }

            request.setAttribute("leaveList", leaveList);
            request.setAttribute("leaveTypeList", leaveTypeList);
            request.setAttribute("employee", employee);
            request.setAttribute("balance", balanceDAO.getBalance(empNo, 1));
            request.setAttribute("employeeList", employeeList);
            request.setAttribute("departmentList", departmentList);
            request.setAttribute("selectedEmpNo", empNo);
            request.setAttribute("selectedDept", deptNo);
            request.setAttribute("remainingPaidLeave", paid);
            request.setAttribute("remainingSpecialLeave", special);
            request.setAttribute("remainingCompLeave", comp);

            if (editingRec != null) {
                request.setAttribute("editingRec", editingRec);
            }

            request.getRequestDispatcher("/web/leave_main.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}