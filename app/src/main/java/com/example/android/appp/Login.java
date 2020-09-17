package com.example.android.appp;

import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import  android.content.Intent;
import android.widget.CheckBox;
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
import java.io.InputStream;
import java.net.Socket;

public class Login extends AppCompatActivity {

    Button connect,registe;
    EditText ip;
    DataInputStream dis;
    Socket server;
    EditText userName;
    EditText password;
    CheckBox show;
    DataOutputStream dos;
    boolean cond=false;
    boolean received=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ActivityCompat.requestPermissions(Login.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        CommonInstance.getInstance().addActivity(this);

        Button loginBtn=(Button) findViewById(R.id.loginBtn);
        userName=(EditText)findViewById(R.id.userId);
        password=(EditText)findViewById(R.id.pass);
        server=null;

        //setCertDir();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (cond) {
                            String id = userName.getText().toString();
                            String passWord = password.getText().toString();

                            try {

                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("type","login");
                                jsonObject.put("access",id);
                                jsonObject.put("password",passWord);

                                dos.writeUTF(jsonObject.toString());
                                dos.flush();

                                receiveJsonUTF();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "未连接服务器", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        show=(CheckBox) findViewById(R.id.checkBox1);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(show.isChecked()){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });


        Button enter=(Button)findViewById(R.id.enterButton);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ChooseItem.class);
                Bundle b = new Bundle();
                startActivity(intent);
            }
        });
        ip=(EditText)findViewById(R.id.ip);
        connect= (Button)findViewById(R.id.button_connect);

        connect.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                new Thread(){
                    public void run(){
                        try {
                            server=new Socket(ip.getText().toString(),8888);

                            dos=new DataOutputStream(new BufferedOutputStream(server.getOutputStream()));
                            dis=new DataInputStream(new BufferedInputStream(server.getInputStream()));
                            ((CommonInstance)getApplication()).setDataOutputStream(dos);
                            ((CommonInstance)getApplication()).setDataInputStream(dis);
                            ((CommonInstance)getApplication()).setServer(server);
                            cond=true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "连接到服务器", Toast.LENGTH_LONG).show();
                                }
                            });

                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        registe=findViewById(R.id.toRegiste);
        registe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

    }

    long mExitTime=0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(Login.this, "再按一次退出智慧医疗", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            CommonInstance.getInstance().exit(this);
        }
    }

    public void receiveJsonUTF() {
        try {
            final String jsonString = dis.readUTF();
            JSONObject jo= JSON.parseObject(jsonString);
            if(jo.getInteger("status")==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
                ((CommonInstance)getApplication()).setAccessAndPassWord(userName.getText().toString(),password.getText().toString());
                Intent intent = new Intent(Login.this, ChooseItem.class);
                startActivity(intent);
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
