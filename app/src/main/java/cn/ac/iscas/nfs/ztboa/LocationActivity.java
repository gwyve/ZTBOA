package cn.ac.iscas.nfs.ztboa;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.location.service.LocationService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity {


    // 用户id
    private String userID;
    // 每次上传间隔时间 单位：秒
    private int interval;
    // 工作区域中心坐标
    private String centerLongitude;
    private String centerLatitude;
    // 本次停止上传的最小半径
    private int stopRadius;
    // 本次停止上传的定位方式
    private int stopLocationType;
    //    本次停止上传后，与下次上传间隔
    private int stopInterval;
    //    定位模式选择
    private String locationMode;
    //    上传地址url
    private String locationUrl;

    private Button startBtn;
    private Button stopBtn;
    private TextView textView;
    Bundle bundle;

//  距离中心位置的距离
    private double distance = Long.MAX_VALUE;
    private int count = 0;

//  服务绑定
    LocUpServiceConn locUpServiceConn;
    LocUpService.LocUpBinder binder = null;

    Intent serviceIntent;

    Intent alarmIntent;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa");
        intentFilter.setPriority(1000);
        registerReceiver(alarmReceiver,intentFilter);


        startBtn = (Button)findViewById(R.id.locationActStartBtn);
        stopBtn = (Button)findViewById(R.id.locationActStopBtn);
        textView = (TextView)findViewById(R.id.locationActText);
        bundle = getIntent().getExtras();


        stopInterval = bundle.getInt("stop_interval");

//        初始化定时
        alarmIntent = new Intent();
        alarmIntent.setAction("cn.ac.iscas.nfs.ztboa");
        pendingIntent  = PendingIntent.getBroadcast(LocationActivity.this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);




        locUpServiceConn = new LocUpServiceConn();

        serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
        serviceIntent.putExtras(bundle);
        stopBtn.setEnabled(false);
        final Intent finalServiceIntent = serviceIntent;
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);

//                finalServiceIntent.putExtras(bundle);
//                startService(finalServiceIntent);
//                LocationActivity.this.bindService(finalServiceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
                Log.e("111","wwwwwwwwwwwwwwwwwffff");
                startAlarmmanager(0);


//                Intent intent = new Intent();
//                intent.setAction("cn.ac.iscas.nfs.ztboa");
//
//                LocationActivity.this.sendBroadcast(intent);

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
                Intent serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
                stopService(serviceIntent);
//                textView.setText("请点击‘开始上传’按钮");

//                if (binder != null){
//                    LocationActivity.this.unbindService(locUpServiceConn);
//                }


                stopAlarmmanager();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startAlarmmanager(long stopInterval){
        if (alarmManager!=null && pendingIntent!=null){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,0,pendingIntent);
            }else {
                Log.e("111","wwweeeeeeeeeeeeeeeeeeeee");
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),stopInterval*1000,pendingIntent);
            }
        }
    }

    private void stopAlarmmanager(){
        alarmManager.cancel(pendingIntent);
        if (binder != null){
            LocationActivity.this.unbindService(locUpServiceConn);
        }
    }

    public class AlarmReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("cn.ac.iscas.nfs.ztboa")){
                Log.e("111","wwwwwwwwwwwwwaaaaaaaa");

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                        Log.e("111",""+stopInterval);
                        if (serviceIntent!=null && locUpServiceConn!=null){
                            startService(serviceIntent);
                            LocationActivity.this.bindService(serviceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
                        }
                        LocationActivity.this.startAlarmmanager(stopInterval*60);
                }else {
                    if (serviceIntent!=null && locUpServiceConn!=null){
                        startService(serviceIntent);
                        LocationActivity.this.bindService(serviceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
                    }
                }
            }
        }
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
                textView.setText(msg.getData().getString("str")+"\n"+textView.getText().toString());
            };
        };

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }


    }






}
