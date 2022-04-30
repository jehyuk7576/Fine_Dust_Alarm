package com.example.fine_dust_alarm;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class MyLocationFinder extends Service implements LocationListener {

    private final Context mContext;
    LocationManager locationManager;
    Location location;
    double latitude;
    double longitude;

    private static final long MIN_TIME_BW_UPDATES = 10;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000 * 60 * 1;


    public MyLocationFinder(Context context) {
        this.mContext = context;
        getLocation();
    }


    public Location getLocation(){
        try{
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if ((isGPSEnabled == false) && (isNetworkEnabled == false)){
                // 사용 불가
                throw new Exception();
            }
            else {
                // 사용 가능

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);

                if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    throw new Exception();
                }

                if (isGPSEnabled) {
                    // GPS 사용 가능
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                else {
                    // GPS 사용 불가
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            Log.d("[DEBUG MESSAGE]", e.toString());
        }
        return location;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public void onLocationChanged(Location location){

    }
    @Override
    public void onProviderDisabled(String provider){

    }
    @Override
    public void onProviderEnabled(String provider){

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }
    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }
}