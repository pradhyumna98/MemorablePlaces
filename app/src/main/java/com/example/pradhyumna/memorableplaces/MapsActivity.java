package com.example.pradhyumna.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;


    public void centerMapLocation(Location location , String place){

        if(location != null){
        LatLng userLatLng = new LatLng(location.getLatitude() , location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLatLng).title(place));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng , 10));
        }
    }


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0 , locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapLocation(lastLocation , "Your Last Known Location");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        if(intent.getIntExtra("Place" , 0) == 0){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapLocation(location , "Current Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0 , locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapLocation(lastLocation , "Your Last Known Location");
            }
            else {
                ActivityCompat.requestPermissions(this , new String [] {Manifest.permission.ACCESS_FINE_LOCATION} , 1);
            }

        }
        else {
            Location placeLocal = new Location(LocationManager.GPS_PROVIDER);
            placeLocal.setLatitude(MainActivity.locations.get(intent.getIntExtra("Place" , 0)).latitude);
            placeLocal.setLongitude(MainActivity.locations.get(intent.getIntExtra("Place" , 0)).longitude);

            centerMapLocation(placeLocal , MainActivity.places.get(intent.getIntExtra("Place" , 0)));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext() , Locale.getDefault());
        String memorableAddress = "";

        try{

            List<Address> addresses = geocoder.getFromLocation(latLng.latitude , latLng.longitude , 1);
            if(addresses != null && addresses.size()>0){
                if(addresses.get(0).getThoroughfare() != null){
                    if(addresses.get(0).getSubThoroughfare() != null){
                        memorableAddress += addresses.get(0).getSubThoroughfare() + "\n";
                    }
                    memorableAddress+= addresses.get(0).getThoroughfare();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        if(memorableAddress.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH : mm yyyy-MM-dd");
            memorableAddress+=sdf.format(new Date());
        }


        mMap.addMarker(new MarkerOptions().position(latLng).title(memorableAddress));

        MainActivity.places.add(memorableAddress);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.pradhyumna.memorableplaces" , Context.MODE_PRIVATE);

        try {
            ArrayList<String> placeLatitude = new ArrayList<>();
            ArrayList<String> placeLongitude = new ArrayList<>();

            for(LatLng cord : MainActivity.locations){
                placeLatitude.add(Double.toString(cord.latitude));
                placeLongitude.add(Double.toString(cord.longitude));
            }

            sharedPreferences.edit().putString("Places" , ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("Latitudes" , ObjectSerializer.serialize(placeLatitude)).apply();
            sharedPreferences.edit().putString("Longitudes" , ObjectSerializer.serialize(placeLongitude)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(MapsActivity.this , "Location Saved" , Toast.LENGTH_SHORT).show();
    }
}
