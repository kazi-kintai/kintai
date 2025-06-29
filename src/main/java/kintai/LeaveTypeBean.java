package kintai;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * leave_type テーブルに対応するJavaBean
 */
public class LeaveTypeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int leaveTypeId;           // LEAVE_TYPE_ID
    private String leaveTypeName;      // LEAVE_TYPE_NAME
    private boolean isPaid;            // IS_PAID
    private boolean isDeleted;         // IS_DELETED
    private Timestamp createdAt;       // CREATED_AT
    private String createdBy;          // CREATED_BY
    private Timestamp updatedAt;       // UPDATED_AT
    private String updatedBy;          // UPDATED_BY

    public LeaveTypeBean() {}

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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
