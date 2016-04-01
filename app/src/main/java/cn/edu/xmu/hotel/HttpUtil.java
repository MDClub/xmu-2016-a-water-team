package cn.edu.xmu.hotel;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/3/20.
 */
public class HttpUtil {

    public static String host = "http://10.30.31.204";
    //public static String host = "http://119.29.101.221/ftpuser";

    public static String currentOrderLink = host + "/search/currentorder.php?";

    public static String historyOrderLink = host + "/search/historyorder.php?";

    public static String currentCheckInLink = host + "/search/currentcheckin.php?";

    public static String waitCheckinList = host + "/search/waitcheckinlist.php?";

    public static String loginLink = host + "/login/login.php?";

    public static String checkLink = host + "/signup/check.php?";

    public static String signupCommitLink = host + "/signup/signup.php?";

    public static String timeGetLink = host + "/time/time.php";

    public static String searchLink = host + "/search/search.php?";

    public static String commitReservLink = host + "/commit/checkbalance.php?";

    public static String rechargeLink = host + "/recharge/recharge.php?";

    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
