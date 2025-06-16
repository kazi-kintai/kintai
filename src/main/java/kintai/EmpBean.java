package kintai;

import java.io.Serializable;

/**
 * 社員情報（empテーブルのレコード）を保持するJavaBean。
 * 部署名と役職名も保持できるように拡張。
 */
public class EmpBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- empテーブルの列に対応するフィールド ---
    private String empNo;       // empテーブルの「EMPNO」列に対応
    private String empName;     // empテーブルの「EMPNAME」列に対応
    private String deptNo;      // empテーブルの「DEPTNO」列に対応
    private String postNo;      // empテーブルの「POSTNO」列に対応
    private String pass;        // empテーブルの「PASS」列に対応
    private int role;           // empテーブルの「ROLE」列に対応 (0:従業員, 1:管理者)
    
    // --- 表示用の追加フィールド ---
    private String deptName;    // 部署名（dept.DEPTNAME）
    private String postName;    // 役職名（post.POSTNAME）

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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
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
}