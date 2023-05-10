package com.example.gymbeacon.ui.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymbeacon.R;
import com.example.gymbeacon.ui.common.CommonUtil;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    // 차트 선언
    BarChart barChart_chest;
    BarChart barChart_back;
    BarChart barChart_lower;
    Button back_btn;
    PieChart pieChart;

    //RadarChart radarChart;   // 레이더 차트

    // 하체 운동 데이터 ArrayList
    ArrayList<BarEntry> barEntryArrayList_lower = new ArrayList<>();
    ArrayList<String> labelsNames_lower = new ArrayList<>();
    ArrayList<DateCountsData> dateCountsDataArrayList_lower = new ArrayList<>();

    // 등 운동 데이터 ArrayList
    ArrayList<BarEntry> barEntryArrayList_back = new ArrayList<>();
    ArrayList<String> labelsNames_back = new ArrayList<>();
    ArrayList<DateCountsData> dateCountsDataArrayList_back = new ArrayList<>();

    // 가슴 운동 데이터 ArrayList
    ArrayList<BarEntry> barEntryArrayList_chest = new ArrayList<>();
    ArrayList<String> labelsNames_chest = new ArrayList<>();
    ArrayList<DateCountsData> dateCountsDataArrayList_chest = new ArrayList<>();

    // 파이 차트 ArrayList
    ArrayList<PieEntry> pieEntryArrayList = new ArrayList<>();
    ArrayList<String> labelsNames_pie = new ArrayList<>();
    ArrayList<DateCountsData> dateCountsDataArrayList_pie = new ArrayList<>();
    int counts_lower = 0;
    int counts_back = 0;
    int counts_chest = 0;

    // 레이더 차트 ArrayList
    //ArrayList<RadarEntry> raderEntry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        back_btn = findViewById(R.id.back_btn);
        barChart_chest = findViewById(R.id.barChart_chest);
        barChart_back = findViewById(R.id.barChart_back);
        barChart_lower = findViewById(R.id.barChart_lower);
        pieChart = findViewById(R.id.pieChart);

        //radarChart = findViewById(R.id.radarChart);


    //  실시간 DB 참조 위치(health/momentum) 설정
    CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.getUid()).addValueEventListener(new ValueEventListener(){

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            dateCountsDataArrayList_lower.clear();    // 리스트 초기화
            barEntryArrayList_lower.clear();
            labelsNames_lower.clear();

            dateCountsDataArrayList_chest.clear();    // 리스트 초기화
            barEntryArrayList_chest.clear();
            labelsNames_chest.clear();

            dateCountsDataArrayList_back.clear();    // 리스트 초기화
            barEntryArrayList_back.clear();
            labelsNames_back.clear();

            dateCountsDataArrayList_pie.clear();     // 파이 차트 리스트 초기화
            pieEntryArrayList.clear();
            labelsNames_pie.clear();

            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                String ex_name = postSnapshot.child("exercise").getValue(String.class);

                // 운동 이름이 가슴 운동인 경우
                if ("벤치프레스".equals(ex_name) || "인클라인 벤치프레스".equals(ex_name) || "케이블 크로스오버".equals(ex_name)) {
                    //데이터 가져오기(count, timestamp 이름으로 된 값)
                    String date = postSnapshot.child("timestamp").getValue(String.class);
                    String counts = postSnapshot.child("count").getValue(String.class);

                    dateCountsDataArrayList_chest.add(new DateCountsData(date, counts));

                    // 가슴 운동 부위의 운동 횟수 설정
                    counts_chest += Integer.parseInt(counts);
                }

                // 운동 이름이 등 운동인 경우
                if ("데드리프트".equals(ex_name) || "턱걸이".equals(ex_name)) {
                    //데이터 가져오기(count, timestamp 이름으로 된 값)
                    String date = postSnapshot.child("timestamp").getValue(String.class);
                    String counts = postSnapshot.child("count").getValue(String.class);

                    dateCountsDataArrayList_back.add(new DateCountsData(date, counts));

                    // 등 운동 부위의 운동 횟수 설정
                    counts_back += Integer.parseInt(counts);
                }

                // 운동 이름이 하체 운동인 경우
                if ("스쿼트".equals(ex_name) || "레그 익스텐션".equals(ex_name)) {

                    //데이터 가져오기(count, timestamp 이름으로 된 값)
                    String date = postSnapshot.child("timestamp").getValue(String.class);
                    String counts = postSnapshot.child("count").getValue(String.class);
                    //
                    Log.i("차트 date 확인", "date = " + date);
                    Log.i("차트 counts 확인", "counts = " + counts);
                    dateCountsDataArrayList_lower.add(new DateCountsData(date, counts));

                    // 하체 운동 부위의 운동 횟수 설정
                    counts_lower += Integer.parseInt(counts);
                }
            }

            Log.i("dateCountsDataArrayList", "date = " + dateCountsDataArrayList_lower.get(0).getDate());
            Log.i("dateCountsDataArrayList", "counts = " + dateCountsDataArrayList_lower.get(0).getCounts());

            fillDateCounts(barChart_lower, dateCountsDataArrayList_lower, labelsNames_lower, barEntryArrayList_lower);
            fillDateCounts(barChart_back, dateCountsDataArrayList_back, labelsNames_back, barEntryArrayList_back);
            fillDateCounts(barChart_chest, dateCountsDataArrayList_chest, labelsNames_chest, barEntryArrayList_chest);

            // 부위별 운동의 파이 차트 데이터 설정
            pieEntryArrayList.add(new PieEntry(counts_back, "등"));
            pieEntryArrayList.add(new PieEntry(counts_lower, "하체"));
            pieEntryArrayList.add(new PieEntry(counts_chest, "가슴"));

            PieDataSet pieDataSet = new PieDataSet(pieEntryArrayList, " ← 운동 부위 ");
            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            Description description = new Description();
            description.setText("부위별 운동량");
            description.setTextSize(15);
            pieChart.setDescription(description);

            pieDataSet.setValueFormatter(new PercentFormatter(pieChart));
            PieData pieData = new PieData(pieDataSet);

            pieChart.setData(pieData);

            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            pieChart.animateXY(2000, 2000);
            pieChart.setUsePercentValues(true);     // 퍼센트 값 사용
            // 파이 차트 설정

            // 레이더 차트 설정

            //

        }   //onDataChange

        @Override
        public void onCancelled(@NonNull DatabaseError error) { }//onCancelled
    });     //addValueEventListener

        // 돌아가기 버튼
        back_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //바 차트 기본 설정
    public void graphInitSetting(BarChart barChart, ArrayList<String> labelsNames, ArrayList<DateCountsData> dateCountsDataArrayList){

        // 배경 색
        barChart.setBackgroundColor(Color.rgb(254,247,235));
        // 그래프 터치 가능
        barChart.setTouchEnabled(true);
        // X축으로 드래그 가능
        barChart.setDragXEnabled(true);
        // Y축으로 드래그 불가능
        barChart.setDragYEnabled(false);
        // 차트 확대 가능 여부
        barChart.setScaleEnabled(false);
        // pinch zoom 가능 (손가락으로 확대축소하는거)
        barChart.setPinchZoom(true);

        // 최대 x좌표 기준으로 몇개를 보여줄지 (최소값, 최대값)
        barChart.setVisibleXRange(1, 10);

        // x축 값 포맷
        XAxis xAxis = barChart.getXAxis();

        ArrayList<String> xAxisLabels = new ArrayList<>();


        // SimpleDateFormat 객체를 생성하여 원하는 형식 지정
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");        // 년도
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd");  // 월-일

        for (String dateStr : labelsNames) {
            try {
                Date date = inputFormat.parse(dateStr);
                String xAxisLabel = outputFormat.format(date);
                xAxisLabels.add(xAxisLabel);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setLabelCount(xAxisLabels.size());
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));

        // x축 라벨 네임 위치 지정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        barChart.animateY(2000);
        barChart.invalidate();

        
        // y축 설정
        int max_counts = Integer.parseInt(dateCountsDataArrayList.get(0).getCounts());  // 최대 개수
        for (int i = 0; i<dateCountsDataArrayList.size() - 1; i++) {
            int next_counts = Integer.parseInt(dateCountsDataArrayList.get(i+1).getCounts());

            if ( max_counts < next_counts ) {
                max_counts = next_counts;
            }
        }
        
        YAxis yAxis = barChart.getAxisLeft();
        barChart.getAxisRight().setEnabled(false);
        yAxis.setAxisMinimum(0f);

        yAxis.setGranularity(1f);
        yAxis.setAxisMaximum(max_counts);

        // y축 단위 개수로 변경하는 코드
        yAxis.setValueFormatter(new yAxisValueFormatter());

//        yAxis.setSpaceMax(1f);
//        yAxis.setSpaceMin(1f);
    }

    public void fillDateCounts(BarChart barChart, ArrayList<DateCountsData> dateCountsDataArrayList, ArrayList<String> labelsNames, ArrayList<BarEntry> barEntryArrayList) {
        for (int i = 0; i < dateCountsDataArrayList.size(); i++) {
            String date = dateCountsDataArrayList.get(i).getDate();
            String counts = dateCountsDataArrayList.get(i).getCounts();

            barEntryArrayList.add(new BarEntry(i, Integer.parseInt(counts)));
            labelsNames.add(date);
        }

        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "운동량");

        barDataSet.setValueFormatter(new CountValueFormatter());

        barDataSet.setColors(Color.GRAY);

        Description description = new Description();
        description.setText("날짜");

        barChart.setDescription(description);

        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);

        graphInitSetting(barChart, labelsNames, dateCountsDataArrayList);     // 차트 기본 세팅

        // 가장 최근에 추가한 데이터의 위치로 이동처리
        barChart.moveViewToX(barDataSet.getEntryCount());

    }

}

class CountValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.format(Locale.getDefault(), "%.0f개", value);
    }
}

class yAxisValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.format(Locale.getDefault(), "%.0f", value);
    }
}