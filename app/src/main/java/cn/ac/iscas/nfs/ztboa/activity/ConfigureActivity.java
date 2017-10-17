
package cn.ac.iscas.nfs.ztboa.activity;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;
import cn.ac.iscas.nfs.ztboa.widget.RangeSeekBar;


public class ConfigureActivity extends AppCompatActivity {

    ZTBApplication application;
    //    屏幕尺寸
    private int width;
    private int height;

    private ViewGroup rootView;
    private ImageButton imageButton;
    private ImageButton homeButton;
    private Switch aSwitch;
    private RadioGroup companyRadioGroup;
    private RadioButton companyRadioButton1;
    private RadioButton companyRadioBUtton5;


//    两个滑动控件
    RangeSeekBar<Integer> seekBar;
    RangeSeekBar<Integer> seekBar2;

//  时间空间
    private TextView begin1TextView;
    private TextView begin2TextView;
    private TextView end1TextView;
    private TextView end2TextView;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private void goNextAct(){


        Intent intent = new Intent(ConfigureActivity.this,LocationActivity.class);

        startActivity(intent);

    }


    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_configure);

        application = (ZTBApplication)getApplication();

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        width = rect.width();
        height = rect.height();

        imageButton = (ImageButton) findViewById(R.id.configBtn);
        homeButton = (ImageButton) findViewById(R.id.configActHomeBtn);
        rootView = (ViewGroup)findViewById(R.id.configActRootView);
        aSwitch = (Switch) findViewById(R.id.configActSwitch);

        companyRadioGroup = (RadioGroup)findViewById(R.id.configureActCompanyRadioGroup);
        companyRadioButton1 = (RadioButton)findViewById(R.id.configureActCompany_1);
        companyRadioBUtton5 = (RadioButton)findViewById(R.id.configureActCompany_5);

        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();

        initView(ConfigureActivity.this);
    }

    @Override
    public void onStart(){
        super.onStart();
        aSwitch.setChecked(sharedPreferences.getBoolean("auto_clock",true));
//        aSwitch.setChecked(true);
        try {
            seekBar.setSelectedMinValue((int)dateFormat.parse(sharedPreferences.getString("begin1","07:30")).getTime());
            seekBar.setSelectedMaxValue((int)dateFormat.parse(sharedPreferences.getString("end1","09:30")).getTime());
            seekBar2.setSelectedMinValue((int)dateFormat.parse(sharedPreferences.getString("begin2","17:30")).getTime());
            seekBar2.setSelectedMaxValue((int)dateFormat.parse(sharedPreferences.getString("end2","19:30")).getTime());

            int company_id = sharedPreferences.getInt("company_id",1);
            if (company_id == 1){
                companyRadioButton1.setChecked(true);
            }else if (company_id == 5){
                companyRadioBUtton5.setChecked(true);
            }
            
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void initView(Context context){
//        背景定义长高
//        ImageView imageTitle = (ImageView)findViewById(R.id.configActTitle);
//        imageTitle.getLayoutParams().height = height;
//        imageTitle.getLayoutParams().width = width;

//        背景填充图
//        Picasso.with(context).load(R.drawable.configure_act).fit().into(imageTitle);


//        最下面的按钮位置
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)imageButton.getLayoutParams();
        layoutParams.setMargins(width*62/750,height*995/1300,0,0);
        imageButton.setPadding(0,0,0,0);
        imageButton.getLayoutParams().height = height * 100/1300;
        imageButton.getLayoutParams().width = width*625/750;
        Picasso.with(context).load(R.drawable.configure_act_btn).fit().into(imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.putActivity("ConfigureActivity",ConfigureActivity.this);
                application.confirmSetting = true;

//                公司信息


                editor.putString("begin1",begin1TextView.getText().toString());
                editor.putString("begin2",begin2TextView.getText().toString());
                editor.putString("end1",end1TextView.getText().toString());
                editor.putString("end2",end2TextView.getText().toString());
                editor.putBoolean("auto_clock",aSwitch.isChecked());

                RadioButton rb = (RadioButton)findViewById(companyRadioGroup.getCheckedRadioButtonId());
                int company_id = -1;
                if (rb == null){
                }else {
                    if (rb.getText().toString().equals("总体部")) {
                        company_id = 1;
                    } else {
                        company_id = 5;
                    }
                }
                editor.putInt("company_id",company_id);
                editor.commit();

                Intent intent = new Intent(ConfigureActivity.this,LocationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.location_act_right_in,R.anim.configure_act_left_out);
            }
        });

//        第一个双向滑动
        begin1TextView = new TextView(context);
        end1TextView = new TextView(context);

        begin1TextView.setText(sharedPreferences.getString("begin1","07:30"));
        end1TextView.setText(sharedPreferences.getString("end1","09:30"));

        seekBar = new RangeSeekBar<Integer>(-28800000, 14400000, this);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
                                                    Integer minValue, Integer maxValue) {
                // handle changed range values
                begin1TextView.setText(String.valueOf(dateFormat.format(new Date(minValue))));
                end1TextView.setText(String.valueOf(dateFormat.format(new Date(maxValue))));
            }
        });

        rootView.addView(begin1TextView);
        rootView.addView(end1TextView);
        rootView.addView(seekBar);

        RelativeLayout.LayoutParams textView1Params = (RelativeLayout.LayoutParams)begin1TextView.getLayoutParams();
        RelativeLayout.LayoutParams textView2Params = (RelativeLayout.LayoutParams)end1TextView.getLayoutParams();

        textView1Params.setMargins(width*40/750,height*210/1300,0,0);
        textView2Params.setMargins(width*585/750,height*210/1300,0,0);
        begin1TextView.setTextSize(width*15/750);
        begin1TextView.setTextColor(Color.rgb(10,135,191));
        end1TextView.setTextSize(width*15/750);
        end1TextView.setTextColor(Color.rgb(10,135,191));

        RelativeLayout.LayoutParams seekBarParams = (RelativeLayout.LayoutParams)seekBar.getLayoutParams();
        seekBar.setPadding(0,0,0,0);
        try {
            seekBar.setSelectedMinValue((int)dateFormat.parse(begin1TextView.getText().toString()).getTime());
            seekBar.setSelectedMaxValue((int)dateFormat.parse(end1TextView.getText().toString()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        seekBarParams.setMargins(width*60/750,height*270/1300,width*64/750,0);

//        第二个双向滑动

        begin2TextView = new TextView(context);
        end2TextView = new TextView(context);
        begin2TextView.setText(sharedPreferences.getString("begin2","17:30"));
        end2TextView.setText(sharedPreferences.getString("end2","19:30"));


        seekBar2 = new RangeSeekBar<Integer>(14400000, 57540000, this);
        seekBar2.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
                                                    Integer minValue, Integer maxValue) {
                // handle changed range values
                begin2TextView.setText(String.valueOf(dateFormat.format(new Date(minValue))));
                end2TextView.setText(String.valueOf(dateFormat.format(new Date(maxValue))));
            }
        });

        rootView.addView(begin2TextView);
        rootView.addView(end2TextView);
        rootView.addView(seekBar2);

        RelativeLayout.LayoutParams textView3Params = (RelativeLayout.LayoutParams)begin2TextView.getLayoutParams();
        RelativeLayout.LayoutParams textView4Params = (RelativeLayout.LayoutParams)end2TextView.getLayoutParams();

        textView3Params.setMargins(width*40/750,height*555/1300,0,0);
        textView4Params.setMargins(width*585/750,height*555/1300,0,0);

        begin2TextView.setTextSize(width*15/750);
        begin2TextView.setTextColor(Color.rgb(10,135,191));
        end2TextView.setTextSize(width*15/750);
        end2TextView.setTextColor(Color.rgb(10,135,191));

        RelativeLayout.LayoutParams seekBar2Params = (RelativeLayout.LayoutParams)seekBar2.getLayoutParams();
        seekBar2.setPadding(0,0,0,0);
        try {
            seekBar2.setSelectedMinValue((int)dateFormat.parse(begin2TextView.getText().toString()).getTime());
            seekBar2.setSelectedMaxValue((int)dateFormat.parse(end2TextView.getText().toString()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        seekBar2Params.setMargins(width*60/750,height*620/1300,width*64/750,0);

//        滑动开始键
        RelativeLayout.LayoutParams switchParams = (RelativeLayout.LayoutParams)aSwitch.getLayoutParams();
        aSwitch.setPadding(0,0,0,0);
        switchParams.setMargins(width*500/750,height*775/1300,0,0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            aSwitch.setChecked(true);
//        }


//      设置公司
        RelativeLayout.LayoutParams companyRadioGroupParams = (RelativeLayout.LayoutParams) companyRadioGroup.getLayoutParams();
        companyRadioGroupParams.setMargins(width*60/750,height*860/1300,width*60/750,0);
        

//        home键
        RelativeLayout.LayoutParams homeBtnParams = (RelativeLayout.LayoutParams)homeButton.getLayoutParams();
        homeButton.setPadding(0,0,0,0);
        homeButton.getLayoutParams().width=width*65/750;
        homeButton.getLayoutParams().height = height*65/1300;
        homeBtnParams.setMargins(width*10/750,height*10/1300,0,0);
        Picasso.with(context).load(R.drawable.configure_act_home_btn).fit().into(homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                application.putActivity("ConfigureActivity",ConfigureActivity.this);
                Intent intent = new Intent(ConfigureActivity.this,LocationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.location_act_right_in,R.anim.configure_act_left_out);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            application.putActivity("ConfigureActivity",ConfigureActivity.this);
            Intent intent = new Intent(ConfigureActivity.this,LocationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.location_act_right_in,R.anim.configure_act_left_out);
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }



}
