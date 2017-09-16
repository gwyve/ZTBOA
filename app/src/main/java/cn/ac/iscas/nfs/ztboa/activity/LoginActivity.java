package cn.ac.iscas.nfs.ztboa.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import cn.ac.iscas.nfs.ztboa.R;
import cn.ac.iscas.nfs.ztboa.Utils.ConfigInfo;

public class LoginActivity extends AppCompatActivity {

    TextView textView;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        textView = (TextView)findViewById(R.id.loginActTextview);
        btn = (Button)findViewById(R.id.loginActButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerPush(LoginActivity.this);
                goNext();
            }
        });

    }

    private void registerPush(Context context){
        XGPushManager.registerPush(context, "123www", new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                Log.e("111",o.toString());
                goNext();
            }

            @Override
            public void onFail(Object o, int i, String s) {

            }
        });
    }

    private void goNext(){
        Intent intent = new Intent(LoginActivity.this,ConfigureActivity.class);
        LoginActivity.this.startActivity(intent);
        LoginActivity.this.finish();
    }
}
