package cn.ac.iscas.nfs.ztboa;

import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.Vibrator;

import com.baidu.location.service.LocationService;


/**
 * Created by VE on 2017/7/2.
 */
public class ZTBApplication extends Application{

    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        SDKInitializer.initialize(getApplicationContext());

    }


}
