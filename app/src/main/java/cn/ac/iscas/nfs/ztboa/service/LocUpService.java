package cn.ac.iscas.nfs.ztboa.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.location.service.LocationService;
import com.tencent.android.tpush.XGPushBaseReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.ac.iscas.nfs.ztboa.Utils.ConfigInfo;
import cn.ac.iscas.nfs.ztboa.Utils.NetUtil;
import cn.ac.iscas.nfs.ztboa.Utils.Utils;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;
import cn.ac.iscas.nfs.ztboa.pushUtils.XGPushReceiver;

/**
 * Created by VE on 2017/8/21.
 */
public class LocUpService extends Service {

    private final static int GRAY_SERVICE_ID = 1001;

    private boolean serviceRunning = false;

    // 用户id
    private int userID;
    // 每次上传间隔时间 单位：秒
    private int interval;
    // 工作区域中心坐标
    private String centerLongitude;
    private String centerLatitude;
    // 本次停止上传的最小半径
//    private int stopRadius;
    // 本次停止上传的定位方式
    private int stopLocationType;
    //    本次停止上传后，与下次上传间隔
    private int stopInterval;

    //    上传地址url
    private String locationUrl;
//  时间段
    Date begin1;
    Date end1;
    Date begin2;
    Date end2;

    private Button startBtn;
    private Button stopBtn;
    private TextView textView;
    Bundle bundle;

    //  距离中心位置的距离
    private double distance = Double.MAX_VALUE;

    private int gpsCount;
    private int net90Count;
    private int net50Count;
    private boolean locationIsRunning;

    private LocationService locationService;
    private LocationClientOption mOption;


    private BDLocation lastLocation;

    Intent alarmIntent;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    private int heartCount;

    NetUtil netUtil;



    public LocUpService(){

    }

    public class LocUpBinder extends Binder {
        public LocUpService getService(){
            return LocUpService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startAlarmmanager(0);
        return new LocUpBinder();
    }


    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        netUtil = ((ZTBApplication)getApplication()).netUtil;


        ConfigInfo configInfo = ((ZTBApplication)getApplication()).configInfo;
        userID = configInfo.getUserID();
        interval = configInfo.getInterval();
        centerLongitude = configInfo.getCenterLongitude();
        centerLatitude = configInfo.getCenterLatitude();
        stopInterval = configInfo.getStopInterval();
        locationUrl = configInfo.getLocationUrl();
        begin1 = configInfo.getBegin1();
        end1 = configInfo.getEnd1();
        begin2 = configInfo.getBegin2();
        end2 = configInfo.getEnd2();


//        设置信鸽
        XGPushReceiver.setContext(this);
        XGReceiver xgReceiver = new XGReceiver();
        IntentFilter xgintentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa.xgpush");
        xgintentFilter.setPriority(1000);
        registerReceiver(xgReceiver,xgintentFilter);


        //        初始化定时
        alarmIntent = new Intent();
        alarmIntent.setAction("cn.ac.iscas.nfs.ztboa");
        pendingIntent  = PendingIntent.getBroadcast(LocUpService.this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa");
        intentFilter.setPriority(1000);
        registerReceiver(alarmReceiver,intentFilter);

        locationService = ((ZTBApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        mOption = locationService.getDefaultLocationClientOption();

        heartCount = -1;
        startAlarmmanager(0);

        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else {
            Intent intent1 = new Intent(this, GrayInnerService.class);
            startService(intent1);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

//        return super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    //给API >= 18 的平台上做灰色保护手段
    public class GrayInnerService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();

            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        // MediaPlayer对象的stop()方法
        stopTask();
        stopAlarmmanager();
        return super.onUnbind(intent);
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        // MediaPlayer对象的stop()方法
        stopAlarmmanager();
        super.onDestroy();
    }


    private synchronized void  startTask(){

        gpsCount = 0;
        net90Count = 0;
        net50Count = 0;
        startLocation();

    }
    private void stopTask(){
        locationIsRunning = false;
        lastLocation = null;
        locationService.stop();
    }



    private void startLocation(){
        if (lastLocation!=null){
            JSONObject json = createjson(lastLocation.getLocType()+"",lastLocation.getTime(),userID,lastLocation.getLatitude()+"",lastLocation.getLongitude()+"",lastLocation.getRadius()+"");
//            netUtil.sendRequestWithHttpClient(locationUrl,json,lastLocation.getLatitude(),lastLocation.getLongitude(),dataCallback);
            byte[] bytes = Utils.locatonEncode((short) lastLocation.getLocType(),Utils.getTimeMillis(lastLocation.getTime()),userID,lastLocation.getLatitude(),lastLocation.getLongitude(),(short)lastLocation.getRadius(),Utils.getSysTime(),Utils.getAPNType(LocUpService.this));
            netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes,lastLocation.getLatitude(),lastLocation.getLongitude(),dataCallback);
        }
        locationIsRunning = false;
        locationService.stop();
        lastLocation = null;
        mOption.setScanSpan(interval*1000);
        if (dataCallback!=null)
            dataCallback.dataChanged(Utils.getCurrentTime()+"   开启百度定位111");
        locationIsRunning = true;
        locationService.start();
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            synchronized (this){
                if (dataCallback!=null){
                    if (location ==null){
                        dataCallback.dataChanged(Utils.getCurrentTime()+"   定位回调：null");
                    } else{
//                        JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                        dataCallback.dataChanged(Utils.getCurrentTime()+"   定位回调： "+location.getRadius());
                    }
                }
                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    Log.e("111",location.getLocType()+""+location.getTime()+userID+location.getLatitude()+"  "+location.getLongitude()+"  "+location.getRadius()+"  "+" net90Count:"+ net90Count+" net50Count:"+net50Count+" gpsCount:"+gpsCount);
                    if (location.getLocType()==161){
                        if (location.getRadius()<90 && location.getRadius()>=50){
                            net90Count++;
                            if (net90Count<=5){
                                if (null==lastLocation || !lastLocation.getTime().equals(location.getTime())){
                                    JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
//                                    netUtil.sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude(),dataCallback);

                                    byte[] bytes = Utils.locatonEncode((short) location.getLocType(),Utils.getTimeMillis(location.getTime()),userID,location.getLatitude(),location.getLongitude(),(short)location.getRadius(),Utils.getSysTime(),Utils.getAPNType(LocUpService.this));
                                    netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes,location.getLatitude(),location.getLongitude(),dataCallback);
                                }
                            }
                            if (net90Count>12){
                                locationIsRunning = false;
                                locationService.stop();
                            }
                        }else if (location.getRadius()<50){
                            net50Count++;
                            if (net50Count<=5){
                                if (null==lastLocation || !lastLocation.getTime().equals(location.getTime())){
                                    JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
//                                    netUtil.sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude(),dataCallback);
                                    byte[] bytes = Utils.locatonEncode((short) location.getLocType(),Utils.getTimeMillis(location.getTime()),userID,location.getLatitude(),location.getLongitude(),(short)location.getRadius(),Utils.getSysTime(),Utils.getAPNType(LocUpService.this));
                                    netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes,location.getLatitude(),location.getLongitude(),dataCallback);
                                }

                            }
                            if (net50Count>12){
                                locationIsRunning = false;
                                locationService.stop();
                            }
                        }
                    }else if (location.getLocType()==61 && location.getRadius()<30){
                        gpsCount++;
                        if (null==lastLocation || !lastLocation.getTime().equals(location.getTime())){
                            JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
//                            netUtil.sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude(),dataCallback);
                            byte[] bytes = Utils.locatonEncode((short) location.getLocType(),Utils.getTimeMillis(location.getTime()),userID,location.getLatitude(),location.getLongitude(),(short)location.getRadius(),Utils.getSysTime(),Utils.getAPNType(LocUpService.this));
                            netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes,location.getLatitude(),location.getLongitude(),dataCallback);
                        }
                        if (gpsCount>5){
                            locationIsRunning = false;
                            locationService.stop();
                        }
                    }else {
                        dataCallback.dataChanged("百度的定位不能上网，错误代码"+location.getLocType());
                    }


                }else if (location.getLocType() == BDLocation.TypeServerError){
                    JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
//                    netUtil.sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude(),dataCallback);
                    byte[] bytes = Utils.locatonEncode((short) location.getLocType(),Utils.getTimeMillis(location.getTime()),userID,location.getLatitude(),location.getLongitude(),(short)location.getRadius(),Utils.getSysTime(),Utils.getAPNType(LocUpService.this));
                    netUtil.sendRequestWithHttpClient42Bytes(locationUrl,json,bytes,location.getLatitude(),location.getLongitude(),dataCallback);
                }
                if (locationIsRunning){
                    lastLocation = location;
                }else {
                    lastLocation = null;
                }

            }
        }
        public void onConnectHotSpotMessage(String s, int i){
        }
    };

    private JSONObject createjson(String LocType,String time,int userid,String latitude,String longitude,String radius){
        JSONObject json = new JSONObject();
        try {
            json.put("loc_type",LocType);
            json.put("time",time);
            json.put("userid",userid);
            json.put("latitude",latitude);
            json.put("longitude",longitude);
            json.put("radius",radius);
//          添加时间、网络类型
            json.put("phonetime",Utils.getSysTime());
            json.put("nettype",Utils.getAPNType(LocUpService.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }







    //    服务相关
    DataCallback dataCallback = null;
    public DataCallback getDataCallback(){
        return dataCallback;
    }
    public void setDataCallback(DataCallback dataCallback){
        this.dataCallback = dataCallback;
    }
    public interface DataCallback{
        void dataChanged(String str);
    }


    private void stopAlarmmanager(){
        alarmManager.cancel(pendingIntent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startAlarmmanager(long stopInterval){
        if (alarmManager!=null && pendingIntent!=null){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,0,pendingIntent);
//                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,pendingIntent);
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,pendingIntent);
            }else {
                alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,pendingIntent);
            }
        }
    }



    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("cn.ac.iscas.nfs.ztboa")) {
                Date curDate = Utils.getCurrentHourMinute();


                heartCount++;
                LocUpService.this.startAlarmmanager(2 * 60);
                Log.e("111","kkkkkkkkkkkkkk"+heartCount);


                if ((curDate.getTime() > begin1.getTime() && curDate.getTime() < end1.getTime()) || (curDate.getTime() > begin2.getTime() && curDate.getTime() < end2.getTime())) {
//                if (true){
                    if (heartCount == 0) {
                        startTask();
                        heartCount = 1;
                    }

                    if (distance < 5000 || distance == Double.MAX_VALUE) {
                        if (heartCount > 3) {
                            startTask();
                            heartCount = 0;
                        }
                    } else if (distance < 10000) {
                        if (heartCount > 6) {
                            startTask();
                            heartCount = 0;
                        }
                    } else if (distance < 15000) {
                        if (heartCount > 9) {
                            startTask();
                            heartCount = 0;
                        }
                    } else if (distance < 20000) {
                        if (heartCount > 12) {
                            startTask();
                            heartCount = 0;
                        }
                    } else if (distance < 25000) {
                        if (heartCount > 15) {
                            startTask();
                            heartCount = 0;
                        }
                    } else {
                        if (heartCount > 15) {
                            startTask();
                            heartCount = 0;
                        }
                    }
                }else {
                    if (dataCallback!=null)
                        dataCallback.dataChanged(Utils.getCurrentTime()+": 单纯就是打一巴掌");
                }

            }else {

            }

        }
    }


    public class XGReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("cn.ac.iscas.nfs.ztboa.xgpush")) {
                Log.e("111","ttttttttttttttttt");
                heartCount = -1;
                Intent intent1 = new Intent();
                intent1.setAction("cn.ac.iscas.nfs.ztboa");
                LocUpService.this.sendBroadcast(intent1);
            }
        }
    }


}
