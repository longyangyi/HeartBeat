package com.example.android.appp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;


public class ChooseItem extends AppCompatActivity {


    Button connectBTButton,startUploadButton,startCheckButton,PersonalInfoButton,
            treatRecordButton,historicalDataButton,requestTreatButton;

    int updateValueTime=1000;// 1000 is 1 second

    Handler handler;
    ArrayList<Bean> BeanList;
    ArrayList<Bean> PrintBeanList;
    boolean uploading=false;
    boolean checking;
    boolean coned;
    int avBeat,avOxygen;
    TextView averageBeat,averageOxygen;


    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "blue_tooth_device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_item_ui);
        CommonInstance.getInstance().addActivity(this);

        ActivityCompat.requestPermissions(ChooseItem.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        Button HeartBeatButton=(Button) findViewById(R.id.HRButton);

        //setCertDir();
        PrintBeanList=new ArrayList<Bean>();
        BeanList=new ArrayList<Bean>();
        handler=new Handler();
        averageBeat=(TextView) findViewById(R.id.HRText);
        averageOxygen=(TextView)findViewById(R.id.BLText);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙设备不可用", Toast.LENGTH_LONG).show();
        }
        HeartBeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseItem.this, HeartRateMeasure.class);
                startActivity(intent);
            }
        });

        Button BloodOxygenButton=(Button)findViewById(R.id.BOButton);
        BloodOxygenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseItem.this,BloodOxygenMeasure.class);
                startActivity(intent);
            }
        });

        connectBTButton=(Button)findViewById(R.id.connnectBTButton);
        connectBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChatService!=null) {
                    if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
                        Intent serverIntent = new Intent(ChooseItem.this, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        sleep(10);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                connectBTButton.setText("断开采集设备");
                                            }
                                        });
                                        break;
                                    }
                                }

                            }
                        }).start();
                    } else {
                        mChatService.stop();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectBTButton.setText("连接采集设备");
                            }
                        });
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "未蓝牙连接采集设备", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        startCheckButton=(Button)findViewById(R.id.startCheckButton);
        startCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(mChatService!=null) {
                            if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "未蓝牙连接采集设备", Toast.LENGTH_LONG).show();
                                    }
                                });
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        startCheckButton.setText("开始测量");
                                    }
                                });
                            } else {
                                if (checking) {
                                    checking = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "已停止测量", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            startCheckButton.setText("开始测量");
                                        }
                                    });
                                } else {
                                    try {

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                startCheckButton.setText("停止测量");
                                            }
                                        });
                                        checking = true;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "开始采集数据", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        while (true) {
                                            try {
                                                sleep(updateValueTime);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (!checking) {
                                                break;
                                            }
                                            int sum = 0, HRnum = 0, BOnum = 0;
                                            for (int i = 0; i < PrintBeanList.size(); i++) {
                                                if (PrintBeanList.get(i).type == -3) {
                                                    sum += PrintBeanList.get(i).value;
                                                    HRnum++;
                                                }
                                            }
                                            if (sum > 0 && HRnum > 0) {
                                                avBeat = sum / HRnum;
                                                handler.post(new Runnable() {
                                                    public void run() {
                                                        BeanList.add(new Bean(-3, avBeat));
                                                        averageBeat.setText("" + avBeat);
                                                        if (HeartRateMeasure.charthandler != null) {
                                                            Message m = new Message();
                                                            m.what = avBeat;
                                                            HeartRateMeasure.charthandler.sendMessage(m);
                                                        }
                                                    }
                                                });
                                            }
                                            sum = 0;
                                            for (int i = 0; i < PrintBeanList.size(); i++) {
                                                if (PrintBeanList.get(i).type == -4) {
                                                    sum += PrintBeanList.get(i).value;
                                                    BOnum++;
                                                }
                                            }
                                            if (sum > 0 && BOnum > 0) {
                                                avOxygen = sum / BOnum;
                                                handler.post(new Runnable() {
                                                    public void run() {
                                                        BeanList.add(new Bean(-4, avOxygen));
                                                        averageOxygen.setText(avOxygen + "%");
                                                        if (BloodOxygenMeasure.charthandler != null) {
                                                            Message m = new Message();
                                                            m.what = avOxygen;
                                                            BloodOxygenMeasure.charthandler.sendMessage(m);
                                                        }
                                                    }
                                                });
                                            }
                                            PrintBeanList.clear();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "未蓝牙连接采集设备", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        startUploadButton=(Button)findViewById(R.id.startUploadButton);
        startUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uploading){
                    uploading=false;
                    handler.post(new Runnable(){
                        public void run(){
                            startUploadButton.setText("开始上传");
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"停止上传数据", Toast.LENGTH_LONG).show();
                        }
                    });
                }else {
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    DataOutputStream dos=((CommonInstance)getApplication()).getDataOutputStream();

                                    if(BeanList.isEmpty()){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"未采集到数据", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }else{

                                        //handle Bean List
                                        JSONObject jsonobject=new JSONObject();
                                        JSONArray ja=new JSONArray();
                                        for(int i=0;i<BeanList.size();i++){
                                            switch(BeanList.get(i).type){
                                                case -3:addJsonArray("heartRate",""+BeanList.get(i).time,""+BeanList.get(i).value,ja);
                                                    break;
                                                case -4:addJsonArray("bloodOxygen",""+BeanList.get(i).time,""+BeanList.get(i).value,ja);
                                                    break;
                                                default:break;
                                            }
                                        }
                                        jsonobject.put("data",ja);
                                        setStartAndLastValue(jsonobject);

                                        File outPa=new File(Environment.getExternalStorageDirectory().getPath()+"/digitalWiseMedical");
                                        if(!outPa.exists())
                                            outPa.mkdir();
                                        File f=new File(outPa+"/acquisitionJson");
                                        if (f.exists()){
                                            f.delete();
                                        }
                                        DataOutputStream fdos=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f,true)));

                                        //fdos.writeUTF(jsonobject.toString());
                                        fdos.write(jsonobject.toString().getBytes());
                                        fdos.flush();
                                        jsonobject.clear();

                                        fdos.close();

                                        JSONObject jo=new JSONObject();
                                        jo.put("type","acquisitionData");

                                        dos.writeUTF(jo.toString());
                                        dos.flush();

                                        jo.clear();
                                        //read Stream from File to Server
                                        DataInputStream fdis=new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

                                        int len=(int)f.length();
                                        dos.writeInt(len);
                                        dos.flush();

                                        byte[] bytes=new byte[len];
                                        fdis.read(bytes);
                                        fdis.close();
                                        f.delete();

                                        dos.write(bytes,0,len);
                                        dos.flush();

                                        BeanList.clear();

                                        DataInputStream dis=((CommonInstance)getApplication()).getDataInputStream();
                                        JSONObject response= JSON.parseObject(dis.readUTF());
                                        if(response.getInteger("ErrCode")==0){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),"上传成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }else{
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),"上传失败", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        });

        requestTreatButton=(Button)findViewById(R.id.requestTreatButton);
        requestTreatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseItem.this,RequestTreat.class);
                startActivity(intent);
            }
        });

        historicalDataButton=(Button)findViewById(R.id.historicalDataButton);
        historicalDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseItem.this,HistoricalDataList.class);
                startActivity(intent);
            }
        });

        treatRecordButton=(Button)findViewById(R.id.historicalTreatRecordButton);
        treatRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseItem.this,TreatRecord.class);
                startActivity(intent);
            }
        });

        PersonalInfoButton=(Button)findViewById(R.id.personalInfo);
        PersonalInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseItem.this,PersonalInfo.class);
                startActivity(intent);
            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null)
                mChatService = new BluetoothService(this, mHandler);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                mChatService.start();
            }
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            handler.post(new Runnable(){
                                public void run(){
                                    connectBTButton.setText("连接采集设备");
                                    startCheckButton.setText("开始测量");
                                    checking=false;
                                }
                            });
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    handleString(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "成功连接到"
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    mChatService = new BluetoothService(this, mHandler);
                } else {
                    Toast.makeText(this, "蓝牙不可用，请手动连接蓝牙设备", Toast.LENGTH_LONG).show();
                }
        }
    }
    public void handleString(String s){
        try{
            int[] nums=new int[10];
            int n=0;
            char[] a=s.toCharArray();

            for(int i=0;i<a.length-1;i++) {
                if(a[i]=='-') {
                    char [] d=new char[2];
                    d[0]='-';
                    d[1]=a[i+1];
                    try{
                        nums[n++] = Integer.parseInt(new String(d));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    char[] b=new char[10];
                    int j,k;
                    for(j=i+2,k=0;j<a.length;j++,k++) {
                        if(a[j]=='-')
                            break;
                        b[k]=a[j];
                    }
                    char[] c=new char[k];
                    for(j=0;j<k;j++)
                        c[j]=b[j];
                    try {
                        nums[n++] = Integer.parseInt(new String(c));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            for(int i=0;i<n;i++) {
                switch(nums[i]) {
                    case -3://Heart Rate
                        if(nums[i+1]>0&&checking) {
                            PrintBeanList.add(new Bean(-3, nums[i + 1]));
                        }
                        break;
                    case -4:
                        if(nums[i+1]>0&&checking) {
                            PrintBeanList.add(new Bean(-4, nums[i + 1]));
                        }
                        break;
                    default:
                        break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(ChooseItem.this, Login.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
    //JSON Util
    public void addJsonArray(String dataType,String time,String value,JSONArray ja) {
        HashMap<String ,Object> hashmap=new HashMap<String,Object>();
        hashmap.put("dataType",dataType);
        hashmap.put("time", time);
        hashmap.put("value", value);
        ja.add(hashmap);
        //hashmap.clear();
    }

    public void setStartAndLastValue(JSONObject jo) {
        try {
            String xllast=null,xylast=null,xlstart=null,xystart=null;
            JSONArray ja=jo.getJSONArray("data");
            int last=0,start=0;
            for(int i=ja.size()-1;i>=0;i--) {
                if(ja.getJSONObject(i).getString("dataType").equals("heartRate")&&xllast==null) {
                    xllast=ja.getJSONObject(i).getString("time");
                    last++;
                }
                if(ja.getJSONObject(i).getString("dataType").equals("bloodOxygen")&&xylast==null) {
                    xylast=ja.getJSONObject(i).getString("time");
                    last++;
                }
                if(last==2) {
                    break;
                }
            }
            for(int i=0;i<ja.size();i++) {
                if(ja.getJSONObject(i).getString("dataType").equals("heartRate")&&xlstart==null) {
                    xlstart=ja.getJSONObject(i).getString("time");
                    start++;
                }
                if(ja.getJSONObject(i).getString("dataType").equals("bloodOxygen")&&xystart==null) {
                    xystart=ja.getJSONObject(i).getString("time");
                    start++;
                }
                if(start==2) {
                    break;
                }
            }

            jo.put("heartRateStartTime", xlstart);
            jo.put("heartRateLastTime", xllast);
            jo.put("bloodOxygenStartTime", xystart);
            jo.put("bloodOxygenLastTime", xylast);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
