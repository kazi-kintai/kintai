package kintai;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 従業員情報（empテーブルのレコード）を保持するJavaBean。
 */
public class EmpBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- empテーブルの列に対応するフィールド ---
    private String empNo;         // EMP_ID
    private String empName;       // EMP_NAME
    private String deptId;        // DEPT_ID
    private String postId;        // POST_ID
    private int roleId;           // ROLE_ID
    private String empType;       // EMP_TYPE
    private String pass;          // PASS
    private String mail;          // MAIL
    private LocalDate empDate;    // EMP_DATE
    private boolean isActive;     // IS_ACTIVE
    private LocalDate leaveDate;  // LEAVE_DATE

    // --- 表示用の追加フィールド（JOINで取得） ---
    private String deptName;      // 部署名
    private String postName;      // 役職名
    private String roleName;      // ロール名

    public EmpBean() {
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getEmpType() {
        return empType;
    }

    public void setEmpType(String empType) {
        this.empType = empType;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public LocalDate getEmpDate() {
        return empDate;
    }

    public void setEmpDate(LocalDate empDate) {
        this.empDate = empDate;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}