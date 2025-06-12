package kintai; 

import java.io.Serializable;

public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- フィールド定義 ---
    // empテーブルの各列に対応する。

    private String empno;       // empテーブルの「emp_id」列に対応
    private String name;        // empテーブルの「emp_name」列に対応
    private String deptId;     // empテーブルの「dept_id」列に対応
    private String postId;       // empテーブルの「pos_id」列に対応

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
}
