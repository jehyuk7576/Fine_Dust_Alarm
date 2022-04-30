package com.example.fine_dust_alarm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button location_btn;
    private Button dust_btn;
    private TextView location_textview;
    private TextView dust_textview;

    private MyLocationFinder locationFinder;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkLocationServicesStatus() == false){
            showDialogForLocationServiceSetting();
        }
        else{
            checkRunTimePermission();
        }

        location_btn = findViewById(R.id.button_loc);
        location_textview = findViewById(R.id.textView_loc);

        dust_btn = findViewById(R.id.button_dust);
        dust_textview = findViewById(R.id.textView_dust);

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationFinder = new MyLocationFinder(MainActivity.this);
                double latitude = locationFinder.getLatitude();
                double longitude = locationFinder.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                location_textview.setText(address);

                Toast.makeText(MainActivity.this, "위도: "+latitude+
                        "\n경도: "+longitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude,5);
        } catch(IOException ioException){
            String errMsg = "IO error 발생";
            return errMsg;
        } catch(IllegalArgumentException illegalArgumentException) {
            String errMsg = "잘못된 GPS 좌표";
            return errMsg;
        }

        if (addresses == null || addresses.size() == 0){
            String errMsg = "주소 미발견";
            return errMsg;
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return result;
    }

    public void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+
                "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            // 권한 있음
            return ;
        }
        else {
            // 권한 없음
            String msg = "이 앱을 실행하려면 위치 접근 권한이 필요합니다.";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }
    }
}