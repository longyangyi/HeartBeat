package com.example.android.appp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Register extends AppCompatActivity {


    boolean received=false;
    DataInputStream dis;
    EditText userName,phoneNumber,age,passWord;
    Button registe;
    boolean coned=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userName=(EditText)findViewById(R.id.registeId);
        phoneNumber=(EditText)findViewById(R.id.registePhoneNumber);
        age=(EditText)findViewById(R.id.registeAge);
        passWord=(EditText)findViewById(R.id.registePassWord);
        registe=(Button) findViewById(R.id.registeButton);

        registe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try{
                            coned=false;
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
                                        Toast.makeText(getApplicationContext(),"未连接服务器", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{

                                try {
                                    DataOutputStream dos=((CommonInstance)getApplication()).getDataOutputStream();
                                    dis=((CommonInstance)getApplication()).getDataInputStream();

                                    JSONObject jsonObject=new JSONObject();
                                    jsonObject.put("type","registe");
                                    jsonObject.put("access",userName.getText().toString());
                                    jsonObject.put("password",passWord.getText().toString());
                                    jsonObject.put("age",age.getText().toString());
                                    jsonObject.put("phoneNumber",phoneNumber.getText().toString());
                                    jsonObject.put("userType","patient");

                                    dos.writeUTF(jsonObject.toString());
                                    dos.flush();

                                    receiveJsonUTF();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
    public void receiveJsonUTF() {
        try {
            final String jsonString = dis.readUTF();
            JSONObject jo= JSON.parseObject(jsonString);
            if(jo.getInteger("ErrCode")==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"注册成功\n"+jsonString, Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"注册失败\n"+jsonString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}
