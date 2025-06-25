package kintai;

import java.io.Serializable;

/**
 * 休暇種別情報（leave_typeテーブルのレコード）を保持するJavaBean。
 */
public class LeaveTypeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int leaveTypeId;       // LEAVE_TYPE_ID (主キー)
    private String leaveTypeName;  // LEAVE_TYPE_NAME
    private boolean isPaid;        // IS_PAID (有給フラグ)

    public LeaveTypeBean() {
    }

    public LeaveTypeBean(int leaveTypeId, String leaveTypeName, boolean isPaid) {
        this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.isPaid = isPaid;
    }

    public int getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(int leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getLeaveTypeName() {
        return leaveTypeName;
    }

    public void setLeaveTypeName(String leaveTypeName) {
        this.leaveTypeName = leaveTypeName;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}
