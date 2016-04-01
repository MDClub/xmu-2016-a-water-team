package cn.edu.xmu.hotel;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckinActivity extends ListActivity {

    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private boolean errorflag = false;
    private String infoMessage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleAdapter adapter = new SimpleAdapter(this,dataList,R.layout.orderlist,
                new String[]{"title","startday","endday","status","orderid","totalprice"},
                new int[]{R.id.title,R.id.startday,R.id.endday,R.id.status,R.id.orderid,R.id.totalprice});
        setListAdapter(adapter);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = (String)msg.obj;
            if(msg.what == 0x123) {
                dataList = parseJSONWithJSONObject(s);
                if(errorflag == false) {
                    SimpleAdapter adapter = new SimpleAdapter(MyApplication.getContext(),dataList,R.layout.orderlist,
                            new String[]{"roomtype","starttime","endtime","status","orderid","amount"},
                            new int[]{R.id.title,R.id.startday,R.id.endday,R.id.status,R.id.orderid,R.id.totalprice});
                    setListAdapter(adapter);
                }
                else if(errorflag == true){
                    Toast.makeText(MyApplication.getContext(), infoMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else if (msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String tempusername = pref.getString("tempusername", "");
        if(tempusername != "") {
            String temppassword = pref.getString("temppassword", "");
            HttpUtil.sendHttpRequest(HttpUtil.waitCheckinList +
                    "id=" + tempusername + "&" +
                    "password=" + temppassword
                    , new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                    Message msg = new Message();
                    msg.what = 0x123;
                    msg.obj = response;
                    handler.sendMessage(msg);
                }

                @Override
                public void onError(Exception e) {
                    Message msg = new Message();
                    msg.what = 0x456;
                    msg.obj = "网络错误,请重试!";
                    handler.sendMessage(msg);
                }
            });
        }
        else {
            Toast.makeText(MyApplication.getContext(), "登录超时！请重新登录！", Toast.LENGTH_SHORT);
            finish();
        }
    }

    private List<Map<String, Object>> parseJSONWithJSONObject(String jsonData) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {

            JSONObject objMain = new JSONObject(jsonData);
            JSONObject userCheck = objMain.getJSONObject("usercheck");
            String data = userCheck.getString("data");
            infoMessage = userCheck.getString("info");
            Log.d("TestC", infoMessage);
            int code = userCheck.getInt("code");
            if(code >=1000) {
                errorflag = false;
            }
            else if(code < 1000){
                errorflag = true;
            }
            if(errorflag == false) {
                JSONArray valiData = objMain.getJSONArray("validata");
                for (int i = 0; i < valiData.length(); i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    JSONObject jsonObject = valiData.getJSONObject(i);
                    String roomtype = jsonObject.getString("roomtype");
                    String starttime = jsonObject.getString("starttime");
                    String endtime = jsonObject.getString("endtime");
                    String amount = jsonObject.getString("amount");
                    String status = jsonObject.getString("status");
                    String orderid = jsonObject.getString("orderid");
                    amount = "总金额: " + amount + "元";
                    orderid = "订单号: " + orderid;
                    starttime = "起止时间: " + starttime + " --- ";
                    map.put("roomtype", roomtype);
                    map.put("starttime", starttime);
                    map.put("endtime", endtime);
                    map.put("amount", amount);
                    map.put("status", status);
                    map.put("orderid", orderid);
                    list.add(map);
                }
            }
        } catch (Exception e) {
            Toast.makeText(MyApplication.getContext(), "JSON数据解析错误，请重试", Toast.LENGTH_SHORT).show();
            finish();
        }
        return list;
    }
}
