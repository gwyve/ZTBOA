package cn.ac.iscas.nfs.ztboa;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
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
import com.baidu.location.Poi;
import com.baidu.location.service.LocationService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class forDemoActivity extends AppCompatActivity {

    private LocationService locationService;

    private String userid;
    private String ip;
//    private String port;
    private int interval;


    private  TextView showDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_demo);

//        Bundle bundle = getIntent().getExtras();
//        userid = bundle.getString("userid");
//        ip = bundle.getString("ip");
////        port = bundle.getString("port");
//        interval = bundle.getInt("interval");
//
//        showDetail = (TextView)findViewById(R.id.showDetail);


        Bundle bundle = getIntent().getExtras();
        Intent intent = new Intent(forDemoActivity.this,ForDemoService.class);
        intent.putExtras(bundle);
        startService(intent);
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
                JSONObject json = createjson(location.getTime(),userid,location.getLatitude()+"",location.getLongitude()+"",location.getRadius()+"");
                sendRequestWithHttpClient(ip,json);
            }
        }

        public void onConnectHotSpotMessage(String s, int i){
        }
    };




    private JSONObject createjson(String time,String userid,String latitude,String longitude,String redius){
        JSONObject json = new JSONObject();
        try {
            json.put("time",time);
            json.put("userid",userid);
            json.put("latitude",latitude);
            json.put("longitude",longitude);
            json.put("redius",redius);
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
                    HttpPost httpPost = new HttpPost(ip);
                    StringEntity entity = new StringEntity(json.toString(),"utf-8");

                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    httpPost.setEntity(entity);

                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200){
                        Log.e("111","wwwwwwww"+json.toString());
                        logMsg("成功");
                    }else {
                        Log.e("111",json.toString());
                        logMsg("失败");
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



    public void logMsg(final String str) {
        final String s = str;
        try {
            if (showDetail != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showDetail.post(new Runnable() {
                            @Override
                            public void run() {
                                showDetail.setText(str);
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




}
