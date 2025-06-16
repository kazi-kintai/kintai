package kintai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 勤怠登録に関連するリクエストを処理するサーブレット。
 */
@WebServlet("/worktime")
public class WorkTimeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * GETリクエストで、勤怠登録画面(dakoku.jsp)を表示する。
     * 表示の前に、本日の打刻データを準備してJSPに渡す。
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // --- ★★★ ここからデータ取得のシミュレーション ★★★ ---
        // 次のステップで、この部分はWorkTimeDaoの呼び出しに置き換わります。
        
        // 1. 本日の出退勤データを準備（シミュレーション）
        Map<String, String> workTimeData = new HashMap<>();
        workTimeData.put("clockInTime", "09:02"); // DBから取得した出勤時刻
        workTimeData.put("clockOutTime", "18:05"); // DBから取得した退勤時刻

        // 2. 本日の休憩記録リストを準備（シミュレーション）
        List<Map<String, String>> breakList = new ArrayList<>();
        
        Map<String, String> break1 = new HashMap<>();
        break1.put("breakId", "101"); // 休憩記録のユニークID
        break1.put("startTime", "12:00");
        break1.put("endTime", "13:00");
        breakList.add(break1);
        
        Map<String, String> break2 = new HashMap<>();
        break2.put("breakId", "102");
        break2.put("startTime", "15:00");
        break2.put("endTime", "15:15");
        breakList.add(break2);

        // --- データをJSPに渡すためにリクエストにセット ---
        request.setAttribute("workTimeData", workTimeData);
        request.setAttribute("breakList", breakList);

        // --- 画面表示 ---
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/dakoku.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * POSTリクエストで、dakoku.jspから送信されたデータを受け取る。
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("delete_break".equals(action)) {
            // --- 休憩削除の処理 ---
            String breakId = request.getParameter("breakId");
            System.out.println("削除要求を受け取りました。削除対象の休憩ID: " + breakId);
            // 次のステップで、ここにWorkTimeDao.deleteBreak(breakId)の呼び出しが入ります。

        } else {
            // --- 時間登録の処理 ---
            String clockInTime = request.getParameter("clockInTime");
            String clockOutTime = request.getParameter("clockOutTime");
            String[] breakStartTimes = request.getParameterValues("breakStartTime");
            String[] breakEndTimes = request.getParameterValues("breakEndTime");
            
            System.out.println("勤怠登録データを受け取りました。");
            // (ここにWorkTimeDao.save(...)の呼び出しが入ります)
        }
        
        // どんな処理の後でも、最終的には画面を再表示して最新の状態を見せる
        doGet(request, response);
    }
}
