package kintai;

import java.io.Serializable;
import java.sql.Time;

/**
 * 休憩情報（breakテーブルのレコード）を保持するJavaBean。
 */
public class BreakBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int breakId;         // breakテーブルの「BREAKID」列に対応
    private int recId;           // breakテーブルの「RECID」列に対応
    private Time breakStart;     // breakテーブルの「BREAKSTART」列に対応
    private Time breakEnd;       // breakテーブルの「BREAKEND」列に対応

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public int getBreakId() {
        return breakId;
    }

    public void setBreakId(int breakId) {
        this.breakId = breakId;
    }

    public int getRecId() {
        return recId;
    }

    public void setRecId(int recId) {
        this.recId = recId;
    }

    public Time getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(Time breakStart) {
        this.breakStart = breakStart;
    }

    public Time getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(Time breakEnd) {
        this.breakEnd = breakEnd;
    }
}