package com.example.android.appp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;


public class DoctorListActivity extends Activity {

    ArrayAdapter<String> doctorItems;
    ListView list;
    JSONArray doctorListJsonArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_doctor_list);

        Intent intent=getIntent();
        doctorListJsonArray= JSON.parseArray(intent.getExtras().getString("extra"));

        doctorItems= new ArrayAdapter<String>(this, R.layout.blue_tooth_device_name);
        for(int i=0;i<doctorListJsonArray.size();i++){
            doctorItems.add(doctorListJsonArray.getJSONObject(i).getString("doctorInfo"));
        }
        list=(ListView)findViewById(R.id.doctorListView);
        list.setAdapter(doctorItems);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String s=(String) parent.getItemAtPosition(position);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(DoctorListActivity.this, RequestTreat.class);
                        intent.putExtra("extra",s);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
            }
        });
    }
}
