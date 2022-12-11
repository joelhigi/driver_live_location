package com.specialops.loctrackerjava.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.specialops.loctrackerjava.models.Journey;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationSharingService extends Service implements LocationListener {
    public LocationSharingService() {
    }
    private LocationManager lm;
    private Double currentLat;
    private Double currentLong;
    private boolean isSharingActive;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;

    private FirebaseDatabase tttFireBase;
    private DatabaseReference dbRef;
    private String dbRefAddress;
    private String uid;
    private String route;
    private String userName;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(getApplicationContext());



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        try {
            if(intent != null){
                uid = intent.getStringExtra("userID");
                userName = intent.getStringExtra("userName");
                route = intent.getStringExtra("chosenRoute");

                tttFireBase = FirebaseDatabase.getInstance("https://noble-radio-299516-default-rtdb.europe-west1.firebasedatabase.app/");
                dbRefAddress = "journeys/routes/"+route;
                dbRef = tttFireBase.getReference(dbRefAddress);

                try{
                    lm = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME, MIN_DIST,LocationSharingService.this);
                }catch(Exception e)
                {
                }
            }
        } catch (Throwable e) {
        }
        return START_REDELIVER_INTENT;

        //return Service.START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Journey disableSharing = new Journey(uid,userName,currentLat,currentLong,false);
        dbRef.setValue(disableSharing);
        lm.removeUpdates(this);
    }

    public void updateCurrentLocation()
    {
        Journey here = new Journey(uid,userName,currentLat,currentLong,true);
        dbRef.setValue(here);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLat = location.getLatitude();
        currentLong = location.getLongitude();
        Log.e("Addr",dbRefAddress);
        updateCurrentLocation();

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


}