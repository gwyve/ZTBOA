package cn.ac.iscas.nfs.ztboa;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.location.service.LocationService;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;
import java.util.Map;

import cn.ac.iscas.nfs.ztboa.Utils.ConfigInfo;
import cn.ac.iscas.nfs.ztboa.Utils.NetUtil;


/**
 * Created by VE on 2017/7/2.
 */
public class ZTBApplication extends Application{

    public LocationService locationService;
    public Vibrator mVibrator;

    public static IWXAPI mWxApi;
    private String WX_APP_ID = "wxd5f571e673b5e375";

//    网络上传工具
    public NetUtil netUtil;
//    配置信息
    public ConfigInfo configInfo;
    public double distance = 0;

//  确认修改设置
    public static boolean confirmSetting = false;

// activity map
    private Map<String,Activity> activityMap = new HashMap<String, Activity>();
    public void putActivity(String activityName,Activity activity){
        if (activityMap.get(activityName)==null)
            activityMap.put(activityName,activity);
    }
    public void destoryActivity(String activityName){
        Activity activity = activityMap.get(activityName);
        if (activity != null)
            activity.finish();
    }
    public Activity getActivity(String activityName){
        return activityMap.get(activityName);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        netUtil = new NetUtil(this);
//        SDKInitializer.initialize(getApplicationContext());
        mWxApi = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
        mWxApi.registerApp(WX_APP_ID);


    }


}
