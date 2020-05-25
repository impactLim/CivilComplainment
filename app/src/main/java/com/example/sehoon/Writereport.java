package com.example.sehoon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Writereport extends AppCompatActivity {

    // 타이틀
    EditText et_title;

    //    String selecteditem;
    // 카테고리
    String Category;

    // 이미지
    Uri Image;

    // 현재시간을 msec 으로 구한다.
    private long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    private Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    // private SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd E요일 HH:mm:ss");
    // ss or mm 생각
    // dateNow 변수에 값을 저장한다.
    private String ReportTime = sdfNow.format(date);

    // 완료버튼
    Button upload_report;

    // https://www.youtube.com/watch?v=FcMiw16bouA 참고
    private Spinner ReportSpinner;

    // 권한 확인 Boolean isPermission
    private Boolean isPermission = true;

    // 이는 이미지를 리사이징 하는 단계에서 카메라에서 온 화면인지 앨범에서 온 화면인지 구분할 용도입니다
    private Boolean isCamera = false;

    // startactivityforresult 갤러리와 카메라 요청번호
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    // tempFile 에 받아온 이미지를 저장
    private File tempFile;

    // 로그
    private static final String TAG = "image check 로그 알림";


    // GPS
    private GpsTracker gpsTracker;

    //  위치 접근 권한과 GPS 사용 가능 여부를 체크해서 처리
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // 위도와 경도
    double latitude;
    double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writereport);


        // 위치 관련 코드
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        final TextView Location = (TextView)findViewById(R.id.Report_location);



        gpsTracker = new GpsTracker(Writereport.this);

        // double latitude
        latitude = gpsTracker.getLatitude();
        // double longitude
        longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        Location.setText(address);

        Toast.makeText(Writereport.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();



        // 카메라, 갤러리 등 권한 요청을 해준다.
        tedPermission();

        // 갤러리를 눌렀을 때
        findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의되어 있을 때 앨범으로 간다.
                if(isPermission)
                    goToAlbum();
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                // 사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.
                else
                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 사진찍기
                if(isPermission)
                    takePhoto();
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                // 사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.
                else
                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });


        // 스피너 참조
        // Spinner 는 드롭다운되어 목록 중 하나를 선택하는 위젯
        ReportSpinner = findViewById(R.id.reportSpinner);

        // categories Arraylist - 실제로 문자열 데이터를 저장하는데 사용할 ArrayList 객체를 생성
        ArrayList<String> categories = new ArrayList<>();
        // ArrayList 객체에 데이터를 집어넣는다.
        categories.add(0, "카테고리 선택");
        categories.add("소화전");
        categories.add("교차로 모퉁이");
        categories.add("버스정류소");
        categories.add("횡단보도");

        // ArrayList 객체와 스피너 객체를 연결하기 위해 ArrayAdapter객체를 사용합니다.
        // Spinner, ListView 등 많은 데이터 처리 시 ArrayAdapter 를 이용한다
        // 우선 ArrayList 객체를 ArrayAdapter 객체에 연결합니다.
        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, // 한 줄에 하나의 텍스트 아이템만 보여주는 레이아웃 파일
                categories // 데이터가 저장되어 있는 ArrayList 객체
        );

        // Adapter 의 setDropDownViewResource() 메소드를 통해 dropdown 의 레이아웃 (spin_dropdown.xml) 을 지정해줍니다
        //출처: https://bitsoul.tistory.com/44 [Happy Programmer~]
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 스피터 객체에 adapter 객체를 연결해줌
        ReportSpinner.setAdapter(dataAdapter);

        // Spinner 는 ListView 와 달리 setOnItemSelectedListener 를 이용하여 이벤트 처리
        ReportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // onItemSelected 에서 position 변수로 몇번째 값이 선택됬는지를 확인할 수 있다
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // 카테고리 선택시
                if(parent.getItemAtPosition(position).equals("카테고리 선택"))
                {
                    // do nothing
                }
                // 그 외 선택시
                else{
                    // on selecting a spinner item
                    // String Category
                    Category = parent.getItemAtPosition(position).toString();

                    // show selected spinner item
                    Toast.makeText(parent.getContext(),"카테고리 : " + Category, Toast.LENGTH_SHORT).show();

                    //anything else you want to do on item selection do here

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing

            }
        });

        et_title = findViewById(R.id.Report_Title);

        //글쓰기 화면의 완료 버튼
        upload_report = findViewById(R.id.ReportAddButton);
        upload_report.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // 신고제목이 공백이라면
                if(et_title.getText().toString().equals("")) {

                    Toast.makeText(getApplicationContext(), "신고제목을 입력해주세요!", Toast.LENGTH_SHORT).show();

                // 카테고리를 선택하지 않았으면
                // 카테고리 선택했다가 다시 카테고리 선택을 누르면 이전에 선택한 카테고리가 표시됨 > '카테고리 선택'이면 신고하기가 되지 않도록 해야함
                }else if(Category == null) {

                    Toast.makeText(getApplicationContext(), "카테고리를 선택해주세요!", Toast.LENGTH_SHORT).show();

                // image가 null이라면
                }else if(Image == null){
                    Toast.makeText(getApplicationContext(), "이미지를 등록해주세요!", Toast.LENGTH_SHORT).show();

                }else{
                    Intent intent = new Intent();

                    intent.putExtra("et_title", et_title.getText().toString());
                    Log.v("et_title 확인 알림 로그","값 확인 : " + et_title.getText().toString());

                    // String Category
                    intent.putExtra("et_category", Category);
                    Log.v("et_category 확인 알림 로그","값 확인 : " + Category);

                    // Uri Image
                    if(Image != null){
                        intent.putExtra("et_image", Image.toString());
                        Log.v("et_image 확인 알림 로그","값 확인 : " +  Image.toString());
                    }

                    // String ReportTime
                    intent.putExtra("et_reporttime",ReportTime);

                    // double latitude
                    intent.putExtra("et_latitude", latitude);

                    // double longitude
                    intent.putExtra("et_longitude", longitude);

                    Log.v("신고하기 화면 Latitude Longitude 확인 알림 로그", "latitude  " + latitude + "  longitude  " + longitude);

                    setResult(RESULT_OK, intent);

                    finish(); // 액티비티를 끝낸다.

                }

            }
        });


    }

    /**
     *  권한 설정
     */
    // TedPermission 클래스를 이용해서 권한체크를 시작합니다.
    private void tedPermission() {

        // 권한이 허가되거나 거부당했을때 결과를 리턴해주는 리스너
        //출처: https://gun0912.tistory.com/61 [박상권의 삽질블로그]
        PermissionListener permissionListener = new PermissionListener() {
            // onPermissionGranted()는 권한이 모두 허용 되고나서 실행
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공 - ispermission은 true로
                // Boolean isPermission
                isPermission = true;

            }

            // onPermissionDenied 요청한 권한중에서 거부당한 권한목록을 리턴
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패 - ispermission은 false로
                isPermission = false;

            }
        };

        // 리스너, 요청하는 권한, 권한요청시 필요한 메세지들 등에 대해서 설정을 해주는 작업을 해주고 check()함수를 통해서 실제로 권한체크를 시작
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2)) // 권한을 요청하기전에 이 권한이 필요한 이유에 대해서 설명하는 메세지
                .setDeniedMessage(getResources().getString(R.string.permission_1)) // 사용자가 권한을 거부했을때 보여지는 메세지를 설정
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    // onActivityResult 에서 requestCode 를 앨범에서 온 경우와 카메라에서 온 경우로 나눠서 처리해줍니다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 예외 사항이란 앨범화면으로 이동 했지만 선택을 하지 않고 뒤로 간 경우 또는 카메라로 촬영한 후 저장하지 않고 뒤로 가기를 간 경우입니다.
        // 이는 resultCode 값을 통해 확인할 수 있습니다. 이 경우 "취소 되었습니다.' 토스트를 보여줍니다.
        // 카메라 촬영할 때 createImageFile() 을 통해 temFile 을 생성
        // 만약 사진 촬영 중 취소를 하게 되면 tempFile 이 빈썸네일로 디바이스에 저장됩니다. 따라서 예외 사항에서 tempFIle 이 존재하면 이를 삭제해 주는 작업이 필요합니다.
        super.onActivityResult(requestCode, resultCode, data);

        // 위치 관련 코드
        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }


        // 카메라, 갤러리 관련 코드
        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            // tempFile = 받아온 이미지가 저장되는 곳
            // 이미지가 null 값이 아니라면
            if (tempFile != null) {
                // 저장된 파일이 있는지 확인
                if (tempFile.exists()) {
                    // 파일 삭제
                    if (tempFile.delete()) {
                        // 삭제 로그
                        Log.v(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        // tempfile은 null값으로 처리
                        tempFile = null;
                    }
                }
            }

            return;
        }


        // startActivityForResult 를 통해 다른 Activity 로 이동한 후 다시 돌아오게 되면 onActivityResult 가 동작되게 됩니다.
        // 이때 startActivityForResult 의 두번 째 파라미터로 보낸 값 {여기서는 PICK_FROM_ALBUM}이 requestCode 로 반환되는 동작을 합니다.
        if (requestCode == PICK_FROM_ALBUM) {

            // photoUri라는 Uri값으로 데이터 받아온다
            // Uri Image
            Image = data.getData();
            // Uri를 string값으로 변환 확인
            Image.toString();
            Log.v("album 로그", "확인" + Image.toString());

            // data.getData() 를 통해 갤러리에서 선택한 이미지의 Uri 를 받아 옵니다.
            // 이를 cursor 를 통해 스키마를 content:// 에서 file:// 로 변경해 줍니다.
            // 이는 사진이 저장된 절대경로를 받아오는 과정입니다.
            // DB에서 가져온 데이터를 쉽게 처리하기 위해서 Cursor 라는 인터페이스를 제공
            // Cursor는 기본적으로 DB에서 값을 가져와서 마치 실제 Table의 한 행(Row), 한 행(Row) 을 참조하는 것처럼 사용
            //출처: https://arabiannight.tistory.com/entry/368 [아라비안나이트]
            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                // Projection은 String[]을 인자로 받습니다. 결과로 받고 싶은 column 명을 배열에 입력.
                String[] proj = {MediaStore.Images.Media.DATA};

                // 고의로 앱을 강제종료시킴으로써 버그를 잡아내는 방법
                // uri - 이미지가 null이 아니다
                assert Image != null; // 이거 재확인
                // 데이터 조회 https://mainia.tistory.com/4924 참고
                cursor = getContentResolver().query(Image, proj, null, null, null);

                assert cursor != null;
                // 특정 필드의 인덱스값을 반환하며, 필드가 존재하지 않을경우 예외를 발생시킵니다.
                //출처: https://pulsebeat.tistory.com/15 [Pulse-Beat's Bits-Box [0010 0000 0001 0011]]
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                // 커서가 쿼리(질의) 결과 레코드들 중에서 가장 처음에 위치한 레코드를 가리키도록 합니다.
                cursor.moveToFirst();

                // tempfile에 인덱스값 받아줌
                tempFile = new File(cursor.getString(column_index));

            } finally {
                if (cursor != null) {
                    // 결과행 집합 해제..? 재확인 필요
                    cursor.close();
                }
            }

            // tempfile의 경로를
            setImage();

        // CAMERA 선택 시
        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage();

        }
    }

    // goToAlbum, takePhoto 함수에서 isCamera 의 변수를 설정
    /**
     *  앨범에서 이미지 가져오기
     */
    // 이제 권한을 설정해 주었으니 앨범에서 이미지를 가져오겠습니다.
    private void goToAlbum() {
        isCamera = false;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

        // Intent 를 통해 앨범화면으로 쉽게 이동할 수 있습니다. 이 때 startActivityForResult 에 PICK_FROM_ALBUM 변수를 넣어주었습니다.
        // 이 변수는 onActivityResult 에서 requestCode 로 반환되는 값입니다.
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    /**
     *  카메라에서 이미지 가져오기
     */
    // Intent 를 통해 카메라화면으로 이동할 수 있습니다. 이때 startAcitivtyResult 에는 PICK_FROM_CAMER 를 파라미터로 넣어줍니다
    private void takePhoto() {
        isCamera = true;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        // 카메라로 보내는 tempFile 의 uri 를 provider 로 감싸주는 로직이 추가되었습니다.
        // 안드로이드 누가 하위 버전에서는 provider로 uri 를 감싸주면 동작하지 않는 경우가 있기 때문에 모든 기기에 적용하기 위해서는 버전 구분을 꼭 해주셔야 합니다.
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                // tempFile 의 Uri 경로를 intent 에 추가해 줘야 합니다. 이는 카메라에서 찍은 사진이 저장될 주소를 의미합니다.
                // 예제에서는 tempFile 을 전역변수로 해서 사용하기 때문에 이 tempFile 에 카메라에서 촬연한 이미지를 넣어줄꺼에요!
                // Uri photoUri 대신에 Uri Image
                Image = FileProvider.getUriForFile(this,
                        "com.example.sehoon.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Image);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                // Uri photoUri 대신에 Uri Image
                Image = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Image);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    /**
     *  폴더 및 파일 만들기
     */
    // 카메라에서 찍은 사진을 저장할 파일 만들기
    // 주석에 적어 놓은 것과 같이 파일 명과 폴더명을 직접 정할 수 있습니다.
    // 그럼 카메라에서 찍은 사진을 우리가 생성한 파일에 저장하고 받아올 수 있게 됩니다.
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( report_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "report_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( report )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/report/");
        if (!storageDir.exists()) storageDir.mkdirs(); // mkdirs 한 번에 여러 디렉토리를 생성.

        // 빈 파일 생성
        // tempFile 에 받아온 이미지를 저장
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.v(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    /**
     *  tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    private void setImage() {

        ImageView imageView = findViewById(R.id.imageView);

        // 카메라 회전각도 조정
        ImageResizeUtils.resizeFile(tempFile, tempFile, 1280, isCamera);

        // setImage 단계에서 최종 파일인 tempFile을 리사이징 해줍니다.
        // 첫 번째 파라미터에 변형시킬 tempFile 을 넣었습니다.
        // 두 번째 파라미터에는 변형시킨 파일을 다시 tempFile에 저장해 줍니다.
        // 세 번째 파라미터는 이미지의 긴 부분을 1280 사이즈로 리사이징 하라는 의미입니다.
        // 네 번째 파라미터를 통해 카메라에서 가져온 이미지인 경우 카메라의 회전각도를 적용해 줍니다.(앨범에서 가져온 경우에는 회전각도를 적용 시킬 필요가 없겠죠?)
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 전역 변수 tempFile 의 경로를 불러와 bitmp 파일로 변형한 후 imageView 에 해당 이미지를 넣어줍
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Log.v(TAG, "setImage : " + tempFile.getAbsolutePath());

        imageView.setImageBitmap(originalBm);

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        tempFile = null;

    }

//    위치 자동첨부 관련 코드들

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(Writereport.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(Writereport.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(Writereport.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(Writereport.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)

            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(Writereport.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요.
                Toast.makeText(Writereport.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청. 요청 결과는 onRequestPermissionResult에서 수신.
                ActivityCompat.requestPermissions(Writereport.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청.
                // 요청 결과는 onRequestPermissionResult에서 수신.
                ActivityCompat.requestPermissions(Writereport.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);

        } catch (IOException ioException) {

            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";

        } catch (IllegalArgumentException illegalArgumentException) {

            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Writereport.this);

        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);

        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//
//            case GPS_ENABLE_REQUEST_CODE:
//
//                //사용자가 GPS 활성 시켰는지 검사
//                if (checkLocationServicesStatus()) {
//                    if (checkLocationServicesStatus()) {
//
//                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
//                        checkRunTimePermission();
//                        return;
//                    }
//                }
//
//                break;
//        }
//    }

    public boolean checkLocationServicesStatus() {
        // LocationManager 안드로이드 위치제공자
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // LocationManaer.NETWORK_PROVIDER : 기지국들로부터 현재 위치 확인
        // LocationManaer.GPS_PROVIDER : GPS들로부터 현재 위치 확인
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}
