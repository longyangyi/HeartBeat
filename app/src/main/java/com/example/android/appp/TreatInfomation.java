package com.example.android.appp;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class TreatInfomation extends AppCompatActivity {

    JSONObject jo;
    String doctorDIDString;
    DataInputStream dis;
    Handler handler=new Handler();
    TextView doctorDID,doctorInfomation,doctorFee,fromTime,toTime,treatResponse,adviceMeetTreat,orderMoney,orderResponse;
    Button orderMeetTreat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treat_infomation);

        orderMeetTreat=(Button)findViewById(R.id.orderMeetTreat);

        orderMeetTreat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                jsonobject.put("type", "orderMeetTreat");
                                jsonobject.put("doctorWalletDID",doctorDIDString);
                                jsonobject.put("diaTime",jo.getString("diaTime"));
                                dos.writeUTF(jsonobject.toString());
                                dos.flush();
                                DataInputStream dis=((CommonInstance)getApplication()).getDataInputStream();
                                if(JSON.parseObject(dis.readUTF()).getInteger("status")==0){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"已支付预约金，请等待结果", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"预约失败", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        doctorDID=(TextView)findViewById(R.id.doctorDID2);
        doctorInfomation=(TextView)findViewById(R.id.doctorInfomation2);
        doctorFee=(TextView)findViewById(R.id.doctorFee2);
        fromTime=(TextView)findViewById(R.id.fromTime2);
        toTime=(TextView)findViewById(R.id.toTime2);
        treatResponse=(TextView)findViewById(R.id.treatResponse);
        adviceMeetTreat=(TextView)findViewById(R.id.adviceMeetTreat);
        orderMoney=(TextView)findViewById(R.id.orderMoney);
        orderResponse=(TextView)findViewById(R.id.orderResponse);////////////

        try{
            Intent intent=getIntent();
            String s=intent.getExtras().getString("extra");
            jo= JSON.parseObject(s);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    doctorDIDString=jo.getString("doctorWalletDID");
                    doctorDID.setText(jo.getString("doctorWalletDID"));
                    doctorFee.setText(jo.getString("treatFee"));
                    fromTime.setText(jo.getString("fromTime"));
                    toTime.setText(jo.getString("toTime"));
                    treatResponse.setText(jo.getString("diaResult"));
                    adviceMeetTreat.setText(jo.getString("whetherToConsult"));
                    orderMoney.setText(20+"");
                    orderResponse.setText(jo.getString("reservationResult"));
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
