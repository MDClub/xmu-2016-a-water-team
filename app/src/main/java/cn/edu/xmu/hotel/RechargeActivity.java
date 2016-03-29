package cn.edu.xmu.hotel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RechargeActivity extends Activity implements View.OnClickListener{

    private int amount = 0;

    private EditText chargeAmount = null;

    private Button amountCommitButton = null;

    private String tempUserName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        chargeAmount = (EditText)findViewById(R.id.chargeamount);
        amountCommitButton = (Button)findViewById(R.id.amountcommitbutton);
        amountCommitButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        amount = intent.getIntExtra("amount", 0);
        if(amount != 0) {
            chargeAmount.setText(String.valueOf(amount));
        }
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        tempUserName = pref.getString("tempusername", "");
        if(tempUserName.equalsIgnoreCase("") == true) {
            Toast.makeText(this, "您还未登录,请先登录!", Toast.LENGTH_SHORT).show();
            Intent intentToMain = new Intent(this, MainActivity.class);
            startActivity(intentToMain);
            finish();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = (String)msg.obj;
            if(msg.what == 0x123) {
                NewAppJSONObject.parseJSONWithJSONObject(s, new JSONCallbackListener() {
                    @Override
                    public void onFinish(String data, String info, int code) {
                        Toast.makeText(MyApplication.getContext(), info, Toast.LENGTH_SHORT).show();
                        if(code == 1004) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MyApplication.getContext(), "客户端JSON数据解析错误,请重试!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if(msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), s, Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amountcommitbutton: {
                if((chargeAmount.getText().toString().matches("^[0-9]+$") == false)||(Integer.parseInt(chargeAmount.getText().toString()) <= 0)) {
                    Toast.makeText(this, "充值金额必须为正整数,请核对后再试", Toast.LENGTH_SHORT).show();
                }
                else {
                    HttpUtil.sendHttpRequest(HttpUtil.rechargeLink + "id=" + tempUserName + "&" +
                            "amount=" + chargeAmount.getText().toString(), new HttpCallbackListener() {
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
                            msg.obj = "网络错误，请重试";
                            handler.sendMessage(msg);
                        }
                    });
                }
            }
        }
    }
}
