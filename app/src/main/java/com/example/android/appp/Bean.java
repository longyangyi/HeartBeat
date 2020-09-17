package com.example.android.appp;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class Bean implements Serializable {
    int type;
    String time;
    int value;

    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    public  Bean(int t,int v){
        this.type=t;//-4 blood oxygen,-3 heart beat
        this.value=v;

        Calendar c= Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        this.year=c.get(Calendar.YEAR);
        this.month=c.get(Calendar.MONTH)+1;// old Roma 's Month start from 0
        this.day=c.get(Calendar.DAY_OF_MONTH);
        this.hour=c.get(Calendar.HOUR_OF_DAY);
        this.minute=c.get(Calendar.MINUTE);
        this.second=c.get(Calendar.SECOND);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.CHINA);
        time=format.format(c.getTime());
    }
}
