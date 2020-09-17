package com.example.android.appp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class HistoricalDataVisualChart extends AppCompatActivity {


    private LineChart lineChart;
    private LineData lineData;
    JSONObject dataJson=null;

    int interval=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_data_visual_chart);

        Intent intent=getIntent();
        JSONObject jo= JSON.parseObject(intent.getExtras().getString("extra"));
        jo.put("type","requestHistoricalData");
        requestHistoricalData(jo);

        final TextView intervalText=(TextView)findViewById(R.id.interval);

        Button heartRateShow=(Button)findViewById(R.id.showHistoricalHeartRateChart);
        heartRateShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataJson==null){
                    Toast.makeText(getApplicationContext(),"请等待服务器数据",Toast.LENGTH_SHORT).show();
                }else{
                    ArrayList<String> timeList=new ArrayList<String>();
                    ArrayList<Integer> valueList=new ArrayList<Integer>();
                    JSONArray ja=dataJson.getJSONArray("data");
                    for(int i=0;i<ja.size();i+=interval){
                        JSONObject jo=ja.getJSONObject(i);
                        if(jo.getString("dataType").equals("heartRate")){
                            valueList.add(Integer.parseInt(jo.getString("value")));
                            timeList.add(jo.getString("time"));
                        }
                    }
                    initChart(timeList,valueList,"心率数据图");
                }
            }
        });
        Button bloodOxygenShow=(Button)findViewById(R.id.showHistoricalBloodOxygenChart);
        bloodOxygenShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataJson==null){
                    Toast.makeText(getApplicationContext(),"请等待服务器数据",Toast.LENGTH_SHORT).show();
                }else{
                    ArrayList<String> timeList=new ArrayList<String>();
                    ArrayList<Integer> valueList=new ArrayList<Integer>();
                    JSONArray ja=dataJson.getJSONArray("data");
                    for(int i=0;i<ja.size();i+=interval){
                        JSONObject jo=ja.getJSONObject(i);
                        if(jo.getString("dataType").equals("bloodOxygen")){
                            valueList.add(Integer.parseInt(jo.getString("value")));
                            timeList.add(jo.getString("time"));
                        }
                    }
                    initChart(timeList,valueList,"血氧数据图");
                }
            }
        });

        SeekBar seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setProgress(1);
        seekBar.setMax(3600);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int i=seekBar.getProgress();
                if(i>0){
                    interval=i;
                    if(i<60){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                intervalText.setText("两点间隔："+i+"秒");
                            }
                        });
                    }else{
                        if(i>=60&&i<3600){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    intervalText.setText("两点间隔："+i/60+"分");
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    intervalText.setText("两点间隔：1小时");
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void initChart(ArrayList<String> timeList,ArrayList<Integer> valueList,String lineName) {
        lineChart = (LineChart) findViewById(R.id.line_chart);
        LineChartManager.setLineName(lineName);
        //创建图表
        lineData = LineChartManager.initSingleLineChart(this, lineChart, timeList,valueList);
        LineChartManager.initDataStyle(lineChart, lineData, this);
    }
    public void requestHistoricalData(JSONObject joo){
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
                        File outPa=new File(Environment.getExternalStorageDirectory().getPath()+"/digitalWiseMedical");
                        if(!outPa.exists())
                            outPa.mkdir();
                        File f=new File(outPa+"/historicalJson");
                        if (f.exists()){
                            f.delete();
                        }
                        DataOutputStream fdos=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f,true)));
                        DataInputStream fdis=new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

                        DataOutputStream dos=((CommonInstance)getApplication()).getDataOutputStream();
                        dos.writeUTF(jo.toString());
                        dos.flush();

                        DataInputStream dis=((CommonInstance)getApplication()).getDataInputStream();
                        int len=dis.readInt();

                        int size=1024;
                        byte[] bytes=new byte[1024];
                        int read=0,sum=0;
                        while(true){
                            read=dis.read(bytes,0,size);
                            sum+=read;
                            fdos.write(bytes,0,read);
                            fdos.flush();
                            if(sum>=len){
                                break;
                            }
                        }
                        fdos.close();
                        byte[] bytes2=new byte[len];
                        fdis.read(bytes2);
                        fdis.close();

                        dataJson=JSON.parseObject(new String(bytes2));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"获取数据成功",Toast.LENGTH_SHORT).show();
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

