package cn.ac.iscas.nfs.ztboa.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.Utils.NetUtil;
import cn.ac.iscas.nfs.ztboa.Utils.Utils;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;
//import cn.ac.iscas.nfs.ztboa.service.LocUpService;

public class FakeActivity extends AppCompatActivity {

    private Button daoBtn;
    private Button tuiBtn;
    private TextView textView;

    private SharedPreferences sharedPreferences;

    private int userId;

    NetUtil netUtil;
    private String locationUrl;
    private Random random;

    //    公司id
    private int companyID;

// 定时相关
    private  int daoCount;
    private Timer daoTimer ;
    private TimerTask daoTimerTask ;
    private Handler daoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //收到消息后显示当前时间
            daoCount --;
            if (daoCount>0){
                textView.setText("到后"+daoCount+"秒");
                Log.e("111","到后"+daoCount+"秒");
            }else {
                daoBtn.setClickable(true);
                tuiBtn.setClickable(true);
                if (daoTimer != null){
                    daoTimer.cancel();
                    daoTimer = null;
                }
                if (daoTimerTask != null){
                    daoTimerTask.cancel();
                    daoTimerTask = null;
                }
                signIn();
                textView.setText("签到成功");
            }
        }
    };


    private  int tuiCount;
    private Timer tuiTimer ;
    private TimerTask tuiTimerTask ;
    private Handler tuiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //收到消息后显示当前时间
            tuiCount --;
            if (tuiCount>0){
                textView.setText("退后"+tuiCount+"秒");
                Log.e("111","退后"+tuiCount+"秒");
            }else {
                daoBtn.setClickable(true);
                tuiBtn.setClickable(true);

                if (tuiTimer != null){
                    tuiTimer.cancel();
                    tuiTimer = null;
                }
                if (tuiTimerTask != null){
                    tuiTimerTask.cancel();
                    tuiTimerTask = null;
                }
                signOut();
                textView.setText("签退成功");
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake);
        daoBtn = (Button)findViewById(R.id.fakeActDaoBtn);
        tuiBtn = (Button)findViewById(R.id.fakeActTuiBtn);
        textView = (TextView)findViewById(R.id.fakeActTextView);


        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa", Context.MODE_WORLD_WRITEABLE);
        netUtil = ((ZTBApplication)getApplication()).netUtil;
        userId = sharedPreferences.getInt("user_id",0);
        companyID = sharedPreferences.getInt("company_id",1);
        locationUrl = sharedPreferences.getString("location_url","http://iscas-ztb-weixin03.wisvision.cn/app/upload/zippos");

        random = new Random();

        daoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                daoBtn.setClickable(false);
                tuiBtn.setClickable(false);
                daoCount = 500+getRandom(150);
//                daoCount = 10;
                if (daoTimer == null){
                    daoTimer = new Timer();
                }
                if (daoTimerTask == null){
                    daoTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            daoHandler.sendEmptyMessage(0);
                        }
                    };
                }
                daoTimer.schedule(daoTimerTask, 1000, 1000);
            }
        });


        tuiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                daoBtn.setClickable(false);
                tuiBtn.setClickable(false);
                tuiCount = 500+getRandom(150);
//                tuiCount = 10;
                if (tuiTimer == null){
                    tuiTimer = new Timer();
                }
                if (tuiTimerTask == null){
                    tuiTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            tuiHandler.sendEmptyMessage(0);
                        }
                    };
                }
                tuiTimer.schedule(tuiTimerTask, 1000, 1000);
            }
        });
    }

    private void signIn(){
        JSONObject json = createjson("61",Utils.getCurrentTime(),userId,createInLatitude()+"",createInLongitude()+"",getRadius()+"","2");
        byte[] bytes = Utils.locatonEncode((short) 61,Utils.getTimeMillis(Utils.getCurrentTime())+getRandom(100),userId,createInLatitude(),createInLongitude(),
                (short)getRadius(),Utils.getSysTime(),(short)2,(short)companyID);
        Log.e("111",json.toString());
        netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes);
    }
    private void signOut(){
        JSONObject json = createjson("61",Utils.getCurrentTime(),userId,createOutLatitude()+"",createOutLongitude()+"",getRadius()+"","2");
        byte[] bytes = Utils.locatonEncode((short) 61,Utils.getTimeMillis(Utils.getCurrentTime())+getRandom(100),userId,createOutLatitude(),createOutLongitude(),
                (short)getRadius(),Utils.getSysTime(),(short)2,(short)companyID);
        Log.e("111",json.toString());
        netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes);
    }

    private float getRadius(){
        int randomInt = getRandom(100);
        while (randomInt<25){
            randomInt = getRandom(100);
        }
        float ret = randomInt/10;
        return ret;
//        DecimalFormat decimalFormat=new DecimalFormat(".0");
//        return decimalFormat.format(ret);
    }

    private double createInLatitude(){
        int randomInt = getRandom(1000);
        double base = 39.985251;
        double ret =  base + 0.000700 * randomInt/1000;
        return ret;
    }
    private double createInLongitude(){
        int randomInt = getRandom(1000);
        double base = 116.343045;
        double ret =  base + 0.000700 * randomInt/1000;
        return ret;
    }
    private double createOutLatitude(){
        int randomInt = getRandom(1000);
        double base = 39.989585;
        double ret =  base + 0.000700 * randomInt/1000;
        return ret;
    }
    private double createOutLongitude(){
        int randomInt = getRandom(1000);
        double base = 116.341423;
        double ret =  base + 0.000700 * randomInt/1000;
        return ret;
    }

    private int getRandom(int tmp){
        return random.nextInt()%tmp;
    }



    private JSONObject createjson(String LocType, String time, int userid, String latitude, String longitude, String radius,String netType){
        JSONObject json = new JSONObject();
        try {
            json.put("loc_type",LocType);
            json.put("time",time);
            json.put("userid",userid);
            json.put("latitude",latitude);
            json.put("longitude",longitude);
            json.put("radius",radius);
//          添加时间、网络类型
            json.put("phonetime", Utils.getSysTime());
            json.put("nettype",netType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
