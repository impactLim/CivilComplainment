package com.example.sehoon;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // 신고게시판 리사이클러뷰 선언
    RecyclerView ReportRecyclerview;
    // 리사이클러뷰랑 데이터 연결해줄 어댑터 선언
    AdapterReport ReportAdapter;
    // 신고들 담길 어레이리스트 선언
    ArrayList<DataReport> DataReportArraylist;
    // 신고게시판 레이아웃매니저
    LinearLayoutManager reportlayoutManager;


    // 신고하기 버튼
    Button addReport_btn;

    // 글스기 요청번호
    private static final int REQ_WRITE_REPORT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //리사이클러뷰 연결
        ReportRecyclerview = findViewById(R.id.recyclerview_report);

        // 레이아웃매니저 연결
        reportlayoutManager = new LinearLayoutManager(this);
        ReportRecyclerview.setLayoutManager(reportlayoutManager);

        // 실제 데이터가 들어갈 리스트 생성
        DataReportArraylist = new ArrayList<>();
        // 데이터가 담긴 리스트를 넣어주며 어댑터 생성
        ReportAdapter = new AdapterReport(DataReportArraylist,this);

        // 데이터가 있는 어댑터를 리사이클러뷰에 연결
        ReportRecyclerview.setAdapter(ReportAdapter);

        // 신고하기
        addReport_btn = (Button) findViewById(R.id.Report_button);
        addReport_btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){

                // 신고하기 추가하는 화면으로 고고
                Intent i = new Intent(getApplicationContext(), Writereport.class);
                startActivityForResult(i, REQ_WRITE_REPORT);
                // startActivityForResult()로 Activity 호출하기
                // 기존에 startActivity()로 호출하던 것을 startActivityForResult()로 호출을 하면서 인수를 하나 추가해 줍니다.
                // 이 인수는 0보다 크거나 같은 integer 값으로 추후 onActivityResult() 메소드에도 동일한 값이 전달되며
                // 이를 통해 하나의 onActivityResult() 메소드에서 (만약 있다면) 여러 개의 startActivityForResult()를 구분할 수 있습니다.

            }

        });


    }

    // 입력한 신고제목에 대한 결과값을 리사이클러뷰에 하나씩 쌓아줌
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 요청코드가 1번이고 결과코드가 OK일 때
        if (requestCode == REQ_WRITE_REPORT) {
            if (resultCode == Activity.RESULT_OK) {

                // 신고하기 화면으로부터 "et_title" 키값에 담긴 값을 string 타입의 Title에 넣어줌
                String Title = data.getStringExtra("et_title");
                Log.v("메인 신고하기 추가  확인 알림 로그", "Title : " + Title);

                // 신고하기 화면으로부터 "et_category" 키값에 담긴 값을 string 타입의 Category에 넣어줌
                String Category = data.getStringExtra("et_category");
                Log.v("메인 신고하기 추가  확인 알림 로그", "Category : " + Category);

                // 신고하기 화면으로부터 "et_image" 키값에 담긴 값을 String 타입의 Image에 넣어줌
                String Image = data.getStringExtra("et_image");
                Log.v("메인 신고하기 추가 확인 알림 로그", "Image : " + Image);

                // 신고하기 화면으로부터 "et_reporttime" 키값에 담긴 값을 String 타입의 ReportTime에 넣어줌
                String ReportTime = data.getStringExtra("et_reporttime");
                Log.v("메인 신고하기 추가 확인 알림 로그", "ReportTime : " + ReportTime);

                // 위도 받아옴
                double Latitude = data.getDoubleExtra("et_latitude",0);
                // 경도 받아옴
                double Longitude = data.getDoubleExtra("et_longitude",0);
                Log.v("메인 Latitude Longitude 확인 알림 로그", " Latitude " + Latitude + "Longitude " + Longitude);



                // string 타입에 담긴 str_dreamname 값을 데이터클래스의 객체가 생성될 때 생성자로 넣어줌
                DataReport datareport = new DataReport(Title, Category, Image, ReportTime);
                Log.v("신고하기 데이터 클래스 객체 생성 확인 알림 로그", " 데이터클래스 객체 생성 ");

                DataReportArraylist.add(0, datareport); //첫 줄에 삽입
                Log.v("신고하기 데이터 클래스 객체를 arraylist에 넣어줌 확인 알림 로그", " 데이터클래스 객체를 arraylist에 넣어줌 ");

                ReportAdapter.notifyDataSetChanged(); //변경된 데이터를 화면에 반영

            } else {

                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();

            }
        }

    }
}
