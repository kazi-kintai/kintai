package kintai;

import java.io.Serializable;

/**
 * 部署情報（deptテーブルのレコード）を保持するJavaBean。
 */
public class DeptBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deptNo;      // deptテーブルの「DEPTNO」列に対応
    private String deptName;    // deptテーブルの「DEPTNAME」列に対応

    /**
     * デフォルトコンストラクタ
     */
    public DeptBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param deptNo 部署番号
     * @param deptName 部署名
     */
    public DeptBean(String deptNo, String deptName) {
        this.deptNo = deptNo;
        this.deptName = deptName;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}