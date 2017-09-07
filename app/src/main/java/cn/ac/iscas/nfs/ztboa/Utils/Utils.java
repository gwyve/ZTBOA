package cn.ac.iscas.nfs.ztboa.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.SynchronousQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by VE on 2017/8/31.
 */

public class Utils {

//    获得网络类型
    public static short getAPNType(Context context) {
        //结果返回值
//        String netType = "nono_connect";
        short netType = 0;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
//            netType = "wifi";
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //4G
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
//                netType = "4G_LTE";
                netType = 2;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    && !telephonyManager.isNetworkRoaming()){
//                netType = "3G_UMTS";
                netType = 3;
            } else if(nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    && !telephonyManager.isNetworkRoaming()){
//                netType = "3G_HSDPA";
                netType = 4;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
//                netType = "3G_EVDO_0";
                netType = 5;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    && !telephonyManager.isNetworkRoaming()){
//                netType = "2G_GPRS";
                netType = 6;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    && !telephonyManager.isNetworkRoaming()){
//                netType = "2G_EDGE";
                netType = 7;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_CDMA && !telephonyManager.isNetworkRoaming()) {
//                netType = "2G_CDMA";
                netType = 8;
            } else {
//                netType = "2G";
                netType = 9;
            }
        }
        return netType;
    }

//    获得系统时间单位毫秒
    public static long getSysTime(){
        return System.currentTimeMillis();
    }
    public static String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\")");
        Date curDate =  new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }
    public static Date getCurrentHourMinute(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH mm");
        Date curDate =  new Date(System.currentTimeMillis());
        try {
            return formatter.parse(formatter.format(curDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getTimeMillis(String date){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            Date dt = sdf.parse(date);
            return dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Long.parseLong(null);
    }

    public static byte[] int2Bytes(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    //byte 数组与 long 的相互转换
    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }
    public static byte[] short2Byte(short number){
        int temp = number;
        byte[] b =new byte[2];
        for(int i =0; i < b.length; i++){
            b[i]=new Integer(temp &0xff).byteValue();//
                    temp = temp >>8;// 向右移8位
        }
        return b;
    }

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static byte[] locatonEncode(short locType, long locTime,int userid,double latitude,double longitude,short radius,
                                long phonetime,short netType){
        byte[] bytes = new byte[42];
        int count = 0;

        byte[] locBytes = Utils.short2Byte(locType);
        for (int i = 0; i < 2; i++) {
            bytes[count] = locBytes[i];
            count++;
        }
        byte[] timtBytes = Utils.long2Bytes(locTime);
        for (int i = 0; i < 8; i++) {
            bytes[count] = timtBytes[i];
            count++;
        }
        byte[] useridBytes = Utils.int2Bytes(userid);
        for (int i = 0; i < 4; i++) {
            bytes[count] = useridBytes[i];
            count++;
        }
        byte[] latitudeBytes = Utils.double2Bytes(latitude);
        for (int i = 0; i < 8; i++) {
            bytes[count] = latitudeBytes[i];
            count++;
        }
        byte[] longtitudeBytes = Utils.double2Bytes(longitude);
        for (int i = 0; i < 8; i++) {
            bytes[count] = longtitudeBytes[i];
            count++;
        }
        byte[] radiusBytes = Utils.short2Byte(radius);
        for (int i = 0; i < 2; i++) {
            bytes[count] = radiusBytes[i];
            count++;
        }
        byte[] phonetimeBytes = Utils.long2Bytes(phonetime);
        for (int i = 0; i < 8; i++) {
            bytes[count] = phonetimeBytes[i];
            count++;
        }
        byte[] nettypeBytes = Utils.short2Byte(netType);
        for (int i = 0; i <2; i++) {
            bytes[count+i] = nettypeBytes[i];
        }

        return bytes;

    }

    public static byte[] AesEcbDecode(byte[] content, byte[] keyBytes, byte[] iv) {
        try{
            KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
            keyGenerator.init(128, new SecureRandom(keyBytes));
            SecretKey key=keyGenerator.generateKey();
            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] result=cipher.doFinal(content);
            return result;
        }catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("exception:"+e.toString());
        }
        return null;
    }





}
