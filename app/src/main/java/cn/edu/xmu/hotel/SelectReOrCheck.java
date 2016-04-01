package cn.edu.xmu.hotel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SelectReOrCheck extends Activity implements View.OnClickListener {

    private Button button1 = null;

    private TextView currentOrder = null;

    private TextView historyOrder = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_re_or_check);
        ActivityCollector.addActivity(this);
        button1 = (Button)findViewById(R.id.reserv_select);
        button1.setOnClickListener(this);
        currentOrder = (TextView)findViewById(R.id.currentorder_text);
        currentOrder.setOnClickListener(this);
        historyOrder = (TextView)findViewById(R.id.historyorder_text);
        historyOrder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reserv_select:{
                Intent intentToReserv = new Intent(this, ReservActivity.class);
                startActivity(intentToReserv);
                break;
            }
            case R.id.currentorder_text: {
                Intent intentToCurrentOrder = new Intent(this, CurrentOrderActivity.class);
                startActivity(intentToCurrentOrder);
                break;
            }
            case R.id.historyorder_text: {
                Intent intentToHistoryOrder = new Intent(this, HistoryOrderActivity.class);
                startActivity(intentToHistoryOrder);
                break;
            }
            default:
                break;
        }
    }
}
