package cn.ac.iscas.nfs.ztboa.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;
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

    //    屏幕尺寸
    private int width;
    private int height;

    private ViewGroup rootView;
    private ImageButton settingButton;
    private ImageView background;


    private WebView webView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    ZTBApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_location);

//        AlarmReceiver alarmReceiver = new AlarmReceiver();
//        IntentFilter intentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa");
//        intentFilter.setPriority(1000);
//        registerReceiver(alarmReceiver,intentFilter);

        application = (ZTBApplication)getApplication();

        startBtn = (Button)findViewById(R.id.locationActStartBtn);
        stopBtn = (Button)findViewById(R.id.locationActStopBtn);
        textView = (TextView)findViewById(R.id.locationActText);
        webView = (WebView)findViewById(R.id.locationActWebView);


        locUpServiceConn = new LocUpServiceConn();
        serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
        stopBtn.setEnabled(false);
        final Intent finalServiceIntent = serviceIntent;

        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("auto_clock",true)){
            startService(finalServiceIntent);
            LocationActivity.this.bindService(finalServiceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
        }
//        startBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startBtn.setEnabled(false);
//                stopBtn.setEnabled(true);
//
//                startService(finalServiceIntent);
//                LocationActivity.this.bindService(finalServiceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
//
//            }
//        });
//
//        stopBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopBtn.setEnabled(false);
//                startBtn.setEnabled(true);
//                Intent serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
//                stopService(serviceIntent);
//
//                if (binder != null){
//                    LocationActivity.this.unbindService(locUpServiceConn);
//                }
//
//            }
//        });

//      获得长宽
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        width = rect.width();
        height = rect.height();

        rootView = (ViewGroup)findViewById(R.id.locationActRootView);
        background = (ImageView)findViewById(R.id.locationActBackground);
        settingButton = (ImageButton)findViewById(R.id.locationActSettingBtn);

        initView(LocationActivity.this);
    }

    @Override
    public void onStart(){
        super.onStart();
        boolean confirmSetting = application.confirmSetting;
        boolean auto_clock = sharedPreferences.getBoolean("auto_clock",true);
        if (confirmSetting){
            webView.loadUrl("http://iscas-ztb-weixin03.wisvision.cn/app/autoatt/record/"+sharedPreferences.getInt("company_id",1)+"/"+sharedPreferences.getInt("user_id",0));
//            webView.loadUrl("http://www.baidu.com");
            webView.onResume();

            Toast.makeText(LocationActivity.this,"设置成功",Toast.LENGTH_LONG).show();

            Intent serviceIntent = new Intent(LocationActivity.this,LocUpService.class);
            stopService(serviceIntent);
//            if (binder != null){
//                LocationActivity.this.unbindService(locUpServiceConn);
//            }

            if (auto_clock){
                startService(serviceIntent);
                LocationActivity.this.bindService(serviceIntent,locUpServiceConn, Context.BIND_ABOVE_CLIENT);
            }
        }
        application.confirmSetting = false;
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

    private void initView(Context context){
//        背景设置
        background.getLayoutParams().height = height;
        background.getLayoutParams().width = width;
        Picasso.with(context).load(R.drawable.location_act_back).fit().into(background);

//        设置键
        RelativeLayout.LayoutParams settingBtnParams = (RelativeLayout.LayoutParams)settingButton.getLayoutParams();
        settingButton.setPadding(0,0,0,0);
        settingButton.getLayoutParams().width = width*65/750;
        settingButton.getLayoutParams().height = height*65/1300;
        settingBtnParams.setMargins(width*675/750,height*10/1300,0,0);
        Picasso.with(context).load(R.drawable.location_act_setting_btn).fit().into(settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocationActivity.this,ConfigureActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.configure_act_right_in,R.anim.location_act_left_out);
            }
        });

//     设置webview
        webView.setPadding(0,0,0,0);
        RelativeLayout.LayoutParams webviewParams = (RelativeLayout.LayoutParams)webView.getLayoutParams();
        webviewParams.setMargins(0,height*87/1300,0,0);
//        webview设置
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(false);
        webSettings.setDatabaseEnabled(false);
        webSettings.setAppCacheEnabled(false);



        webView.loadUrl("http://iscas-ztb-weixin03.wisvision.cn/app/autoatt/record/"+sharedPreferences.getInt("company_id",1)+"/"+sharedPreferences.getInt("user_id",0));
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            exit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    private long mExitTime;
    private void exit(){
        if((System.currentTimeMillis() - mExitTime) > 2000){
            Toast.makeText(LocationActivity.this,"再按一次退出",Toast.LENGTH_LONG).show();
            mExitTime = System.currentTimeMillis();
        }else {
            application.destoryActivity("ConfigureActivity");
            LocationActivity.this.finish();
        }
    }



}
