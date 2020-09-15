package com.example.s3727634.afinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location myCurrentLocation;
    private final int ZOOM_LEVEL_DEFAULT = 18;
    LocationCallback mLocationCallback;
    private final LatLng mDefaultLocation = new LatLng(10.777306, 106.696273);


    private boolean isAlreadySetupDestination = false;
    private LatLng destinationToUnlock = null;
    Polyline currentLine = null;

    private final int DESTINATION_APPROACH_DISTANCE = 10; //10m

    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabaseHelper = new DatabaseHelper(this);

        Button btnGame = findViewById(R.id.btnGame);
        btnGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(gameIntent);
            }
        });

        mLocationPermissionGranted = false;

        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.
                            ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getCurrentLocation();
                }
                break;
        }

        updateLocationUI();
    }

    void updateLocationUI() {
        if (mLocationPermissionGranted) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                rePositionMyLocationButton();

            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else  {

            try {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                requestPermission();

            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();

            }

        }
    }

    private void moveCameraToCurrentLocation() {
        if(myCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(myCurrentLocation.getLatitude(),
                            myCurrentLocation.getLongitude()),
                    ZOOM_LEVEL_DEFAULT));
        }
    }

    private void getCurrentLocation() {

        if(mLocationPermissionGranted) {
            try {
                Task<Location> result =
                        mFusedLocationProviderClient.getLastLocation();
                result.addOnCompleteListener(this,
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if(task.isSuccessful()) {
                                    myCurrentLocation = task.getResult();

                                    if(myCurrentLocation != null) {
                                        moveCameraToCurrentLocation();
                                    }
                                }

                            }
                        });



            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();
            }

            requestLocationUpdate();
        }
    }

    public void rePositionMyLocationButton() {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        SupportMapFragment mMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        View locationButton = ((View) mMapFragment.getView().findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestPermission();
        } else {
            mLocationPermissionGranted = true;
            getCurrentLocation();
            updateLocationUI();
        }

    }


    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    void requestLocationUpdate() {
        try {

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    myCurrentLocation = locationResult.getLastLocation();
                    moveCameraToCurrentLocation();

                    if(myCurrentLocation != null && isAlreadySetupDestination == false) {
                        addTargetLocationToUnlockGameOnMap();

                    }

                    if(destinationToUnlock != null) {
                        if(currentLine!= null) {
                            currentLine.remove();
                            currentLine = null;
                        }
                        LatLng currentPos = new LatLng(myCurrentLocation.getLatitude(),
                                myCurrentLocation.getLongitude());
                        currentLine = mMap.addPolyline(new PolylineOptions()
                                .add(currentPos, destinationToUnlock)
                                .width(5)
                                .color(Color.RED));

                        //check if arrive at destination
                        double distanceToDestination =
                                SphericalUtil.computeDistanceBetween(currentPos, destinationToUnlock);

                        if(distanceToDestination <= DESTINATION_APPROACH_DISTANCE) {
                            getHintsAndLifeAndShowDialogResult();
                        }

                    }
                }
            };

            mFusedLocationProviderClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback, null);

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void addTargetLocationToUnlockGameOnMap() {

        if(myCurrentLocation != null) {
            isAlreadySetupDestination = true;
            final int RADIUS = 200; //100 meters
            final int MIN_DISTANCE = 70;
            LatLng currentPos = new LatLng(myCurrentLocation.getLatitude(),
                    myCurrentLocation.getLongitude());
            destinationToUnlock = getRandomLocation(currentPos, RADIUS, MIN_DISTANCE);

            Log.d("destinationToUnlock", destinationToUnlock.latitude + "," + destinationToUnlock.longitude);

            Marker destination = mMap.addMarker(new MarkerOptions().position(destinationToUnlock).title("Destination").snippet("Go here to unlock the game!"));
            destination.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationToUnlock));

        }

    }

    public LatLng getRandomLocation(LatLng point, int radius, int minDistance) {

        LatLng center = point;
        LatLng northEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), 45);
        LatLng southWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), 225);

        double distanceRandPointAndCenter;
        LatLng randomPointInRadius;

        do {
            double randLat = (Math.random() *(northEast.latitude - southWest.latitude)) + southWest.latitude;
            double randLng = (Math.random() *(northEast.longitude - southWest.longitude)) + southWest.longitude;
            randomPointInRadius = new LatLng(randLat, randLng);
            distanceRandPointAndCenter = SphericalUtil.computeDistanceBetween(center, randomPointInRadius);

        } while (distanceRandPointAndCenter >= minDistance && distanceRandPointAndCenter <= radius);

        return randomPointInRadius;

    }

    void getHintsAndLifeAndShowDialogResult() {

        //get more Hints and Life

            SharedPreferences sharedPreferencesHint = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int valueHint = sharedPreferencesHint.getInt(MainActivity.HINT,0);
            valueHint = valueHint + 1;
            SharedPreferences.Editor editorHint = sharedPreferencesHint.edit();
            editorHint.putInt(MainActivity.HINT, valueHint);
            editorHint.apply();

            SharedPreferences sharedPreferencesLife = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int valueLife = sharedPreferencesLife.getInt(MainActivity.LIFE,0);
            valueLife = valueLife + 1;
            SharedPreferences.Editor editorLife = sharedPreferencesLife.edit();
            editorLife.putInt(MainActivity.LIFE, valueLife);
            editorLife.apply();

            int score = mDatabaseHelper.getLocs() + 1;
            mDatabaseHelper.setLocs(Integer.toString(score));


        //show dialog result
        AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
        dialog.setCancelable(true);
        dialog.setTitle("Congratulation");
        dialog.setMessage("You get one new hint!");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = dialog.create();
        alert.show();

        //reset add new location
        mMap.clear();
        addTargetLocationToUnlockGameOnMap();
        LatLng currentPos = new LatLng(myCurrentLocation.getLatitude(),
                myCurrentLocation.getLongitude());
        currentLine = mMap.addPolyline(new PolylineOptions()
                .add(currentPos, destinationToUnlock)
                .width(5)
                .color(Color.RED));

    }


}

