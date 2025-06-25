package kintai;

import java.io.Serializable;

public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- フィールド定義 ---
    // empテーブルの各列に対応する。新しいER図の定義に合わせる。

    private String empno;       // empテーブルの「EMPNO」列に対応
    private String name;        // empテーブルの「EMPNAME」列に対応 (旧emp_name)
    private String deptNo;      // empテーブルの「DEPTNO」列に対応 (旧deptId)
    private String postNo;      // empテーブルの「POSTNO」列に対応 (旧postId)
    private int roleId;         // empテーブルの「ROLEID」列に対応 (旧role)
    private String empType;        // empテーブルの「GRADENO」列に対応 (新規追加)
    // PASS, MAIL, EMPDATE はUserBeanのログイン情報として必要であれば追加可能だが、
    // 認証には直接使用しないため、ここでは省略。

    public UserBean() {
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public String getEmpno() {
        return empno;
    }
    public void setEmpno(String empno) {
        this.empno = empno;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
}
