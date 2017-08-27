
package cn.ac.iscas.nfs.ztboa;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import cn.ac.iscas.nfs.ztboa.Entity.Location;

public class ConfigureActivity extends AppCompatActivity {

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
    private int stopLocationType;
//    本次停止上传后，与下次上传间隔
    private int stopInterval;
//    定位模式选择
    private String locationMode;
//    上传地址url
    private String locationUrl;



    private EditText userIdEdit;
    private EditText intervalEdit;
    private EditText centerLongitudeEdit;
    private EditText centerLatitudeEdit;
    private EditText stopRadiusEdit;
//    private EditText stopLocationTypeEdit;
    private EditText stopIntervalEdit;
    private EditText locationUrlEdit;
    private RadioGroup locationModeRadioGroup;
    private Button nextBtn;



    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private void init(){
        userIdEdit = (EditText)findViewById(R.id.configActUserIdEdit);
        intervalEdit = (EditText)findViewById(R.id.configureActIntervalEdit);
        centerLatitudeEdit = (EditText)findViewById(R.id.configureActCenterLatitudeEdit);
        centerLongitudeEdit = (EditText) findViewById(R.id.configureActCenterLongitudeEdit);
        stopRadiusEdit = (EditText)findViewById(R.id.configureActStopRadiusEdit);
        stopIntervalEdit = (EditText)findViewById(R.id.configureActStopIntervalEdit);
//        stopLocationTypeEdit = (EditText)findViewById(R.id.configureActStopIntervalEdit);
        locationModeRadioGroup = (RadioGroup)findViewById(R.id.configActLocationMode);
        locationUrlEdit = (EditText) findViewById(R.id.configActLocationUrlEdit);



        userID = sharedPreferences.getString("user_id","0");
//        interval = sharedPreferences.getInt("interval",2);
//        centerLongitude = sharedPreferences.getString("center_longitude","116.343789");
//        centerLatitude = sharedPreferences.getString("center_latitude","39.985749");
//        stopRadius = sharedPreferences.getInt("stop_radius",15);
//        stopLocationType = sharedPreferences.getInt("stop_location_type",61);
//        stopInterval = sharedPreferences.getInt("stop_interval",5);
//        locationMode = sharedPreferences.getString("location_mode","Hight_Accuracy");
//        locationUrl = sharedPreferences.getString("location_url","http://iscas-ztb-weixin03.wisvision.cn/app/upload/pos");


        userIdEdit.setText(userID);
//        intervalEdit.setText(interval+"");
//        centerLongitudeEdit.setText(centerLongitude);
//        centerLatitudeEdit.setText(centerLatitude);
//        stopRadiusEdit.setText(stopRadius+"");
//        stopLocationTypeEdit.setText(stopLocationType+"");
//        stopIntervalEdit.setText(stopInterval+"");


    }

    private void goNextAct(){
        editor.putString("user_id",userIdEdit.getText().toString());
        editor.commit();
//        editor.putInt("interval",Integer.parseInt(intervalEdit.getText().toString()));
//        editor.putString("center_longitude",centerLongitudeEdit.getText().toString());
//        editor.putString("center_latitude",centerLatitudeEdit.getText().toString());
//        editor.putInt("stop_radius",Integer.parseInt(stopRadiusEdit.getText().toString()));
//        editor.putInt("stop_location_type",Integer.parseInt(stopLocationTypeEdit.getText().toString()));
//        editor.putInt("stop_interval",Integer.parseInt(stopIntervalEdit.getText().toString()));
//
//        RadioButton radioButton = (RadioButton)ConfigureActivity.this.findViewById(locationModeRadioGroup.getCheckedRadioButtonId());
//        editor.putString("location_mode",radioButton.getText().toString());

        Intent intent = new Intent(ConfigureActivity.this,LocationActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("user_id",userIdEdit.getText().toString());
        bundle.putInt("interval",Integer.parseInt(intervalEdit.getText().toString()));
        bundle.putString("center_longitude",centerLongitudeEdit.getText().toString());
        bundle.putString("center_latitude",centerLatitudeEdit.getText().toString());
        bundle.putInt("stop_radius",Integer.parseInt(stopRadiusEdit.getText().toString()));
//        bundle.putInt("stop_location_type",Integer.parseInt(stopLocationTypeEdit.getText().toString()));
        bundle.putInt("stop_interval",Integer.parseInt(stopIntervalEdit.getText().toString()));
        bundle.putString("location_url",locationUrlEdit.getText().toString());
        RadioButton radioButton = (RadioButton)ConfigureActivity.this.findViewById(locationModeRadioGroup.getCheckedRadioButtonId());
        bundle.putString("location_mode",radioButton.getText().toString());
        intent.putExtras(bundle);

        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();

        init();

        Button button = (Button)findViewById(R.id.configBtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNextAct();
            }
        });

    }

}
