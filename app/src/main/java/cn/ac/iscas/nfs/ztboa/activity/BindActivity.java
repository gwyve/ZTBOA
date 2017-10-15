package cn.ac.iscas.nfs.ztboa.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
        imageButton = (ImageButton) findViewById(R.id.bindActImageBtn);

        progressBar = (ProgressBar)findViewById(R.id.bindActProgressBar);

        initView(BindActivity.this);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageButton.setClickable(false);
                getUserId(emailEditText.getText().toString());

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
        RelativeLayout.LayoutParams emailEditParams = (RelativeLayout.LayoutParams)emailEditText.getLayoutParams();
        emailEditParams.setMargins(width*63/750,height*550/1300,width*63/750,0);
        emailEditText.setPaddingRelative(width*20/750,0,0,0);
        emailEditText.setText("请输入邮箱进行绑定");
        emailEditText.setBackground(getResources().getDrawable(R.drawable.bind_act_email));
        emailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailEditText.getText().toString().equals("请输入邮箱进行绑定"))
                    emailEditText.setText("");
            }
        });

//        imagebutton
        imageButton.setPadding(0,0,0,0);
        imageButton.getLayoutParams().width = width*630/750;
        imageButton.getLayoutParams().height = height*100/1300;
        RelativeLayout.LayoutParams imageButtonParams = (RelativeLayout.LayoutParams)imageButton.getLayoutParams();
        imageButtonParams.setMargins(width*62/750,height*800/1300,0,0);
        Picasso.with(context).load(R.drawable.bind_act_btn).fit().into(imageButton);
    }

    private void getUserId(final String email){
        progressBar.setVisibility(View.VISIBLE);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("email",email);
            RequestBody body = RequestBody.create(JSON,json.toString());
            Request request = new Request.Builder()
                    .url("http:192.168.1.100:8081/userid.php")
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
                        JSONObject resJson = new JSONObject(response.body().string());
                        if (resJson.getInt("userid")==-1){
                            BindActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(BindActivity.this,"请输入正确电子邮箱",Toast.LENGTH_LONG).show();
                                }
                            });
                        }else {
                            editor.putString("email",email);
                            editor.putInt("user_id",resJson.getInt("userid"));
                            editor.putString("user_name",resJson.getString("name"));
                            editor.commit();

                            Intent intent = new Intent(BindActivity.this,LocationActivity.class);
                            startActivity(intent);
                            BindActivity.this.finish();
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

}
