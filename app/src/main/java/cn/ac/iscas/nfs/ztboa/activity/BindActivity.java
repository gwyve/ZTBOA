package cn.ac.iscas.nfs.ztboa.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.stat.a;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.ac.iscas.nfs.ztboa.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.factor;
import static android.R.attr.type;

public class BindActivity extends AppCompatActivity {

    private int width;
    private int height;

    private ViewGroup rootView;
    private ImageView backImage;
    private EditText emailEditText;
    private EditText phoneEditText;
    private RadioGroup companyRadioGroup;
    private ImageButton imageButton;

    private ProgressBar progressBar;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;



    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_bind);

        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getInt("user_id",-1) != -1){
            Intent intent = new Intent(BindActivity.this,LocationActivity.class);
            startActivity(intent);
            BindActivity.this.finish();
        }

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        width = rect.width();
        height = rect.height();

        rootView = (ViewGroup)findViewById(R.id.bindActRootView);
        backImage  =(ImageView)findViewById(R.id.bindActBackground);
        emailEditText = (EditText) findViewById(R.id.bindActEmailEditText);
        phoneEditText = (EditText) findViewById(R.id.bindActPhoneEditText);
        companyRadioGroup = (RadioGroup)findViewById(R.id.loginActCompanyRadioGroup);
        imageButton = (ImageButton) findViewById(R.id.bindActImageBtn);

        progressBar = (ProgressBar)findViewById(R.id.bindActProgressBar);

        EditText defaultEditTest = (EditText)findViewById(R.id.bindActDefault);
        defaultEditTest.requestFocus();

        initView(BindActivity.this);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton rb = (RadioButton)findViewById(companyRadioGroup.getCheckedRadioButtonId());
                if (rb == null){
                    Toast.makeText(BindActivity.this,"请选择公司",Toast.LENGTH_LONG).show();
                }else {
                    int company_id;
                    if (rb.getText().toString().equals("总体部")){
                        company_id = 1;
                    }else {
                        company_id = 5;
                    }
//                  访问服务器，获得user id
                    imageButton.setClickable(false);
                    getUserId(emailEditText.getText().toString(),phoneEditText.getText().toString(),company_id);
                }
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initView(Context context){
//        背景
        backImage.getLayoutParams().width = width;
        backImage.getLayoutParams().height = height;
        Picasso.with(context).load(R.drawable.bind_act_background).fit().into(backImage);


//      email匡
        emailEditText.setPadding(0,0,0,0);
        emailEditText.getLayoutParams().height = height*100/1300;
        emailEditText.getLayoutParams().width = width*625/750;
        final RelativeLayout.LayoutParams emailEditParams = (RelativeLayout.LayoutParams)emailEditText.getLayoutParams();
        emailEditParams.setMargins(width*63/750,height*550/1300,width*63/750,0);
        emailEditText.setPaddingRelative(width*20/750,0,0,0);
        emailEditText.setText("请输入邮箱进行绑定");
        emailEditText.setBackground(getResources().getDrawable(R.drawable.bind_act_email));
//        emailEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                String s = emailEditText.getText().toString();
//                if (s.equals("输入邮箱进行绑定")||s.equals("请入邮箱进行绑定")||s.equals("请输邮箱进行绑定")
//                        || s.equals("请输入箱进行绑定")||s.equals("请输入邮进行绑定")||s.equals("请输入邮箱行绑定")
//                        ||s.equals("请输入邮箱进绑定")||s.equals("请输入邮箱进行定")||s.equals("请输入邮箱进行绑"))
//                    emailEditText.setText("");
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//        emailEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (emailEditText.getText().toString().equals("请输入邮箱进行绑定"))
//                    emailEditText.setText("");
//            }
//        });



//      phone的输入框
        phoneEditText.setPadding(0,0,0,0);
        phoneEditText.getLayoutParams().height = height*100/1300;
        phoneEditText.getLayoutParams().width = width*625/750;
        final RelativeLayout.LayoutParams phoneEditParams = (RelativeLayout.LayoutParams)phoneEditText.getLayoutParams();
        phoneEditParams.setMargins(width*64/750,height*680/1300,width*63/750,0);
        phoneEditText.setPaddingRelative(width*20/750,0,0,0);
        phoneEditText.setText("请输入手机号进行绑定");
        phoneEditText.setBackground(getResources().getDrawable(R.drawable.bind_act_email));
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = phoneEditText.getText().toString();
                if (s.equals("输入手机号进行绑定")||s.equals("请入手机号进行绑定")||s.equals("请输手机号进行绑定")
                        || s.equals("请输入机号进行绑定")||s.equals("请输入手号进行绑定")||s.equals("请输入手机进行绑定")
                        ||s.equals("请输入手机号行绑定")||s.equals("请输入手机号进绑定")||s.equals("请输入手机号进行定")
                        ||s.equals("请输入手机号进行绑"))
                    phoneEditText.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
//        phoneEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (phoneEditText.getText().toString().equals("请输入手机号进行绑定"))
//                    phoneEditText.setText("");
//            }
//        });


//      公司选择radio
        RelativeLayout.LayoutParams companyRadioGroupParams = (RelativeLayout.LayoutParams) companyRadioGroup.getLayoutParams();
        companyRadioGroupParams.setMargins(width*60/750,height*800/1300,width*60/750,0);

//        imagebutton
        imageButton.setPadding(0,0,0,0);
        imageButton.getLayoutParams().width = width*630/750;
        imageButton.getLayoutParams().height = height*100/1300;
        RelativeLayout.LayoutParams imageButtonParams = (RelativeLayout.LayoutParams)imageButton.getLayoutParams();
        imageButtonParams.setMargins(width*62/750,height*1030/1300,0,0);
        Picasso.with(context).load(R.drawable.bind_act_btn).fit().into(imageButton);
    }

    private void getUserId(final String email, final String phone, final int company_id){
        progressBar.setVisibility(View.VISIBLE);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        final JSONObject json = new JSONObject();
        try {
            json.put("email",email);
            json.put("phone",phone);
            json.put("company",company_id);

            RequestBody body = RequestBody.create(JSON,json.toString());
            Request request = new Request.Builder()
//                    .url("http:192.168.1.100:8081/userid.php")
//                    .url("http://iscas-ztb-weixin03.wisvision.cn/app/info")
                    .url("http://iscas-ztb-weixin03.wisvision.cn/app/bind")
                    .post(body).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    imageButton.setClickable(true);
                    BindActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(BindActivity.this,"网络不好",Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject resJson = new JSONObject(response.body().string());
                        Log.e("111",resJson.toString());
                        Log.e("111","======================================");
                        Log.e("111",json.toString());
                        if (resJson.getInt("code")!=1000 || resJson.getInt("user_id")==-1 ){
                            Log.e("111","==++++++++++++++++++++++++++++++++");
                            BindActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    try {
                                        Toast.makeText(BindActivity.this,resJson.getString("err_msg"),Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
//                                        e.printStackTrace();
                                        Toast.makeText(BindActivity.this,"服务器有错误",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else {
                            Log.e("111","kkkkkkkkkkkkkkkkkkk");
                            editor.putString("email",email);
                            editor.putString("phone",phone);
                            editor.putInt("company_id",company_id);

                            editor.putInt("user_id",resJson.getInt("user_id"));
//                            editor.putString("user_name",resJson.getString("name"));
                            editor.commit();

                            registerPush(BindActivity.this);
//                            Intent intent = new Intent(BindActivity.this,LocationActivity.class);
//                            startActivity(intent);
//                            BindActivity.this.finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    imageButton.setClickable(true);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void registerPush(final Context context){
        XGPushManager.registerPush(context, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
//                Log.e("111",o.toString());
//                Toast.makeText(LoginActivity.this,o.toString(),Toast.LENGTH_LONG);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                OkHttpClient client = new OkHttpClient();
                final JSONObject json = new JSONObject();
                try{
                    json.put("userId",sharedPreferences.getInt("user_id",-1));
                    json.put("xinge_token",o.toString());
                    Log.e("111",""+o.toString());
                    RequestBody body = RequestBody.create(JSON,json.toString());
                    Request request = new Request.Builder()
//                    .url("http:192.168.1.100:8081/userid.php")
//                    .url("http://iscas-ztb-weixin03.wisvision.cn/app/info")
                            .url("http://iscas-ztb-weixin03.wisvision.cn/app/push/settoken")
                            .post(body).build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(context,"信鸽错误",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Intent intent = new Intent(BindActivity.this,LocationActivity.class);
                            startActivity(intent);
                            BindActivity.this.finish();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFail(Object o, int i, String s) {
                Toast.makeText(context,"信鸽错误",Toast.LENGTH_LONG);
            }
        });
    }
}
