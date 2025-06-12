package kintai;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * データベースへの接続を提供するクラス。
 * このクラスのgetConnection()メソッドは、呼び出されるたびに新しい接続を確立します。
 */
public class DBAccess {

    /** データベースの接続情報 */
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/kintai?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Tokyo";
    private static final String DB_USER = "root";
    private static final String DB_PWD = ""; // MySQLに設定したパスワード

    /**
     * データベースへの接続を確立し、Connectionオブジェクトを返す。
     * このメソッドはnullを返しません。失敗した場合は例外をスローします。
     * @return データベース接続を表すConnectionオブジェクト
     * @throws SQLException データベースアクセスエラーが発生した場合
     * @throws ClassNotFoundException JDBCドライバが見つからない場合
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        // 1. JDBCドライバをロード
        Class.forName(DB_DRIVER);
        
        // 2. データベースに接続し、その接続を直接返す
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
    }
}
