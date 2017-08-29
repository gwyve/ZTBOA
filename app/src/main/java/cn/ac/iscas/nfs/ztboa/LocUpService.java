package cn.ac.iscas.nfs.ztboa;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.util.AsyncListUtil;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by VE on 2017/8/21.
 */
public class LocUpService extends Service {

    private final static int GRAY_SERVICE_ID = 1001;

    private boolean serviceRunning = false;

    // 用户id
    private String userID;
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
    //    定位模式选择
    private String locationMode;
    //    上传地址url
    private String locationUrl;

    private Button startBtn;
    private Button stopBtn;
    private TextView textView;
    Bundle bundle;

    //  距离中心位置的距离
    private double distance = Double.MAX_VALUE;
    //    private int count = 0;
    private int gpsCount;
    private int net90Count;
    private int net50Count;
    private boolean locationIsRunning;


    //   定时任务器
//    private TimerHandler timerHandler;
//    private Timer mTimer;
//    private MyTimerTask myTimerTask;

    private LocationService locationService;
    private LocationClientOption mOption;


    private BDLocation lastLocation;

    Intent alarmIntent;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    private int heartCount = 0;

    public LocUpService(){

    }

    public class LocUpBinder extends Binder {
        LocUpService getService(){
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
        bundle = intent.getExtras();
        userID = bundle.getString("user_id");
        interval = bundle.getInt("interval");
        centerLongitude = bundle.getString("center_longitude");
        centerLatitude = bundle.getString("center_latitude");
//        stopRadius = bundle.getInt("stop_radius");
        stopInterval = bundle.getInt("stop_interval");
        locationUrl = bundle.getString("location_url");
        locationMode = bundle.getString("location_mode");

        //        初始化定时
        alarmIntent = new Intent();
        alarmIntent.setAction("cn.ac.iscas.nfs.ztboa");
        pendingIntent  = PendingIntent.getBroadcast(LocUpService.this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter("cn.ac.iscas.nfs.ztboa");
        intentFilter.setPriority(1000);
        registerReceiver(alarmReceiver,intentFilter);

//        mTimer = new Timer();
//        timerHandler = new TimerHandler();

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

        return super.onStartCommand(intent,flags,startId);
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

//        if (myTimerTask != null)
//            myTimerTask.cancel();

    }



    private void startLocation(){
        Log.e("111","4444444444444444444444444444");
        if (lastLocation!=null){
            JSONObject json = createjson(lastLocation.getLocType()+"",lastLocation.getTime(),userID,lastLocation.getLatitude()+"",lastLocation.getLongitude()+"",lastLocation.getRadius()+"");
            sendRequestWithHttpClient(locationUrl,json,lastLocation.getLatitude(),lastLocation.getLongitude());
        }
        locationIsRunning = false;
        locationService.stop();
        lastLocation = null;
        mOption.setScanSpan(interval*1000);
        switch (locationMode){
            case "Hight_Accuracy":
                mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
                locationService.setLocationOption(mOption);
                break;
            case "Battery_Saving":
                mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
                locationService.setLocationOption(mOption);
                break;
            case "Device_Sensors":
                mOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
                locationService.setLocationOption(mOption);
                break;
        }
        Log.e("111","555555555555555555555555555555");
        if (dataCallback!=null)
            dataCallback.dataChanged(getCurrentTime()+"   开启百度定位111");
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
                Log.e("111","66666666666666666");
                if (dataCallback!=null){
                    if (location ==null){
                        dataCallback.dataChanged(getCurrentTime()+"   定位回调：null");
                    } else{
//                        JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                        dataCallback.dataChanged(getCurrentTime()+"   定位回调： "+location.getRadius());
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
                                    sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude());
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
                                    sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude());
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
                            sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude());
                        }
                        if (gpsCount>5){
                            locationIsRunning = false;
                            locationService.stop();
                        }
                    }else {
                        dataCallback.dataChanged("百度的定位不能上网，错误代码"+location.getLocType());
                    }

//                    JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
//                    sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude());
//                                                locationIsRunning = false;
//                            locationService.stop();
                }else if (location.getLocType() == BDLocation.TypeServerError){
                    JSONObject json = createjson(location.getLocType()+"",location.getTime(),userID,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                    sendRequestWithHttpClient(locationUrl,json,location.getLatitude(),location.getLongitude());
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

    private JSONObject createjson(String LocType,String time,String userid,String latitude,String longitude,String radius){
        JSONObject json = new JSONObject();
        try {
            json.put("loc_type",LocType);
            json.put("time",time);
            json.put("userid",userid);
            json.put("latitude",latitude);
            json.put("longitude",longitude);
            json.put("radius",radius);
//          添加时间、网络类型
            json.put("phonetime",getSysTime());
            json.put("nettype",getAPNType(LocUpService.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void sendRequestWithHttpClient(final String url, final JSONObject json, final double latitude, final double longitude){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                try{
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
                    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,3000);

                    StringEntity entity = new StringEntity(json.toString(),"utf-8");
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200){
                        Log.e("111",json.toString());
                        distance = getDistance(latitude,longitude);
                        dataCallback.dataChanged("成功: "+json.toString()+"\ndistance"+distance);
                    }else {
                        dataCallback.dataChanged("NET ERROR:"+response.getStatusLine().getStatusCode()+json.toString());
                    }
                } catch (UnsupportedEncodingException e) {
                    dataCallback.dataChanged("NET ERROR:"+e);
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    dataCallback.dataChanged("NET ERROR:"+e);
                    e.printStackTrace();
                } catch (IOException e) {
                    dataCallback.dataChanged("NET ERROR:"+e);
                    e.printStackTrace();
                } catch (Exception e){
                    dataCallback.dataChanged("NET ERROR"+e);
                    e.printStackTrace();
                } finally {
                    Log.e("111","ppppppppppppppppp");
                    httpClient.getConnectionManager().shutdown();
                }

            }
        }).start();
    }


    private float getDistance(double latitude2,double longitude2){
        float[] res=new float[1];
        double workLatitude = Double.valueOf(centerLatitude).doubleValue();
        double workLongitude = Double.valueOf(centerLongitude).doubleValue();
        Location.distanceBetween(workLatitude, workLongitude, latitude2, longitude2, res);
        return res[0];
    }

    private static String getAPNType(Context context) {
        //结果返回值
        String netType = "nono_connect";
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = "wifi";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //4G
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "4G_LTE";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    && !telephonyManager.isNetworkRoaming()){
                netType = "3G_UMTS";
            } else if(nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    && !telephonyManager.isNetworkRoaming()){
                netType = "3G_HSDPA";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "3G_EVDO_0";
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    && !telephonyManager.isNetworkRoaming()){
                netType = "2G_GPRS";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    && !telephonyManager.isNetworkRoaming()){
                netType = "2G_EDGE";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_CDMA && !telephonyManager.isNetworkRoaming()) {
                netType = "2G_CDMA";
            } else {
                netType = "2G";
            }
        }
        return netType;
    }
    private long getSysTime(){
        return System.currentTimeMillis();
    }
    private String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\")");
        Date curDate =  new Date(System.currentTimeMillis());
        return formatter.format(curDate);
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
                Log.e("111","wwweeeeeeeeeeeeeeeeeeeee");
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),stopInterval*1000,pendingIntent);
            }
        }
    }



    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("cn.ac.iscas.nfs.ztboa")){
                Log.e("111","wwwwwwwwwwwwwaaaaaaaa");

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    heartCount ++;
                    LocUpService.this.startAlarmmanager(2*60);

                    if (heartCount==0){
                        startTask();
                        heartCount = 1;
                    }

                    if (distance<5000 || distance==Double.MAX_VALUE){
                        if (heartCount>3){
                            startTask();
                            heartCount = 0;
                        }
                    }else if (distance<10000){
                        if (heartCount>6){
                            startTask();
                            heartCount = 0;
                        }
                    }else if (distance<15000){
                        if (heartCount>9){
                            startTask();
                            heartCount = 0;
                        }
                    }else if (distance<20000){
                        if (heartCount>12){
                            startTask();
                            heartCount = 0;
                        }
                    }else if (distance<25000){
                        if (heartCount>15){
                            startTask();
                            heartCount = 0;
                        }
                    }else {
                        if (heartCount>15){
                            startTask();
                            heartCount = 0;
                        }
                    }



//                    Log.e("111",""+stopInterval);
//                    LocUpService.this.startAlarmmanager(stopInterval*60);
//                    if (distance<5000 || distance==Double.MAX_VALUE){
//                        LocUpService.this.startAlarmmanager(stopInterval*60);
//                    }else if (distance<10000){
//                        LocUpService.this.startAlarmmanager(10*60);
//                    }else if (distance<15000){
//                        LocUpService.this.startAlarmmanager(15*60);
//                    }else if (distance<20000){
//                        LocUpService.this.startAlarmmanager(20*60);
//                    }else if (distance<25000){
//                        LocUpService.this.startAlarmmanager(25*60);
//                    }else {
//                        LocUpService.this.startAlarmmanager(30*60);
//                    }
//                    Log.e("111",distance+"");
                }

            }else {
                startTask();
            }
        }
    }



}
