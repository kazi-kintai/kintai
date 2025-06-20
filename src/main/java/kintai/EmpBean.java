package kintai;

import java.io.Serializable;
import java.time.LocalDate; // LocalDateをインポート

/**
 * 従業員情報（empテーブルのレコード）を保持するJavaBean。
 * 新しいER図のempテーブルの全列に対応し、
 * 部署名、役職名、ロール名、等級名も保持できるように拡張。
 */
public class EmpBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- empテーブルの列に対応するフィールド ---
    private String empNo;       // empテーブルの「EMPNO」列に対応
    private String empName;     // empテーブルの「EMPNAME」列に対応
    private String deptNo;      // empテーブルの「DEPTNO」列に対応
    private String postNo;      // empテーブルの「POSTNO」列に対応
    private int roleId;         // empテーブルの「ROLEID」列に対応
    private int gradeNo;        // empテーブルの「GRADENO」列に対応 (新規追加)
    private String pass;        // empテーブルの「PASS」列に対応
    private String mail;        // empテーブルの「MAIL」列に対応 (新規追加)
    private LocalDate empDate;  // empテーブルの「EMPDATE」列に対応 (新規追加)
    
    // --- 表示用の追加フィールド（JOINで取得） ---
    private String deptName;    // 部署名（dept.DEPTNAME）
    private String postName;    // 役職名（post.POSTNAME）
    private String roleName;    // ロール名（role.ROLENAME） (新規追加)
    private String gradeName;   // 等級名（grade.GRADENAME） (新規追加)

    /**
     * デフォルトコンストラクタ
     */
    public EmpBean() {
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

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

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getPostNo() {
        return postNo;
    }

    public void setPostNo(String postNo) {
        this.postNo = postNo;
    }

    // 旧getRole/setRoleからgetRoleId/setRoleIdへ変更
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    // 新規追加
    public int getGradeNo() {
        return gradeNo;
    }

    public void setGradeNo(int gradeNo) {
        this.gradeNo = gradeNo;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    // 新規追加
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    // 新規追加
    public LocalDate getEmpDate() {
        return empDate;
    }

    public void setEmpDate(LocalDate empDate) {
        this.empDate = empDate;
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

    // 新規追加
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    // 新規追加
    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }
}
