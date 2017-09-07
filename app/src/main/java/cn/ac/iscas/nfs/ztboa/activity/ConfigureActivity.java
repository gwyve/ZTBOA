
package cn.ac.iscas.nfs.ztboa.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.Utils.ConfigInfo;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;


public class ConfigureActivity extends AppCompatActivity {


    // 用户id
    private String userID;


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
        locationUrlEdit = (EditText) findViewById(R.id.configActLocationUrlEdit);



        userID = sharedPreferences.getString("user_id","0");


        userIdEdit.setText(userID);


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
//        bundle.putString("user_id",userIdEdit.getText().toString());
//        bundle.putInt("interval",Integer.parseInt(intervalEdit.getText().toString()));
//        bundle.putString("center_longitude",centerLongitudeEdit.getText().toString());
//        bundle.putString("center_latitude",centerLatitudeEdit.getText().toString());
//        bundle.putInt("stop_radius",Integer.parseInt(stopRadiusEdit.getText().toString()));
////        bundle.putInt("stop_location_type",Integer.parseInt(stopLocationTypeEdit.getText().toString()));
//        bundle.putInt("stop_interval",Integer.parseInt(stopIntervalEdit.getText().toString()));
//        bundle.putString("location_url",locationUrlEdit.getText().toString());
//        RadioButton radioButton = (RadioButton)ConfigureActivity.this.findViewById(locationModeRadioGroup.getCheckedRadioButtonId());
//        bundle.putString("location_mode",radioButton.getText().toString());
        int begin1Hour = Integer.parseInt(((EditText)findViewById(R.id.configActBegin1HourEdit)).getText().toString());
        int begin1Minute = Integer.parseInt(((EditText)findViewById(R.id.configActBegin1MinuteEdit)).getText().toString());
        int end1Hour = Integer.parseInt(((EditText)findViewById(R.id.configActEnd1HourEdit)).getText().toString());
        int end1Minute = Integer.parseInt(((EditText)findViewById(R.id.configActEnd1MinuteEdit)).getText().toString());


        int begin2Hour = Integer.parseInt(((EditText)findViewById(R.id.configActBegin2HourEdit)).getText().toString());
        int begin2Minute = Integer.parseInt(((EditText)findViewById(R.id.configActBegin2MinuteEdit)).getText().toString());
        int end2Hour = Integer.parseInt(((EditText)findViewById(R.id.configActEnd2HourEdit)).getText().toString());
        int end2Minute = Integer.parseInt(((EditText)findViewById(R.id.configActEnd2MinuteEdit)).getText().toString());

        DateFormat df = new SimpleDateFormat("HH mm");
        Date begin1 = null;
        Date end1 = null;
        Date begin2 = null;
        Date end2 = null;
        try {
            begin1 = df.parse(begin1Hour+" "+begin1Minute);
            end1 = df.parse(end1Hour+" "+ end1Minute);
            begin2 = df.parse(begin2Hour+" "+begin2Minute);
            end2 = df.parse(end2Hour+" "+end2Minute);
        } catch (ParseException e) {
            e.printStackTrace();
            finish();
        }


        ((ZTBApplication)getApplication()).configInfo = new ConfigInfo(Integer.parseInt(userIdEdit.getText().toString()),Integer.parseInt(intervalEdit.getText().toString()),
                centerLongitudeEdit.getText().toString(),centerLatitudeEdit.getText().toString(),Integer.parseInt(stopIntervalEdit.getText().toString()),
                locationUrlEdit.getText().toString(),Integer.parseInt(stopRadiusEdit.getText().toString()),begin1,end1,begin2,end2);

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
