package com.example.android.appp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;


public class HistoricalDataInfomation extends AppCompatActivity {

    TextView POEID,created,walletDID,hashValue,uploadTime;
    Handler handler=new Handler();
    Button visualChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_data_infomation);

        Intent intent=getIntent();
        final JSONObject jo= JSON.parseObject(intent.getExtras().getString("extra"));
        jo.put("type","requestHistoricalDataInfomation");
        requestHistoricalDataInfomation(jo);

        POEID=(TextView)findViewById(R.id.POEID);
        created=(TextView)findViewById(R.id.created);
        walletDID=(TextView)findViewById(R.id.walletDID2);
        hashValue=(TextView)findViewById(R.id.hashValue);
        uploadTime=(TextView)findViewById(R.id.uploadTime);

        visualChart=(Button)findViewById(R.id.visualChartButton);
        visualChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoricalDataInfomation.this, HistoricalDataVisualChart.class);
                intent.putExtra("extra",jo.toString());/////////////
                startActivity(intent);
            }
        });
    }
    public void requestHistoricalDataInfomation(JSONObject joo){
        final JSONObject jo=joo;
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        dos.writeUTF(jo.toString());
                        dos.flush();

                        DataInputStream dis=((CommonInstance)getApplication()).getDataInputStream();
                        final JSONObject jooo=JSON.parseObject(dis.readUTF());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadTime.setText(jo.getString("uploadTime"));
                                POEID.setText(jooo.getJSONObject("Payload").getString("id"));
                                created.setText(jooo.getJSONObject("Payload").getString("created"));
                                walletDID.setText(jooo.getJSONObject("Payload").getString("owner"));
                                hashValue.setText(jooo.getJSONObject("Payload").getJSONObject("offchain_metadata").getString("contentHash"));
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
