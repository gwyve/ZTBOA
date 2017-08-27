package cn.ac.iscas.nfs.ztboa;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

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

public class ForDemoService extends Service {

    private LocationService locationService;
    private LocationClientOption mOption;
    private TimerHandler timerHandler;

    private final static int GRAY_SERVICE_ID = 1001;


    private String userid;
    private String ip;
    private int interval;
    private Timer mTimer;

    private String locationMode;

    public ForDemoService() {
    }



    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Bundle bundle = intent.getExtras();
        userid = bundle.getString("userid");
        ip = bundle.getString("ip");
//        port = bundle.getString("port");
        interval = bundle.getInt("interval");
        locationMode = bundle.getString("LocationMode");

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




        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else {
            Intent intent1 = new Intent(this, GrayInnerService.class);
            startService(intent1);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        locationService.start();

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



    private void startTimer(){
        timerHandler = new TimerHandler();
//        Timer timer = new Timer();
//        timer.schedule(new MyTimerTask(),0,interval*1000);

        if (mTimer != null){
//            mTimer.cancel();
        }else {
            mTimer = new Timer();
            mTimer.schedule(new MyTimerTask(),0,interval*1000);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();

        // -----------location config ------------
        locationService = ((ZTBApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);

        mOption = new LocationClientOption();
        mOption = locationService.getDefaultLocationClientOption();

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
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                JSONObject json = createjson(location.getLocType()+"",location.getTime(),userid,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                Log.e("222","wwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                sendRequestWithHttpClient(ip,json);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }


    private void sendRequestWithHttpClient(final String ip, final JSONObject json){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    Log.e("111",ip);
                    HttpPost httpPost = new HttpPost(ip);
                    StringEntity entity = new StringEntity(json.toString(),"utf-8");

                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    httpPost.setEntity(entity);

                    HttpResponse response = httpClient.execute(httpPost);
                    Log.e("111",json.toString()+"wwwwwwwwwwwww"+interval);
//                    if (response.getStatusLine().getStatusCode() == 200){
//                        Log.e("111","wwwwwwww"+json.toString());
//                        Log.e("111","成功");
//                    }else {
//                        Log.e("111",json.toString());
//                        Log.e("111","失败");
//                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timerHandler.sendEmptyMessage(0);
        }
    }
    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            locationService.start();
            locationService.stop();
        }
    }
}
