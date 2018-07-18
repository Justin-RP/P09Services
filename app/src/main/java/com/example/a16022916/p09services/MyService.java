package com.example.a16022916.p09services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MyService extends Service {

    boolean started;
    String folderLocation;
    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;


    @Override
    public IBinder onBind(Intent intent) {
//        private final IBinder mBinder = new LocalBinder();
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        client = LocationServices.getFusedLocationProviderClient(this);
        Log.d("Service","Service created");
        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        if (started == false) {
            started = true;
            Log.d("Service","Service started");
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location loc = locationResult.getLastLocation();
                        double lat = loc.getLatitude();
                        double lng = loc.getLongitude();
                        File targetFile = new File(folderLocation, "data.txt");

                        try {
                            FileWriter writer = new FileWriter(targetFile, true);
                            writer.write(lat + "," + lng);
                            writer.flush();
                            writer.close();
                        } catch (Exception e) {
                            Toast.makeText(MyService.this, "Failed to write!",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            };
            createFolder();
        } else {
            if (checkPermission() == true) {
                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setSmallestDisplacement(100);

                client.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
            } else {
                String msg = "Permission not granted to retrieve location info";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Service is still running", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        Log.d("Service","Service exited");
        client.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }
    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void createFolder(){
        folderLocation = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/MyFolder";

        File folder = new File(folderLocation);
        if (!folder.exists()){
            boolean result = folder.mkdir();
            if (result == true){
                Log.d("File Read/Write", "Folder created");
            } else {
                Log.e("File Read/Write", "Folder creation failed");
            }
        }
    }
}

