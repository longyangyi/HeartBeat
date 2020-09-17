package com.example.android.appp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import static java.lang.Thread.sleep;

public class HeartRateMeasure extends Activity {

    private String title = "心率";
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private int addX = -1, addY;

    int[] xv = new int[100];
    int[] yv = new int[100];
    public static Handler charthandler;

    TextView HeartRateText;
    int value;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the window layout
        setContentView(R.layout.heart_rate_measure_layout);
        CommonInstance.getInstance().addActivity(this);

        ActivityCompat.requestPermissions(HeartRateMeasure.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        context = getApplicationContext();
        //这里获得main界面上的布局，下面会把图表画在这个布局里面
        LinearLayout layout = (LinearLayout) findViewById(R.id.HeartRateChart);

        //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        series = new XYSeries(title);

        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();

        //将点集添加到这个数据集中
        mDataset.addSeries(series);

        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color = Color.GREEN;
        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color, style, true);

        //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0, 10, 30, 210, Color.WHITE, Color.WHITE);

        //生成图表
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);

        //将图表添加到布局中去
        layout.addView(chart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        //这里的Handler实例将配合下面的Timer实例，完成定时更新图表的功能
        charthandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //刷新图表
                value=msg.what;
                updateChart(value);
                this.post(new Runnable(){
                    public void run(){
                        HeartRateText.setText(""+value);
                    }
                });
                super.handleMessage(msg);
            }
        };

        HeartRateText = (TextView) findViewById(R.id.HeartRateChartTextValue);

    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(5);
        r.setDisplayChartValues(true);
        r.setChartValuesTextSize(30);//设置数值的字体大小
        r.setDisplayChartValuesDistance(10);//设置数值与样式点的距离

        renderer.addSeriesRenderer(r);

        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有关对图表的渲染可参看api文档
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GREEN);
        renderer.setXLabels(0);
        renderer.setYLabels(10);
        renderer.setXTitle("Time");
        renderer.setYTitle("心率");
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setPointSize((float) 5);
        renderer.setShowLegend(true);
        renderer.setMarginsColor(Color.WHITE);//画布距数轴之间的颜色
        renderer.setAxesColor(Color.BLACK);//设置坐标轴颜色
        renderer.setYLabelsColor(0, Color.BLACK);//Y轴上标签的字体颜色
        renderer.setXLabelsColor(Color.BLACK);//X轴上标签的字体颜色
        renderer.setZoomEnabled(false, false);//设置不可缩放
        renderer.setPanEnabled(false, false);//不允许X轴可拉动
        renderer.setGridColor(Color.WHITE);//设置网格颜色
    }

    private void updateChart(int value) {

        //设置好下一个需要增加的节点
        addX = 0;
        addY=value;
       // addY = (int)(Math.random() * 90);

        //移除数据集中旧的点集
        mDataset.removeSeries(series);

        //判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
        int length = series.getItemCount();
        if (length > 10) {
            length = 10;
        }

        //将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i) + 1;
            yv[i] = (int) series.getY(i);
        }

        //点集先清空，为了做成新的点集而准备
        series.clear();

        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        //这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        series.add(addX, addY);
        for (int k = 0; k < length; k++) {
            series.add(xv[k], yv[k]);
        }

        //在数据集中添加新的点集
        mDataset.addSeries(series);

        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart.invalidate();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(HeartRateMeasure.this, ChooseItem.class);
            Bundle b = new Bundle();
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}