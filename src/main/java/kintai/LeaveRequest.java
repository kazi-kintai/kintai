package kintai;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class LeaveRequest implements Serializable {
    private int leaveId;
    private String empNo;
    private int leaveTypeId;
    private Date startDate;
    private Date endDate;
    private String reason;
    private String approvedBy;
    private Timestamp createdAt;

    private int totalPaidLeave;

    public int getLeaveId() { return leaveId; }
    public void setLeaveId(int leaveId) { this.leaveId = leaveId; }

    public String getEmpNo() { return empNo; }
    public void setEmpNo(String empNo) { this.empNo = empNo; }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getTotalPaidLeave() { return totalPaidLeave; }
    public void setTotalPaidLeave(int totalPaidLeave) { this.totalPaidLeave = totalPaidLeave; }
}