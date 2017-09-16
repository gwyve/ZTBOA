package cn.ac.iscas.nfs.ztboa.Utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.service.LocUpService;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;

/**
 * Created by VE on 2017/8/31.
 */

public class NetUtil {
    ZTBApplication app;
    public NetUtil(ZTBApplication app){
        this.app = app;
    }
    public void longShowDetail(String str, LocUpService.DataCallback dataCallback){
//        Toast.makeText(app,str,Toast.LENGTH_LONG).show();
        dataCallback.dataChanged(str);
    }
    public void shortShowDetail(String str){
        Toast.makeText(app,str,Toast.LENGTH_SHORT).show();
    }

    public void longShowDetail(Context context,String str){
        Toast.makeText(context,str,Toast.LENGTH_LONG).show();
    }


    public void sendRequestWithHttpClient(final String url,
                                           final JSONObject json, final double latitude, final double longitude, final LocUpService.DataCallback dataCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                try {
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
                    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);

                    StringEntity entity = new StringEntity(json.toString(), "utf-8");
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        app.distance = getDistance(latitude,longitude);
                        dataCallback.dataChanged("成功 "+json.toString());
                    } else {
                        longShowDetail("定位上传网络错误,http错误代码：" +response.getStatusLine().getStatusCode(),dataCallback);
                    }
                } catch (UnsupportedEncodingException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (IOException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (Exception e){
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }

            }
        }).start();
    }



    public void sendRequestWithHttpClient42Bytes(final String url, final JSONObject json,
                                                 final byte[] context, final double centerLatitude, final double centerLongitude, final LocUpService.DataCallback dataCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                try {
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
                    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);

                    ByteArrayEntity entity = new ByteArrayEntity(context);
//                    StringEntity entity = new StringEntity(json.toString(), "utf-8");
                    entity.setContentEncoding("");
                    entity.setContentType("");
                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        app.distance = getDistance(centerLatitude,centerLongitude);
                        dataCallback.dataChanged("成功 "+json.toString());
                        Log.e("成功",""+json.toString());
                    } else {
                        longShowDetail("定位上传网络错误,http错误代码：" +response.getStatusLine().getStatusCode(),dataCallback);
                    }
                    Log.e("111","rrrrrrrrrrrrrrrr"+response.getStatusLine().getStatusCode());
                } catch (UnsupportedEncodingException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (IOException e) {
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } catch (Exception e){
                    longShowDetail("上传网络异常："+e.toString(),dataCallback);
                    e.printStackTrace();
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }

            }
        }).start();
    }

    private float getDistance(double latitude2,double longitude2){
        float[] res=new float[1];
        double workLatitude = Double.valueOf(app.configInfo.getCenterLatitude()).doubleValue();
        double workLongitude = Double.valueOf(app.configInfo.getCenterLongitude()).doubleValue();
        Location.distanceBetween(workLatitude, workLongitude, latitude2, longitude2, res);
        return res[0];
    }




}
