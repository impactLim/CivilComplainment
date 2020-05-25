package com.example.sehoon;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

// 한 컬럼에 들어갈 데이터의 기본 틀을 만든다
public class DataReport {

    // 들어가는 데이터들 선언
    // 신고제목
    public String reportTitleStr;

    // 신고 카테고리
    public String reportCaegoryStr;

    // 신고 이미지
    public String reportImageStr;

    // 현재시간을 msec 으로 구한다.
    private long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    private Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    // private SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd E요일 HH:mm:ss");
    // ss or mm 생각
    // dateNow 변수에 값을 저장한다.
    private String reporttimeStr = sdfNow.format(date);

    // 생성자 - 한 묶음으로 묶어줌
    public DataReport(String reportTitle, String category, String image, String reporttime){
        reportTitleStr = reportTitle;
        reportCaegoryStr = category;
        reportImageStr = image;
        reporttimeStr = reporttime;
        Log.v("데이터 클래스 확인 알림 로그","DataReport 생성자 reportTitle :" + reportTitle );
        Log.v("데이터 클래스 확인 알림 로그","DataReport 생성자 category :" + category );
        Log.v("데이터 클래스 확인 알림 로그","DataReport 생성자 image :" + image );
        Log.v("데이터 클래스 확인 알림 로그","DataReport 생성자 reporttime :" + reporttime );
    }

    public String getReportTitleStr() {
        return reportTitleStr;
    }

    public String getReportCaegoryStr() {
        return reportCaegoryStr;
    }

    public String getReportImageStr() {
        return reportImageStr;
    }

    public String getReporttimeStr() {
        return reporttimeStr;
    }
}
