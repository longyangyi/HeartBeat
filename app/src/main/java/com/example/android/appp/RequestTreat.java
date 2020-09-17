package com.example.android.appp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class RequestTreat extends AppCompatActivity {

    String treatOnceMoney;

    boolean coned=false;
    Button sendRequeatTreatButton,doctorListButton;
    TextView doctorDID,doctorInfomation,doctorFee;
    EditText fromTime,toTime,additionalInfo;
    DataInputStream dis;
    DataOutputStream dos;
    Handler handler;

    JSONArray doctorListJsonArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_treat);

        doctorDID=(TextView)findViewById(R.id.doctorDID);
        doctorInfomation=(TextView)findViewById(R.id.doctorInfomation3);
        doctorFee=(TextView)findViewById(R.id.doctorFee);
        sendRequeatTreatButton=(Button)findViewById(R.id.sendRequestTreat);
        fromTime=(EditText)findViewById(R.id.fromTime);
        toTime=(EditText)findViewById(R.id.toTime);
        additionalInfo=(EditText)findViewById(R.id.additionalInfo);

        handler=new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                dos=((CommonInstance)getApplication()).getDataOutputStream();
                dis=((CommonInstance)getApplication()).getDataInputStream();

                JSONObject jo=new JSONObject();
                jo.put("type","requestDoctorList");

                try{
                    dos.writeUTF(jo.toString());
                    dos.flush();
                    JSONObject jo2=receiveJson();
                    doctorListJsonArray=jo2.getJSONArray("doctorList");
                    treatOnceMoney=jo2.getString("treatOnceMoney");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();


        doctorListButton=(Button)findViewById(R.id.chooseDoctor);
        doctorListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RequestTreat.this, DoctorListActivity.class);
                intent.putExtra("extra",doctorListJsonArray.toString());
                startActivityForResult(intent, 0);

            }
        });

        sendRequeatTreatButton.setOnClickListener(new View.OnClickListener() {
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
                                        Toast.makeText(getApplicationContext(),"未连接服务器", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{

                                try {

                                    JSONObject jsonObject=new JSONObject();
                                    jsonObject.put("type","requestTreat");
                                    jsonObject.put("fromTime",fromTime.getText().toString());
                                    jsonObject.put("toTime",toTime.getText().toString());
                                    jsonObject.put("additionalInfomation",additionalInfo.getText().toString());
                                    jsonObject.put("doctorWalletDID",doctorDID.getText().toString());///////

                                    dos.writeUTF(jsonObject.toString());
                                    dos.flush();

                                    JSONObject jo=receiveJson();
                                    if(jo.getInteger("status")==0){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"请求成功", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"请求失败", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
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
    public JSONObject receiveJson() {
        try {
            return JSON.parseObject(dis.readUTF());
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String s=intent.getExtras().getString("extra");
        for(int i=0;i<doctorListJsonArray.size();i++){
            if(doctorListJsonArray.getJSONObject(i).getString("doctorInfo").equals(s)){
                final JSONObject jo=doctorListJsonArray.getJSONObject(i);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        doctorInfomation.setText(jo.getString("doctorInfo"));
                        doctorDID.setText(jo.getString("walletDID"));
                        doctorFee.setText(treatOnceMoney);
                    }
                });
                break;
            }
        }
    }
}
