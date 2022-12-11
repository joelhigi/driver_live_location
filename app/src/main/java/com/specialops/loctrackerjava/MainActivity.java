package com.specialops.loctrackerjava;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.specialops.loctrackerjava.managers.UserManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.specialops.loctrackerjava.service.LocationSharingService;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private UserManager userManager = UserManager.getInstance();


    private Switch location_switch;
    private boolean location_service = false;

    private FirebaseFirestore tttFireStore;
    private FirebaseUser tttUser;

    private String uid;
    private String userName;
    private String userRole;
    private String[] routes;
    private String chosenRoute;
    private String refAddress;
    private Double latitude;
    private Double longitude;
    private FirebaseDatabase tttRealTime;
    private DatabaseReference tttRealTimeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!userManager.isCurrentUserLogged()){
            startSignInActivity();
        }

        setContentView(R.layout.activity_main);
        location_switch = findViewById(R.id.driver_location_switch);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 100);

        tttUser = FirebaseAuth.getInstance().getCurrentUser();
        tttFireStore = FirebaseFirestore.getInstance();
        uid = tttUser.getUid();
        userName = tttUser.getDisplayName();

        DocumentReference docRef = tttFireStore.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userRole = document.get("role").toString();
                    } else {
                        Log.d("Failed", "No such document");
                    }
                } else {
                    Log.d("Failed 2", "get failed with ", task.getException());
                }
            }
        });


        location_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                location_service = isChecked;
                if(location_service)
                {
                    routes = new String[]{"Route 1", "Route 2", "Route 3"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Pick a Route");
                    builder.setItems(routes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            chosenRoute = routes[choice];

                            Intent intent = new Intent(getBaseContext(),LocationSharingService.class);
                            intent.putExtra("userRole",userRole);
                            intent.putExtra("userID",uid);
                            intent.putExtra("userName",userName);
                            intent.putExtra("chosenRoute",chosenRoute);
                            startService(intent);

                            tttRealTime = FirebaseDatabase.getInstance("https://noble-radio-299516-default-rtdb.europe-west1.firebasedatabase.app/");
                            refAddress = "journeys/routes/"+chosenRoute;
                            tttRealTimeRef = tttRealTime.getReference(refAddress);

                            Log.e("OO",refAddress);
                            //Listening for LatLng changes
                            ValueEventListener tttRealTimeListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(snapshot.exists()) {
                                        boolean shareStatus = snapshot.child("sharingStatus").getValue(Boolean.class);
                                        if (shareStatus) {
                                            latitude = snapshot.child("latitude").getValue(Double.class);
                                            longitude = snapshot.child("longitude").getValue(Double.class);
                                        }
                                        FragmentManager fragManager = getSupportFragmentManager();
                                        FragmentTransaction fragSwitch = fragManager.beginTransaction();
                                        FragmentContainerView fragContainer = findViewById(R.id.fragmentContainerView);
                                        fragContainer.removeAllViews();
                                        fragSwitch.replace(R.id.fragmentContainerView, new DriverMapFragment(latitude,longitude));
                                        fragSwitch.commit();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Authorization Error","Database Inaccessible");
                                }
                            };
                            tttRealTimeRef.addValueEventListener(tttRealTimeListener);
                        }

                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            location_switch.setChecked(false);
                        }
                    });
                    builder.show();
                }
                else
                {
                    stopService(new Intent(getBaseContext(), LocationSharingService.class));
                }
            }
        });


    }

    private void startSignInActivity(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
//                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
//                        .setLogo(R.drawable.ic_launcher_foreground)
                        .build(),
                RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        this.handleResponseAfterSignIn(requestCode,resultCode,data);
    }

    private void showSnackBar(String message){
        Toast ma;
        ma = Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
        ma.show();
    }


    public void handleResponseAfterSignIn(int requestCode,int resultCode,Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if(requestCode == RC_SIGN_IN){
            //success
            if(resultCode == RESULT_OK){
                userManager.createUser();
                showSnackBar(getString(R.string.connection_succeed));
            } else {
                //ERRORS
                if(response == null){
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else if( response.getError() !=null){
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
                        showSnackBar(getString(R.string.error_no_internet));
                    } else if( response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR){
                        showSnackBar(getString(R.string.error_unknown_error));
                    }
                }
            }
        }
    }


}


