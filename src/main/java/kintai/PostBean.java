package kintai;

import java.io.Serializable;

/**
 * 役職情報（postテーブルのレコード）を保持するJavaBean。
 */
public class PostBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String postNo;      // postテーブルの「POSTNO」列に対応
    private String postName;    // postテーブルの「POSTNAME」列に対応

    /**
     * デフォルトコンストラクタ
     */
    public PostBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param postNo 役職番号
     * @param postName 役職名
     */
    public PostBean(String postNo, String postName) {
        this.postNo = postNo;
        this.postName = postName;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public String getPostNo() {
        return postNo;
    }

    public void setPostNo(String postNo) {
        this.postNo = postNo;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }
}