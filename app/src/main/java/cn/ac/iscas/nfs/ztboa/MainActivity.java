package cn.ac.iscas.nfs.ztboa;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.location.service.LocationService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Bundle bundle;

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
//    private int stopLocationType;
    //    本次停止上传后，与下次上传间隔
    private int stopInterval;
    //    定位模式选择
    private String locationMode;
    //    上传地址url
    private String locationUrl;





    private LocationService locationService;
    private TextView LocationResult;
    private Button startLocation;
    private EditText workLatitudeEditText;
    private EditText workLongitudeEditText;
    private EditText workRadiusEditText;

    private int workRadius;





    private String userid;
    private String ip;

    private String preTime;

    private LocationClientOption mOption;



//    private void init(){
//        userID = bundle.getString("user_id","0");
//        interval = bundle.getInt("interval",2);
//        centerLatitude = bundle.getString("center_longitude","116.343789");
//        centerLatitude = bundle.getString("center_latitude","39.985749");
//        stopRadius = bundle.getInt("stop_radius",15);
////        startLocation = bundle.getInt("stop_location_type",);
//        stopInterval = bundle.getInt("stop_interval",15);
//        locationUrl = bundle.getString("location_url","");
//        locationMode = bundle.getString("location_mode","Hight_Accuracy");
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bundle = this.getIntent().getExtras();
//        init();


        LocationResult = (TextView) findViewById(R.id.mainActShowText);
        LocationResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        startLocation = (Button) findViewById(R.id.button);
        startLocation.setText("开始定位");
        workLatitudeEditText = (EditText)findViewById(R.id.workLatitudeEditText);
        workLongitudeEditText = (EditText)findViewById(R.id.workLongitudeEditText);
        workRadiusEditText = (EditText)findViewById(R.id.workRadiusEditText);


        Bundle bundle = getIntent().getExtras();
        userid = bundle.getString("userid");
        ip = bundle.getString("ip");



    }



    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(String str) {
        final String s = str;
        try {
            if (LocationResult != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LocationResult.post(new Runnable() {
                            @Override
                            public void run() {
                                LocationResult.setText(LocationResult.getText().toString()+"\n"+s);
                            }
                        });

                    }
                }).start();
            }
            //LocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // -----------location config ------------
        locationService = ((ZTBApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);

        mOption = new LocationClientOption();
        mOption = locationService.getDefaultLocationClientOption();
//        mOption.setOpenAutoNotifyMode();

        locationService.setLocationOption(mOption);


        startLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (startLocation.getText().toString().equals(getString(R.string.startlocation))) {
                    startLocation.setEnabled(false);
//                    LocationResult.setText("定位中......");
//                    locationService.start();// 定位SDK
//                    // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
                    startLocation.setText(getString(R.string.stoplocation));

                    locationService.start();

                } else {
                    LocationResult.setText("定位吧~");
                    locationService.stop();
                    startLocation.setText(getString(R.string.startlocation));
                }



            }
        });

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
            Log.e("111",location.getTime());
            if (null != location && location.getLocType() != BDLocation.TypeServerError ) {

                JSONObject json = createjson(location.getLocType()+"",location.getTime(),userid,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                sendRequestWithHttpClient(ip,json);
                preTime = location.getTime();
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\n定位类型 : ");// 定位类型
                sb.append(location.getLocType());
//                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
//                sb.append(location.getLocTypeDescription());
                sb.append("\n纬度 : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\n经度 : ");// 经度
                sb.append(location.getLongitude());
                float realDistance = getDistance(location.getLatitude(),location.getLongitude());
                if (isInRegion(realDistance,location.getRadius())){
                    sb.append("\n 恭喜你~~~在办公区域内");
                }else {
                    sb.append("\n 我并不知道你在哪里，但是，至少不在工作区域内");
                }

                logMsg(sb.toString());
                startLocation.setClickable(true);
            }
        }

        public void onConnectHotSpotMessage(String s, int i){
        }
    };

    private float getDistance(double latitude2,double longitude2){
        float[] res=new float[1];
        double workLatitude = Double.valueOf(String.valueOf(workLatitudeEditText.getText())).doubleValue();
        double workLongitude = Double.valueOf(String.valueOf(workLongitudeEditText.getText())).doubleValue();
        Location.distanceBetween(workLatitude, workLongitude, latitude2, longitude2, res);
        return res[0];
    }
    private boolean isInRegion(float realDistance,float radius){
        workRadius = Integer.parseInt(workRadiusEditText.getText().toString());
        if (realDistance < workRadius + radius)
            return true;
        else
            return false;
    }


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
                    if (response.getStatusLine().getStatusCode() == 200){
                        Log.e("111","wwwwwwww"+json.toString());
                        Log.e("111","成功");
                    }else {
                        Log.e("111",json.toString());
                        Log.e("111","失败");
                    }
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






}
