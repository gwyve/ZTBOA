package cn.ac.iscas.nfs.ztboa.pushUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import cn.ac.iscas.nfs.ztboa.service.LocUpService;

/**
 * Created by VE on 2017/9/16.
 */

public class XGPushReceiver extends XGPushBaseReceiver {

    private static Context context = null;

    public static void setContext(Context cont){
        context=cont;
    }

    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        Log.e("111",xgPushTextMessage.toString());
        if (context!=null){
            Log.e("1111",xgPushTextMessage.getContent());
            if (xgPushTextMessage.getContent().equals("0000")){
                sendLocUp();
            }
        }
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {

    }

    private void sendLocUp(){
        Intent intent = new Intent();
        intent.setAction("cn.ac.iscas.nfs.ztboa.xgpush");
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),0,pendingIntent);
//                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,pendingIntent);
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+stopInterval*1000,pendingIntent);
        }else {
            context.sendBroadcast(intent);
        }

    }
}
