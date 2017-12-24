package cn.ac.iscas.nfs.ztboa.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.ac.iscas.nfs.ztboa.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {
    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;


// http://www.jb51.net/article/92100.htm
//  屏幕分辨率
    private int ratioWidth;
    private int ratidHeight;
//  屏幕尺寸大小
    private double sizeWidth;
    private double sizeHeight;
//  厂商名称
    private String brand = Build.BRAND;
//  设备名称
    private String model = Build.MODEL;
//  硬件信息
    private String deviceId;
    private String sim;
    private String imsi;
//  是否root
    private boolean isRoot;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }



            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean hasPermission(){
        if (Build.VERSION.SDK_INT>=23){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermission()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                goNext();
                }
            },1000);
        }else {
            SplashActivity.this.finish();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();

//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//            String packageName = getApplication().getPackageName();
//            boolean isIgnoring = ((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(packageName);
//            Log.e("111","eeeeeeeeeeeeeeeee");
//            Log.e("111",packageName);
//            if (!isIgnoring) {
//                Intent intent = new Intent(
//                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                try {
//                    startActivity(intent);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//        getPersimmions();
//        if (hasPermission()){
            goNext();
//        }
    }

    private void checkVersion(){

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("","")
                .build();
        final Request request = new Request.Builder()
//                .url("http:192.168.1.100:8081/version.php")
                .url("http://iscas-ztb-weixin03.wisvision.cn/app/minimum_version")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SplashActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(SplashActivity.this).
                                            setMessage("网络不可用，\n请检查网络设置").
                                            setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    SplashActivity.this.finish();
                                                    System.exit(0);
                                                }})
                                            .show();
                                }
                            });
                        }
                    });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject resJson = new JSONObject(response.body().string());
                    float serVersion = Float.parseFloat(resJson.getString("version"));
                    final String url = resJson.getString("address");
                    PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(),0);
                    float curVersion = Float.parseFloat(pi.versionName);
                    if (curVersion>serVersion){
//                        成功
                        Intent intent = new Intent(SplashActivity.this,BindActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }else {
                        SplashActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(SplashActivity.this).
                                        setMessage("该版本已不可用，\n请下载新版本应用").
                                        setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent();
                                                intent.setAction("android.intent.action.VIEW");
                                                Uri content_url = Uri.parse(url);
                                                intent.setData(content_url);
                                                startActivity(intent);
                                                SplashActivity.this.finish();
                                                }})
                                        .show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void goNext(){
//        checkVersion();
        Intent intent = new Intent(SplashActivity.this,BindActivity.class);
        startActivity(intent);
        SplashActivity.this.finish();



//        setConfig();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {

//            }
//        },1000);
    }

    private void setConfig(){
        editor.putInt("interval",5);
        editor.putInt("stop_interval",5);
        editor.putString("work_longitude","116.343789");
        editor.putString("work_latitude","39.985749");
        editor.putString("location_url","http://iscas-ztb-weixin03.wisvision.cn/app/upload/zippos");
    }


}
