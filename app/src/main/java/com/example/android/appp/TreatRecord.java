package com.example.android.appp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TreatRecord extends AppCompatActivity {

    DataInputStream dis;
    JSONObject treatRecordJson;
    JSONArray ja;
    ArrayAdapter<String> recordItems;
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treat_record);

        recordItems= new ArrayAdapter<String>(this, R.layout.blue_tooth_device_name);
        requestTreatRecord();

        list=(ListView)findViewById(R.id.treatRecordListView);
        list.setAdapter(recordItems);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String s=(String) parent.getItemAtPosition(position);
                for(int i=0;i<ja.size();i++){
                    if(s.equals(ja.getJSONObject(i).getString("diaTime"))){
                        final int ii=i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent=new Intent(TreatRecord.this,TreatInfomation.class);
                                try{
                                    intent.putExtra("extra",treatRecordJson.getJSONArray("treatRecord").getJSONObject(ii).toString());/////////////
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                startActivity(intent);
                            }
                        });
                        break;
                    }
                }

            }
        });

    }
    public void requestTreatRecord(){
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
                        jsonobject.put("type", "requestTreatRecord");

                        dos.writeUTF(jsonobject.toString());
                        dos.flush();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"向服务器请求诊疗记录...", Toast.LENGTH_SHORT).show();
                            }
                        });

                        dis=((CommonInstance)getApplication()).getDataInputStream();
                        receiveJson();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void receiveJson() {
        try {
            String jsonString = dis.readUTF();
            treatRecordJson= JSON.parseObject(jsonString);

            JSONObject jo=JSON.parseObject(jsonString);
            ja=jo.getJSONArray("treatRecord");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<ja.size();i++){
                            recordItems.add(ja.getJSONObject(i).getString("diaTime"));
                        }
                    }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(TreatRecord.this, ChooseItem.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}