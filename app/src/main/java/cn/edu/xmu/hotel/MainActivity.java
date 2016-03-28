package cn.edu.xmu.hotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private Button reg = null;

    private Button login = null;

    private EditText id_edit = null;

    private EditText password_edit = null;

    private CheckBox checkBox = null;

    private boolean md5Flag = false;

    private String str;

    private boolean deleteFlag = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        if(checkBox.isChecked()){
            if(!(pref.getString("username_reged", "").equals(""))) {
                editor.putString("username", pref.getString("username_reged", ""));
                editor.putString("password", pref.getString("password_reged", ""));
                editor.putString("username_reged", "");
                editor.putString("password_reged", "");
                editor.commit();
            }
        }
        else {
            editor.putString("username", "");
            editor.putString("password", "");
            editor.putString("username_reged", "");
            editor.putString("password_reged", "");
            editor.commit();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String username;
        if(!((username = pref.getString("username_reged", "")).equals(""))) {
            id_edit.setText(username);
            password_edit.setText(pref.getString("password_reged", ""));
            deleteFlag = false;
        }
        else if(!(pref.getString("username", "").equals(""))) {
            id_edit.setText(pref.getString("username", ""));
            password_edit.setText(pref.getString("password", ""));
            deleteFlag = false;
        }
        else {
            id_edit.setText("");
            password_edit.setText("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reg = (Button)findViewById(R.id.reg);
        login = (Button)findViewById(R.id.login);
        id_edit = (EditText)findViewById(R.id.id_edit);
        password_edit = (EditText)findViewById(R.id.password_edit);
        checkBox = (CheckBox)findViewById(R.id.password_chebox);
        password_edit.addTextChangedListener(textWatcher);
        reg.setOnClickListener(this);
        login.setOnClickListener(this);
        ActivityCollector.addActivity(this);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if(deleteFlag == false) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deleteFlag = true;
                        Message message = new Message();
                        message.what = 0x321;
                        handler.sendMessage(message);
                    }
                }).start();
            }

        }
    };
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 0x123) {

                NewAppJSONObject.parseJSONWithJSONObject((String) msg.obj, new JSONCallbackListener() {
                    @Override
                    public void onFinish(String data, String info, int code) {
                        Toast.makeText(MyApplication.getContext(), info, Toast.LENGTH_SHORT).show();
                        if(code == 1000) {
                            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            if(checkBox.isChecked()) {
                                if(!(pref.getString("username_reged", "").equals(""))) {
                                    editor.putString("username", pref.getString("username_reged", ""));
                                    editor.putString("password", pref.getString("password_reged", ""));
                                    editor.putString("username_reged", "");
                                    editor.putString("password_reged", "");
                                    editor.putString("tempusername", id_edit.getText().toString());
                                    editor.putString("temppassword", password_edit.getText().toString());
                                    editor.commit();
                                }
                                else {
                                    if(deleteFlag == true) {
                                        editor.putString("username", id_edit.getText().toString());
                                        editor.putString("password", MD5.stringToMD5(password_edit.getText().toString()));
                                        editor.putString("tempusername", id_edit.getText().toString());
                                        editor.putString("temppassword", MD5.stringToMD5(password_edit.getText().toString()));
                                        editor.commit();
                                    }
                                    else {
                                        editor.putString("username", id_edit.getText().toString());
                                        editor.putString("password", password_edit.getText().toString());
                                        editor.putString("tempusername", id_edit.getText().toString());
                                        editor.putString("temppassword", (password_edit.getText().toString()));
                                        editor.commit();
                                    }
                                }
                            }

                            Intent intentToSelect = new Intent(MyApplication.getContext(), SelectReOrCheck.class);
                            startActivity(intentToSelect);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("Test", e.getMessage());
                        Toast.makeText(MyApplication.getContext(), "客户端JSON数据解析错误，请您再试一次", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            else if(msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 0x321) {
                password_edit.setText("");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg:
                Intent intentToCheck = new Intent(this, SignUp.class);
                startActivity(intentToCheck);
                break;
            case R.id.login:
                if(id_edit.getText().length() == 18) {
                    String tempPassword;
                    if(deleteFlag == true) {
                        tempPassword = MD5.stringToMD5(password_edit.getText().toString());
                    }
                    else {
                        tempPassword = password_edit.getText().toString();
                    }
                    HttpUtil.sendHttpRequest(HttpUtil.loginLink + "id=" + id_edit.getText()
                            + "&" + "password=" + tempPassword, new HttpCallbackListener() {
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
                } else {
                    Message msg = new Message();
                    msg.what = 0x456;
                    msg.obj = "请按格式输入您的身份证号！(18位数字)";
                    handler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }

}