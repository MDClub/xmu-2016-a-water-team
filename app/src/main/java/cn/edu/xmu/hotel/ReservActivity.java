package cn.edu.xmu.hotel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReservActivity extends Activity implements View.OnClickListener {

    private DatePicker datePicker1 = null;

    private Button daySelectButton = null;

    private TextView todayText = null;

    private TextView checkinDaySelect = null;

    private TextView leaveDaySelect = null;

    private Calendar calendar = Calendar.getInstance();

    private TextView startDayText=null;

    private TextView endDaytText=null;

    private int y = 0;

    private int m = 0;

    private int d = 0;

    private boolean noonPassFlag = false;

    private int fYear,fMonth,fDay;

    private int sYear,sMonth,sDay;

    private boolean checkinDaySelectFlag = false;

    private boolean endDaySelectFlag = false;

    private String reservLimit = null;

    private int limitYear;

    private int limitMonth;

    private int limitDay;

    private Calendar startCalendar = Calendar.getInstance();

    private Calendar endCalendar = Calendar.getInstance();

    StringBuilder sb1 = new StringBuilder();

    StringBuilder sb2 = new StringBuilder();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123) {
                String s = (String)msg.obj;
                y = Integer.parseInt(s.substring(0, 4));
                m = Integer.parseInt(s.substring(5, 7)) - 1;
                d = Integer.parseInt(s.substring(8, 10));
                reservLimit = s.substring(19, 29);
                limitYear = Integer.parseInt(reservLimit.substring(0, 4));
                limitMonth = Integer.parseInt(reservLimit.substring(5, 7));
                limitDay = Integer.parseInt(reservLimit.substring(8, 10));
                todayText.setText("今天是: " + s.substring(0, 10) + "\n" +
                 "当前提供截止到入住时间为" + reservLimit + "的房间预订服务");
            }
            else if(msg.what == 0x456) {
                Toast.makeText(MyApplication.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        HttpUtil.sendHttpRequest(HttpUtil.timeGetLink, new HttpCallbackListener() {
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserv);
        ActivityCollector.addActivity(this);
        datePicker1=(DatePicker)findViewById(R.id.datePick1);
        checkinDaySelect = (TextView)findViewById(R.id.checkinday_select);
        leaveDaySelect=(TextView)findViewById(R.id.leaveday_select);
        startDayText=(TextView)findViewById(R.id.startdaytext);
        endDaytText=(TextView)findViewById(R.id.enddaytext);
        checkinDaySelect.setOnClickListener(this);
        leaveDaySelect.setOnClickListener(this);
        daySelectButton = (Button)findViewById(R.id.dayselectbutton);
        daySelectButton.setOnClickListener(this);
        todayText = (TextView)findViewById(R.id.todaytext);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dayselectbutton: {
                if(checkinDaySelectFlag == false)
                    Toast.makeText(this, "请选择入住时间！", Toast.LENGTH_SHORT).show();
                else if(endDaySelectFlag == false)
                    Toast.makeText(this, "请选择离店时间！", Toast.LENGTH_SHORT).show();
                else if(judgeTwoDays(sYear, sMonth, sDay, fYear, fMonth, fDay) == false) {
                    Toast.makeText(this, "离店日期必须晚于入住日期！请重新选择！", Toast.LENGTH_SHORT).show();
                }
                else if(judgeTwoDays(y, m, d, fYear, (fMonth - 1), fDay) == true) {
                    Toast.makeText(this, "入住日期不能早于今天！请重新选择！", Toast.LENGTH_SHORT).show();
                }
                else if(judgeTwoDays(sYear, sMonth, sDay, limitYear, limitMonth, limitDay + 1) == true) {
                    StringBuilder sbTemp = new StringBuilder();
                    Toast.makeText(this, "离店日期最晚只能选择截止入住时间的第二天！请重新选择！", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intentToAvaRoom = new Intent(this, AvaRoomActivity.class);
                    startCalendar.set(fYear, fMonth - 1, fDay);
                    endCalendar.set(sYear, sMonth - 1, sDay);
                    long val = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
                    long day = val / (1000 * 60 * 60 * 24);
                    int days = (int)day;
                    intentToAvaRoom.putExtra("startDay", sb1.toString());
                    intentToAvaRoom.putExtra("endDay", sb2.toString());
                    intentToAvaRoom.putExtra("days", days);
                    startActivity(intentToAvaRoom);
                }
                break;
            }
            case R.id.checkinday_select: {
                fYear = datePicker1.getYear();
                fMonth = datePicker1.getMonth() + 1;
                fDay = datePicker1.getDayOfMonth();
                sb1 = new StringBuilder();
                String s1 = String.valueOf(fYear);
                sb1.append(s1 + "-");
                s1 = String.valueOf(fMonth);
                sb1.append(s1 + "-");
                s1 = String.valueOf(fDay);
                sb1.append(s1);
                checkinDaySelectFlag = true;
                startDayText.setText("入住时间:" + "\n" + sb1 + "\n" + "\n");
                checkinDaySelect.setText("修改入住时间");
                break;
            }
            case R.id.leaveday_select: {
                sYear = datePicker1.getYear();
                sMonth = datePicker1.getMonth() + 1;
                sDay = datePicker1.getDayOfMonth();
                sb2 = new StringBuilder();
                String s2 = String.valueOf(sYear);
                sb2.append(s2 + "-");
                s2 = String.valueOf(sMonth);
                sb2.append(s2 + "-");
                s2 = String.valueOf(sDay);
                sb2.append(s2);
                endDaytText.setText("离店时间:" + "\n" + sb2 + "\n" + "\n");
                leaveDaySelect.setText("修改离店时间");
                endDaySelectFlag = true;
                break;
            }
            default:
                break;
        }
    }

    //if firstday > secondday,return true; firstday <= secondday return false;
    public boolean judgeTwoDays(int fYear, int fMonth, int fDay, int sYear, int sMonth, int sDay) {
        if(fYear >= sYear) {
            if(fYear > sYear) {
                return true;
            } else if(fMonth >= sMonth) {
                if(fMonth > sMonth) {
                    return true;
                } else if(fDay >= sDay) {
                    if(fDay > sDay) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}