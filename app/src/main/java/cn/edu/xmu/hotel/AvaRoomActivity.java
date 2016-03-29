
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
                            CustomDialog.Builder builder = new CustomDialog.Builder(MyApplication.getContext());
                            builder.setMessage("您的余额不足,是否立即充值?");
                            builder.setTitle("提示");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(MyApplication.getContext(), RechargeActivity.class);
                                    intent.putExtra("amount", Integer.parseInt(amount));
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

        /*
        int totalPrice = days * Integer.parseInt((String)roomTypePrice.get(position).toString());
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        roomtypeCommit = (String)roomTypeList.get(position).toString();
        builder.setMessage(().append("\u60a8\u9009\u62e9\u7684\u623f\u95f4\u7c7b\u578b\u4e3a:").append((String)roomTypeList.get(position).toString()).append("\n").append("\u5171"));
        localCustomDialog1 = ().append("\u60a8\u9009\u62e9\u7684\u623f\u95f4\u7c7b\u578b\u4e3a:").append((String)roomTypeList.get(position).toString()).append("\n").append("\u5171").append(Integer.toString("\u5171")).append("\u665a,").append("\u603b\u4ef7\u4e3a: ").append(Integer.toString(totalPrice)).toString();
        builder.setTitle("\u8bf7\u786e\u8ba4\u60a8\u7684\u9009\u62e9");
        builder.setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener(this) {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SharedPreferences pref = getSharedPreferences("data", 0x0);
                String tempusername = pref.getString("tempusername", "");
                if(tempusername != "") {
                    String temppassword = pref.getString("temppassword", "");
                    try {
                        HttpUtil.sendHttpRequest(().append(HttpUtil.commitReservLink).append("id=").append(tempusername).append("&").append("password=").append(temppassword).append("&").append("startday=").append(startDay).append("&").append("endday=").append(endDay).append("&").append("roomtype="), roomtypeCommit);
                        return;
                        localStringBuilder1 = ().append(HttpUtil.commitReservLink).append("id=").append(tempusername).append("&").append("password=").append(temppassword).append("&").append("startday=").append(startDay).append("&").append("endday=").append(endDay).append("&").append("roomtype=").append(URLEncoder.encode(roomtypeCommit, "UTF-8")).toString()AvaRoomActivity.3.1 localAvaRoomActivity.3.12 = new HttpCallbackListener(this) {

                            public void onFinish(String response) {
                                Message msg = new Message();
                                msg.what = 0x789;
                                msg.obj = response;
                                handler.sendMessage(msg);
                            }

                            public void onError(Exception e) {
                                Message msg = new Message();
                                msg.what = 0x456;
                                msg.obj = "\u7f51\u7edc\u9519\u8bef\uff0c\u8bf7\u91cd\u8bd5";
                                handler.sendMessage(msg);
                            }
                        };
                    } catch(UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return;
                    }
                    Toast.makeText(MyApplication.getContext(), "\u767b\u5f55\u8d85\u65f6\uff01\u8bf7\u91cd\u65b0\u767b\u5f55\uff01", roomtypeCommit);
                }
            }
        });
        builder.setNegativeButton("\u53d6\u6d88", new DialogInterface.OnClickListener(this) {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
        */

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

                    Log.d("Testusername", tempusername);
                    Log.d("Testpassword", temppassword);

                    Log.d("Testfordif", HttpUtil.commitReservLink + "id=" + tempusername + "&" +
                            "password=" + temppassword + "&" +
                            "startday=" + startDay + "&" +
                            "endday=" + endDay + "&" +
                            "roomtype=" + roomtypeCommit);
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
