
package cn.edu.xmu.hotel;

import android.app.ListActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Handler;
import java.util.ArrayList;
import org.json.JSONArray;
import java.util.HashMap;
import org.json.JSONObject;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.ListView;
import android.view.View;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

public class AvaRoomActivity extends ListActivity {
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    List<String> roomTypeList = new ArrayList<String>();
    List<String> roomTypePrice = new ArrayList<String>();

    private int days;
    String endDay;
    String roomtypeCommit;
    String startDay;

    private String amount = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = (String)msg.obj;
            if(msg.what == 0x123) {
                dataList = parseJSONWithJSONObject(s);
                SimpleAdapter adapter = new SimpleAdapter(MyApplication.getContext(),dataList,R.layout.vlist,
                        new String[]{"title","info","img","price","amount"},
                        new int[]{R.id.title,R.id.info,R.id.img,R.id.price,R.id.amount});
                setListAdapter(adapter);
                if(roomTypeList.size() == 0) {
                    Toast.makeText(MyApplication.getContext(), "该时段已满房!请返回查找其他时段的房间!", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            } else if (msg.what == 0x789) {
                NewAppJSONObject.parseJSONWithJSONObject(s, new JSONCallbackListener() {
                    @Override
                    public void onFinish(String data, String info, int code) {
                        Toast.makeText(MyApplication.getContext(), info, Toast.LENGTH_SHORT).show();

                        if(code == 7) {
                            amount = data;
                            Intent intent = new Intent(MyApplication.getContext(), RechargeActivity.class);
                            intent.putExtra("amount", Integer.parseInt(amount));
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MyApplication.getContext(), "客户端JSON数据解析错误,请重试!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleAdapter adapter = new SimpleAdapter(this,dataList,R.layout.vlist,
                new String[]{"title","info","img","price","amount"},
                new int[]{R.id.title,R.id.info,R.id.img,R.id.price,R.id.amount});
        setListAdapter(adapter);
    }

    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        startDay = intent.getStringExtra("startDay");
        endDay = intent.getStringExtra("endDay");
        days = intent.getIntExtra("days", 0);
        HttpUtil.sendHttpRequest(HttpUtil.searchLink + "startday=" + startDay + "&" + "endday=" + endDay, new HttpCallbackListener() {

            public void onFinish(String response) {
                Message msg = new Message();
                msg.what = 0x123;
                msg.obj = response;
                handler.sendMessage(msg);
            }

            public void onError(Exception e) {
                Message msg = new Message();
                msg.what = 0x456;
                msg.obj = "网络错误,请重试!";
                handler.sendMessage(msg);
            }
        });
    }
    private List<Map<String, Object>> parseJSONWithJSONObject(String jsonData) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            roomTypeList = new ArrayList<String>();
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String roomtype = jsonObject.getString("roomtype");
                String amount = jsonObject.getString("amount");
                String price = jsonObject.getString("price");
                String remark = jsonObject.getString("remark");
                map.put("title", roomtype);
                map.put("info", remark);
                map.put("img", R.drawable.i1);
                map.put("price", "\n" + "      " + price);
                map.put("amount", amount + "间");
                list.add(map);
                roomTypeList.add(roomtype);
                roomTypePrice.add(price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        int totalPrice = days * (Integer.parseInt(roomTypePrice.get(position).toString()));
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        roomtypeCommit = roomTypeList.get(position).toString();
        builder.setMessage("您选择的房间类型为:" + roomTypeList.get(position).toString() + "\n" + "共" + Integer.toString(days) + "晚," + "总价为: " + Integer.toString(totalPrice));
        builder.setTitle("请确认您的选择");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                String tempusername = pref.getString("tempusername", "");
                if(tempusername != "") {
                    String temppassword = pref.getString("temppassword", "");
                    try {
                        HttpUtil.sendHttpRequest(HttpUtil.commitReservLink + "id=" + tempusername + "&" +
                                "password=" + temppassword + "&" +
                                "startday=" + startDay + "&" +
                                "endday=" + endDay + "&" +
                                "roomtype=" + URLEncoder.encode(roomtypeCommit, "UTF-8"), new HttpCallbackListener() {
                            @Override
                            public void onFinish(String response) {
                                Message msg = new Message();
                                msg.what = 0x789;
                                msg.obj = response;
                                handler.sendMessage(msg);
                            }
                            @Override
                            public void onError(Exception e) {
                                Message msg = new Message();
                                msg.what = 0x456;
                                msg.obj = "网络错误，请重试";
                                handler.sendMessage(msg);
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(MyApplication.getContext(), "登录超时！请重新登录！", Toast.LENGTH_SHORT);
                    return;
                }
            }
        });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.create().show();
}


}
