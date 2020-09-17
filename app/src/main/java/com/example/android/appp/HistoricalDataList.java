package com.example.android.appp;

import android.content.Intent;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class HistoricalDataList extends AppCompatActivity {

    DataInputStream dis;
    ArrayAdapter<String> dataItems;
    JSONArray dataListJsonArray;
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_data_list);

        dataItems= new ArrayAdapter<String>(this, R.layout.blue_tooth_device_name);
        requestHistoricalDataList();

        list=(ListView)findViewById(R.id.historicalDataListView);
        list.setAdapter(dataItems);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String s=(String) parent.getItemAtPosition(position);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HistoricalDataList.this, HistoricalDataInfomation.class);
                        JSONObject jo=new JSONObject();
                        jo.put("uploadTime",s);
                        intent.putExtra("extra",jo.toString());/////////////
                        startActivity(intent);
                    }
                });
            }
        });
    }
    public void requestHistoricalDataList(){
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
                        JSONObject jsonobject=new JSONObject();
                        jsonobject.put("type", "requestHistoricalDataList");

                        DataOutputStream dos=((CommonInstance)getApplication()).getDataOutputStream();
                        dos.writeUTF(jsonobject.toString());
                        dos.flush();

                        dis=((CommonInstance)getApplication()).getDataInputStream();

                        final String s=dis.readUTF();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                            }
                        });
                        dataListJsonArray= JSON.parseObject(s).getJSONArray("historicalDataList");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i=0;i<dataListJsonArray.size();i++){
                                    dataItems.add(dataListJsonArray.getJSONObject(i).getString("fileName"));
                                }
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(HistoricalDataList.this, ChooseItem.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}