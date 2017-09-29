package cn.ac.iscas.nfs.ztboa.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.Utils.ConfigInfo;
import cn.ac.iscas.nfs.ztboa.ZTBApplication;

public class LoginActivity extends AppCompatActivity {

    TextView textView;
    Button btn;

    EditText editText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public static IWXAPI api;
    private String WX_APP_ID = "wxd5f571e673b5e375";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textView = (TextView)findViewById(R.id.loginActTextview);

//
        editText = (EditText)findViewById(R.id.loginActUserIdEdit);
        sharedPreferences = getSharedPreferences("cn.ac.iscas.nfs.ztboa",Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();
        editText.setText(sharedPreferences.getString("user_id","0"));


//        ZTBApplication.mWxApi = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
//        ZTBApplication.mWxApi.registerApp(WX_APP_ID);
        api = ZTBApplication.mWxApi;
        textView = (TextView)findViewById(R.id.loginActTextview);
        btn = (Button)findViewById(R.id.loginActButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("111","ppppppppppppppppp");
                registerPush(LoginActivity.this);
//                wxLogin();
            }
        });

    }

    private void registerPush(Context context){
        XGPushManager.registerPush(context, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                Log.e("111",o.toString());
                Toast.makeText(LoginActivity.this,o.toString(),Toast.LENGTH_LONG);
                goNext();
            }

            @Override
            public void onFail(Object o, int i, String s) {
                Toast.makeText(LoginActivity.this,"信鸽错误咯咯咯咯咯",Toast.LENGTH_LONG);
            }
        });
    }

    private void wxLogin(){
        if (!api.isWXAppInstalled()) {
            Log.e("111","www");
            return;
        }
        Log.e("wwww","ppppppppppppppppppppppppp");
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "diandi_wx_login";
        ZTBApplication.mWxApi.sendReq(req);
    }
    private void goNext(){
        editor.putString("user_id",editText.getText().toString());
        editor.commit();

        Intent intent = new Intent(LoginActivity.this,ConfigureActivity.class);
        LoginActivity.this.startActivity(intent);
        LoginActivity.this.finish();
    }
}
