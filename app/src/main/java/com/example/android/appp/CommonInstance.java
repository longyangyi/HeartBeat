package com.example.android.appp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class CommonInstance extends MultiDexApplication {
    public Socket server=null;
    public void setServer(Socket s){
        this.server=s;
    }
    public Socket getServer(){
        return server;
    }

    DataOutputStream dos=null;
    DataInputStream dis=null;
    public void setDataOutputStream(DataOutputStream s){
        this.dos=s;
    }
    public void setDataInputStream(DataInputStream s){
        this.dis=s;
    }
    public DataOutputStream getDataOutputStream(){
        return dos;
    }
    public DataInputStream getDataInputStream(){
        return dis;
    }


    public String access=null,password=null;
    public void setAccessAndPassWord(String a,String p){
        this.access=a;
        this.password=p;
    }
    public String getAccess(){
        return this.access;
    }
    public String getPassword(){
        return password;
    }



    private List<Activity> list = new ArrayList<Activity>();
    private static CommonInstance ea;//ExitApplication
    public static CommonInstance getInstance() {
        if (null == ea) {
            ea = new CommonInstance();
        }
        return ea;
    }
    public void addActivity(Activity activity) {
        list.add(activity);
    }
    public void exit(Context context) {
        for (Activity activity : list) {
            activity.finish();
        }
        System.exit(0);
    }

}
