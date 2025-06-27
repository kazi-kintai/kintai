package kintai;

import java.sql.Date;

public class LeaveBalanceBean {
    private String empNo;
    private int leaveTypeId;
    private String leaveTypeName;
    private Date grantedDate;      // java.sql.Dateで統一
    private Date expirationDate;
    private int grantedDays;       // 付与日数
    private int usedDays;

    public String getEmpNo() { return empNo; }
    public void setEmpNo(String empNo) { this.empNo = empNo; }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public Date getGrantedDate() { return grantedDate; }
    public void setGrantedDate(Date grantedDate) { this.grantedDate = grantedDate; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public int getGrantedDays() { return grantedDays; }
    public void setGrantedDays(int grantedDays) { this.grantedDays = grantedDays; }

    public int getUsedDays() { return usedDays; }
    public void setUsedDays(int usedDays) { this.usedDays = usedDays; }
}
