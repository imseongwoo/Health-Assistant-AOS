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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    BarChart barChart_chest;
    BarChart barChart_back;
    BarChart barChart_lower;
    Button back_btn;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList<String> labelsNames = new ArrayList<>();

    ArrayList<DateCountsData> dateCountsDataArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        back_btn = findViewById(R.id.back_btn);
        barChart_chest = findViewById(R.id.barChart_chest);
        barChart_back = findViewById(R.id.barChart_back);
        barChart_lower = findViewById(R.id.barChart_lower);

        barEntryArrayList = new ArrayList<>();
        //labelsNames = new ArrayList<>();

//        barEntryArrayList.clear();
//        labelsNames.clear();

//        fillDateCounts();
//  실시간 DB 참조 위치(health/momentum) 설정
    CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.getUid()).addValueEventListener(new ValueEventListener(){
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            dateCountsDataArrayList.clear();    // 리스트 초기화
            barEntryArrayList.clear();
            labelsNames.clear();

            for (DataSnapshot postSnapshot: snapshot.getChildren()) {

                //데이터 가져오기(count, timestamp 이름으로 된 값)
                String date = postSnapshot.child("timestamp").getValue(String.class);
                String counts = postSnapshot.child("count").getValue(String.class);
                //
                Log.i("차트 date 확인", "date = " + date);
                Log.i("차트 counts 확인", "counts = " + counts);
                dateCountsDataArrayList.add(new DateCountsData(date, counts));
            }

            Log.i("dateCountsDataArrayList", "date = " + dateCountsDataArrayList.get(1).getDate());
            Log.i("dateCountsDataArrayList", "counts = " + dateCountsDataArrayList.get(1).getCounts());

            for (int i=0; i<dateCountsDataArrayList.size(); i++) {
                String date = dateCountsDataArrayList.get(i).getDate();
                String counts = dateCountsDataArrayList.get(i).getCounts();

                barEntryArrayList.add(new BarEntry(i, Integer.parseInt(counts)));
                labelsNames.add(date);
            }

            Log.i("날짜", "date(2) = " + labelsNames.get(1));

            BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "날짜별 부위별 운동 개수");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            Description description = new Description();
            description.setText("날짜");

            barChart_chest.setDescription(description);
            barChart_back.setDescription(description);
            barChart_lower.setDescription(description);

            BarData barData = new BarData(barDataSet);
            barChart_chest.setData(barData);
            barChart_back.setData(barData);
            barChart_lower.setData(barData);

            graphInitSetting(barChart_chest);     // 기본 세팅
            graphInitSetting(barChart_back);
            graphInitSetting(barChart_lower);

            // 가장 최근에 추가한 데이터의 위치로 이동처리
            barChart_back.moveViewToX(barDataSet.getEntryCount());
            barChart_chest.moveViewToX(barDataSet.getEntryCount());
            barChart_lower.moveViewToX(barDataSet.getEntryCount());

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
    public void graphInitSetting(BarChart barChart){

        // 배경 색
        barChart.setBackgroundColor(Color.rgb(254,247,235));
        // 그래프 터치 가능
        barChart.setTouchEnabled(true);
        // X축으로 드래그 가능
        barChart.setDragXEnabled(true);
        // Y축으로 드래그 불가능
        barChart.setDragYEnabled(false);
        // 확대 불가능
        barChart.setScaleEnabled(false);
        // pinch zoom 가능 (손가락으로 확대축소하는거)
        barChart.setPinchZoom(true);

        // 최대 x좌표 기준으로 몇개를 보여줄지 (최소값, 최대값)
        barChart.setVisibleXRange(1, 7);

        // x축 값 포맷
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));

        // x축 라벨 네임 위치 지정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        barChart.animateY(2000);
        barChart.invalidate();

        // y축 설정
        YAxis yAxis = barChart.getAxisLeft();
        barChart.getAxisRight().setEnabled(false);
        yAxis.setAxisMinimum(0f);
        yAxis.setSpaceMax(1f);
        yAxis.setSpaceMin(1f);
    }

}