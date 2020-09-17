package com.example.android.appp;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PersonalInfo extends AppCompatActivity {


    TextView id,DID,balance,phoneNumber,age;
    DataInputStream dis;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        handler=new Handler();
        id=(TextView)findViewById(R.id.personalId);
        DID=(TextView)findViewById(R.id.walletDID);
        balance=(TextView)findViewById(R.id.walletBalance);
        phoneNumber=(TextView)findViewById(R.id.personalPhoneNumber);
        age=(TextView)findViewById(R.id.personalAge);
        new Thread(new Runnable(){
            public void run(){
                boolean coned=false;
                try{
                    coned=((CommonInstance)getApplication()).getServer().isConnected();
                    coned=!((CommonInstance)getApplication()).getServer().isClosed();
                }catch(Exception e){
                    e.printStackTrace();
                }
                if(!coned){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"未连接服务器", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    try{
                        DataOutputStream dos=((CommonInstance)getApplication()).getDataOutputStream();

                        JSONObject jsonobject=new JSONObject();
                        jsonobject.put("type", "requestPersonalInfomation");

                        dos.writeUTF(jsonobject.toString());
                        dos.flush();

                        dis=((CommonInstance)getApplication()).getDataInputStream();
                        receiveJsonUTF();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void receiveJsonUTF() {
        try {
            final String jsonString = dis.readUTF();
            final JSONObject jo= JSON.parseObject(jsonString);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    id.setText(((CommonInstance)getApplication()).getAccess());
                    DID.setText(jo.getString("walletDID"));
                    balance.setText(jo.getString("walletBalance"));
                    phoneNumber.setText(jo.getString("phoneNumber"));
                    age.setText(jo.getString("age"));
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),jsonString, Toast.LENGTH_LONG).show();
                }
            });

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(PersonalInfo.this, ChooseItem.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
