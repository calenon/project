package com.example.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private EditText etSDestination;
    private EditText etFDestination;
    public List<Marker> originMarkers = new ArrayList<>();
    public List<Marker> destinationMarkers = new ArrayList<>();
    public List<Polyline> polylinePaths = new ArrayList<>();
    private   ProgressDialog progressDialog;
    private boolean done=false;
    private boolean done2=false;
    private boolean done1=false;
    RequestQueue requestQueue;
    public String insertUrl = "";
    public  String startingLong;
    public  String startingLat;

///https://www.google.gr/maps/place/lat,long;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);//
        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etSDestination = (EditText) findViewById(R.id.etSDestination);
        etFDestination = (EditText) findViewById(R.id.etFDestination);
        etDestination = (EditText) findViewById(R.id.etDestination);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        new Updateserver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                mMap.clear();
                done2=false;
                done=false;
                done1=false;
                String origin = etOrigin.getText().toString();

                lastPointKnown(origin);


            }
        });




    }

    private void sendRequest(String origin,String destination) {

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private   class Updateserver extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params ){
            while(true){
            genaratecoordinates();
            try{


                Thread.currentThread().sleep(120000);
            }
            catch(InterruptedException e){ }

            }
        }

        @Override
        protected void onPostExecute(String res) {
            //
        }



    }


   private void genaratecoordinates(){

       double Lat;
       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            startingLong=String.valueOf(location.getLongitude());
                            startingLat=String.valueOf(location.getLatitude());
                            StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    System.out.println(response.toString());
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }) {

                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String,String> parameters  = new HashMap<String, String>();
                                    parameters.put("longitude",startingLong);
                                    parameters.put("latitute",startingLat);


                                    return parameters;
                                }
                            };
                            requestQueue.add(request);



                            // mMap.addPolyline(new PolylineOptions().add(pointA,new LatLng(location.getLatitude(),location.getLongitude())));
                        }
                    }
                });

   }


    private void lastPointKnown(final String pointa){


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            location.getLatitude();
                            location.getLongitude();
                            sendRequest(geocoder(location.getLatitude(),location.getLongitude()), pointa);
                            // mMap.addPolyline(new PolylineOptions().add(pointA,new LatLng(location.getLatitude(),location.getLongitude())));
                        }
                    }
                });



    }
    private String geocoder(double latitude, double longitude){
        Geocoder geocoder;
        List<android.location.Address> addresses;
        try {
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            return address;

        }
        catch (IOException e){

        }

        return null;
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng hcmus = new LatLng(35.514067, 24.020348);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Χανιά")
                .position(hcmus)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


    }


    @Override
    public void onDirectionFinderStart() {
        if(done1==false) {
            progressDialog = ProgressDialog.show(this, "Please wait.",
                    "Finding direction..!", true);
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
         //   ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
           // ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            if(done1==false&&done==false&&done2==false)
            {
                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLACK).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));

            }
             else if(done==false&&done2==false) {
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_a))
                        .title(route.startAddress)
                        .position(route.startLocation)));
                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_b))
                        .title(route.endAddress)
                        .position(route.endLocation)));
                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));

            }
            else if(done==true&&done2==false){
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_b))
                        .title(route.startAddress)
                        .position(route.startLocation)));
                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_c))
                        .title(route.endAddress)
                        .position(route.endLocation)));
                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));

            }
            else if(done==true&&done2==true){
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_c))
                        .title(route.startAddress)
                        .position(route.startLocation)));
                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.letter_d))
                        .title(route.endAddress)
                        .position(route.endLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));

            }


        }
        DrawNext();


    }
    private void DrawNext(){
        String firstD = etFDestination.getText().toString();
        String secondD = etSDestination.getText().toString();
        String destination = etDestination.getText().toString();
        String origin = etOrigin.getText().toString();

        if(done1==false){
            progressDialog.dismiss();
            done1=true;
            sendRequest(origin,firstD);
        }
        else if(done==false){
            done=true;
            done2=false;

            sendRequest(firstD,secondD);


        }
        else if(done2==false){
            done2=true;
            sendRequest(secondD,destination);


        }


    }

}
