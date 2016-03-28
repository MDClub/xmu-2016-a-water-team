package cn.edu.xmu.hotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URLEncoder;



public class SignUp extends Activity implements OnClickListener{

    private EditText id_Edit_SignUp = null;

    private Button reg_SignUp = null;

    private Spinner sexSelect = null;

    private EditText nameSignUP = null;

    private EditText mobileSignUp = null;

    private EditText passwordSignUp = null;

    private Button checkCommit = null;

    private boolean finishFlag = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        id_Edit_SignUp = (EditText)findViewById(R.id.id_edit_signup);
        reg_SignUp = (Button)findViewById(R.id.id_check);
        checkCommit = (Button)findViewById(R.id.signup_checkcommit);
        sexSelect = (Spinner)findViewById(R.id.sex_signup);
        nameSignUP = (EditText)findViewById(R.id.name_edit_signup);
        mobileSignUp = (EditText)findViewById(R.id.mobile_edit_signup);
        passwordSignUp = (EditText)findViewById(R.id.password_edit_signup);
        reg_SignUp.setOnClickListener(this);
        checkCommit.setOnClickListener(this);
        ActivityCollector.addActivity(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_check:
                if((id_Edit_SignUp.getText().length() == 18))
                    HttpUtil.sendHttpRequest(HttpUtil.checkLink + "id=" + id_Edit_SignUp.getText()
                            , new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            Message msg = new Message();
                            msg.what=0x123;
                            msg.obj=response;
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void onError(Exception e) {
                            Message msg = new Message();
                            msg.what = 0x456;
                            msg.obj = "网络错误！";
                            handler.sendMessage(msg);
                        }
                    });
                else {
                    Message msg = new Message();
                    msg.what = 0x456;
                    msg.obj = "请按格式输入您的身份证号!(18位数字)";
                    handler.sendMessage(msg);
                }
                break;
            case R.id.signup_checkcommit:
                try {
                    if ((id_Edit_SignUp.getText().length() == 18) && (nameSignUP.getText().length() != 0)
                            && (mobileSignUp.getText().toString().matches("^[0-9]+$") == true) && (passwordSignUp.getText().length() != 0))
                        HttpUtil.sendHttpRequest(HttpUtil.signupCommitLink + "id=" + id_Edit_SignUp.getText()
                                + "&" + "name=" + URLEncoder.encode(nameSignUP.getText().toString(), "UTF-8")
                                + "&" + "sex=" + URLEncoder.encode(sexSelect.getSelectedItem().toString(), "UTF-8")
                                + "&" + "mobile=" + mobileSignUp.getText()
                                + "&" + "password=" + MD5.stringToMD5(passwordSignUp.getText().toString()), new HttpCallbackListener() {
                            @Override
                            public void onFinish(String response) {
                                Message msg = new Message();
                                msg.what = 0x123;
                                msg.obj = response;
                                handler.sendMessage(msg);
                                Log.d("Test", MD5.stringToMD5(passwordSignUp.getText().toString()));
                            }

                            @Override
                            public void onError(Exception e) {
                                Message msg = new Message();
                                msg.what = 0x456;
                                msg.obj = "网络错误，请重试";
                                handler.sendMessage(msg);
                            }
                        });
                    else{
                        Message msg = new Message();
                        msg.what = 0x456;
                        msg.obj = "请按正确格式输入您的个人信息！";
                        handler.sendMessage(msg);
                    }
                } catch (UnsupportedEncodingException e) {
                    Message msg = new Message();
                    msg.what = 0x456;
                    msg.obj = "编码错误，请重试！";
                    handler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if (msg.what == 0x123) {

                NewAppJSONObject.parseJSONWithJSONObject((String) msg.obj, new JSONCallbackListener() {
                    @Override
                    public void onFinish(String data, String info, int code) {
                        if(code == 1001) {
                            reg_SignUp.setEnabled(false);
                            reg_SignUp.setText("此身份证号可用");
                        }
                        else {
                            Toast.makeText(MyApplication.getContext(), info, Toast.LENGTH_SHORT).show();
                            if (code == 5) {
                                reg_SignUp.setEnabled(true);
                                reg_SignUp.setText("检测一下以前是否注册过");
                            } else if (code == 1002) {
                                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                editor.putString("username_reged", id_Edit_SignUp.getText().toString());
                                editor.putString("password_reged", MD5.stringToMD5(passwordSignUp.getText().toString()));
                                editor.commit();
                                Intent backMainIntent = new Intent(MyApplication.getContext(), MainActivity.class);
                                startActivity(backMainIntent);
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MyApplication.getContext(), "客户端JSON数据解析错误，请您再试一次", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

}
