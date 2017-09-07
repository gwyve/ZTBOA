package cn.ac.iscas.nfs.ztboa.Utils;

import java.util.Date;

/**
 * Created by VE on 2017/8/31.
 */

public class ConfigInfo {
    // 用户id
    private int userID;
    // 每次上传间隔时间 单位：秒
    private int interval;
    // 工作区域中心坐标
    private String centerLongitude;
    private String centerLatitude;
    //    本次停止上传后，与下次上传间隔
    private int stopInterval;
    //    上传地址url
    private String locationUrl;

    public int getStopRadius() {
        return stopRadius;
    }

    //    停止上传半径
    private int stopRadius;

    private Date begin1;
    private Date begin2;
    private Date end1;
    private Date end2;



    public int getUserID() {
        return userID;
    }

    public int getInterval() {
        return interval;
    }

    public String getCenterLongitude() {
        return centerLongitude;
    }

    public String getCenterLatitude() {
        return centerLatitude;
    }

    public int getStopInterval() {
        return stopInterval;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public Date getBegin1() {
        return begin1;
    }

    public Date getBegin2() {
        return begin2;
    }

    public Date getEnd1() {
        return end1;
    }

    public Date getEnd2() {
        return end2;
    }

    public ConfigInfo(int userID, int interval, String centerLongitude, String centerLatitude, int stopInterval, String locationUrl, int stopRadius, Date begin1, Date end1, Date begin2, Date end2) {
        this.userID = userID;
        this.interval = interval;
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.stopInterval = stopInterval;
        this.locationUrl = locationUrl;
        this.stopRadius = stopRadius;
        this.begin1 = begin1;
        this.end1 = end1;
        this.begin2 = begin2;
        this.end2 = end2;
    }




}
