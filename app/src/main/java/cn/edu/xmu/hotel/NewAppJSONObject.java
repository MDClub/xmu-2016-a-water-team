package cn.edu.xmu.hotel;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/3/27.
 */
public class NewAppJSONObject  {
    public static void parseJSONWithJSONObject(final String JSONData, final JSONCallbackListener listener) {
        try {
            if (listener != null) {
                JSONObject jsonObject = new JSONObject(JSONData);
                String data = jsonObject.getString("data");
                String info = jsonObject.getString("info");
                int code = jsonObject.getInt("code");
                listener.onFinish(data, info, code);
            }
        } catch (JSONException e) {
            if (listener != null) {
                Log.d("Test", e.getMessage());
                listener.onError(e);
            }
        }
    }
}
