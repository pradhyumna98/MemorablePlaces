package com.example.pradhyumna.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    static ArrayList<String> places;
    static ArrayList<LatLng> locations;
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.pradhyumna.memorableplaces" , Context.MODE_PRIVATE);

        ArrayList<String> placeLatitude = new ArrayList<>();
        ArrayList<String> placeLongitude = new ArrayList<>();

        locations.clear();
        places.clear();
        placeLongitude.clear();
        placeLatitude.clear();

        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Places" , ObjectSerializer.serialize(new ArrayList<String>())));
            placeLatitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Latitudes" , ObjectSerializer.serialize(new ArrayList<String>())));
            placeLongitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Longitudes" , ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(places.size() >0 && placeLatitude.size() >0 && placeLongitude.size() >0){
            if(places.size() == placeLatitude.size() && places.size() == placeLongitude.size()){
                for(int i=0 ; i<placeLatitude.size() ; i++){

                    locations.add(new LatLng(Double.parseDouble(placeLatitude.get(i)) , Double.parseDouble(placeLongitude.get(i))));
                }

            }
        }
        else {
            places.add("Add a new place....");
            locations.add(new LatLng(0 , 0));
        }

        listView = findViewById(R.id.listView);

        places = new ArrayList<String>();
        locations = new ArrayList<LatLng>();

        places.add("Add a new place....");
        locations.add(new LatLng(0 , 0));

        arrayAdapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1 , places);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this , MapsActivity.class);
                intent.putExtra("Place" , position);
                startActivity(intent);
            }
        });
    }
}
