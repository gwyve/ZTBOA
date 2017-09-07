package cn.ac.iscas.nfs.ztboa.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.service.LocUpService;

public class LocationActivity extends AppCompatActivity {


    private Button startBtn;
    private Button stopBtn;
    private TextView textView;


//  服务绑定
    LocUpServiceConn locUpServiceConn;
    LocUpService.LocUpBinder binder = null;

    Intent serviceIntent;

//    Intent alarmIntent;
//    AlarmManager alarmManager;
//    PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

//        AlarmReceiver alarmReceiver = new AlarmReceiver();
//        IntentFilter intentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa");
//        intentFilter.setPriority(1000);
//        registerReceiver(alarmReceiver,intentFilter);


        startBtn = (Button)findViewById(R.id.locationActStartBtn);
        stopBtn = (Button)findViewById(R.id.locationActStopBtn);
        textView = (TextView)findViewById(R.id.locationActText);






        locUpServiceConn = new LocUpServiceConn();

        serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
        stopBtn.setEnabled(false);
        final Intent finalServiceIntent = serviceIntent;
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);

                startService(finalServiceIntent);
                LocationActivity.this.bindService(finalServiceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
                Log.e("111","wwwwwwwwwwwwwwwwwffff");

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
                Intent serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
                stopService(serviceIntent);

                if (binder != null){
                    LocationActivity.this.unbindService(locUpServiceConn);
                }

            }
        });

    }



    class LocUpServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (LocUpService.LocUpBinder) service;
            binder.getService().setDataCallback(new LocUpService.DataCallback() {
                @Override
                public void dataChanged(String str) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("str", str);
                    msg.setData(bundle);
                    //发送通知
                    handler.sendMessage(msg);
                }
            });
        }

        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
            //在handler中更新UI
                if (textView.getText().toString().split("\n").length>200){
                    textView.setText(msg.getData().getString("str"));
                }else {
                    textView.setText(msg.getData().getString("str")+"\n\n"+textView.getText().toString());
                }
            };
        };

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }


    }






}
