package com.example.sehoon;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterReport extends RecyclerView.Adapter<AdapterReport.ViewHolder> {

    // context 권한 필요
    private Context mreportContext;
    private ArrayList<DataReport> dataReportArrayList;

    public AdapterReport(ArrayList<DataReport> dataReportArrayList, MainActivity mainActivity) {
        this.dataReportArrayList = dataReportArrayList;
        this.mreportContext = mainActivity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // 리사이클러뷰에 필요한 것들 선언
        // 신고제목
        TextView tv_reporttitle;
        // 카테고리
        TextView tv_reportcategory;
        // 이미지
        ImageView iv_reportimage;
        // 시간
        TextView tv_reporttime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 리사이클러뷰에 보여질 아이템 뷰들을 참조해준다.
            // 신고제목
            tv_reporttitle = itemView.findViewById(R.id.item_reporttitle);
            // 카테고리
            tv_reportcategory = itemView.findViewById(R.id.item_reportcategory);
            // 이미지
            iv_reportimage = itemView.findViewById(R.id.report_imageview);
            // 시간
            tv_reporttime = itemView.findViewById(R.id.report_titme);

        }
    }

    // 이 아이템 뷰에 한 컬럼 당 들어갈 에이아웃이 객체화 되어서 들어갈 것임
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 레이아웃 인플레이터 서비스를 사용하기 위해 context 가져옴 - 시스템 정보들에 대한 권한
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 한 컬럼에 들어갈 레이아웃을 객체화시켜서 view에 담는다.
        View view = inflater.inflate(R.layout.item_report, parent, false) ;
        // 객체화 시킨 걸 뷰홀더에 담아준다.
        AdapterReport.ViewHolder vh = new AdapterReport.ViewHolder(view) ;

        return vh;    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DataReport item = dataReportArrayList.get(position);

        // 데이터와 리사이클러뷰 아이템 연결시켜줌
        holder.tv_reporttitle.setText(item.getReportTitleStr()); // 신고제목
        holder.tv_reportcategory.setText(item.getReportCaegoryStr()); // 신고카테고리
        holder.iv_reportimage.setImageURI(Uri.parse(item.getReportImageStr())); // 신고이미지
        holder.tv_reporttime.setText(dataReportArrayList.get(position).getReporttimeStr()); // 신고시간

    }

    // 전체 데이터 갯수 반환
    @Override
    public int getItemCount() {
        return (null != dataReportArrayList ? dataReportArrayList.size() : 0);
    }

}
