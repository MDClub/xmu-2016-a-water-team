package cn.edu.xmu.hotel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectReOrCheck extends Activity implements View.OnClickListener {

    private Button button1 = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_re_or_check);
        ActivityCollector.addActivity(this);
        button1 = (Button)findViewById(R.id.reserv_select);
        button1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reserv_select:{
                Intent intentToReserv = new Intent(this, ReservActivity.class);
                startActivity(intentToReserv);
            }
        }
    }
}
