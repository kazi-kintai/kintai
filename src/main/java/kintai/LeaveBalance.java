package kintai;

import java.io.Serializable;

public class LeaveBalance implements Serializable {
    private String empNo;
    private int leaveTypeId;
    private int totalDays;
    private int usedDays;

    public String getEmpNo() { return empNo; }
    public void setEmpNo(String empNo) { this.empNo = empNo; }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getUsedDays() { return usedDays; }
    public void setUsedDays(int usedDays) { this.usedDays = usedDays; }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }
}