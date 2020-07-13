package com.lambton.maps_android_1;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    private static final int REQUEST_CODE = 1;
    private static final int POLYGON_SIDES = 4;
    Polyline line;
    Polygon shape;
    List<Marker> markersList = new ArrayList<>();
    List<Marker> distanceMarkers = new ArrayList<>();
    ArrayList<Polyline> polylinesList = new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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

        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startUpdateLocations();
            LatLng canadaCenterLatLong = new LatLng( 43.651070,-79.347015);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canadaCenterLatLong, 5));
        }

    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {

            if (markersList.size() == POLYGON_SIDES) {
                for(Polyline line: polylinesList){
                    line.remove();
                }
                polylinesList.clear();

                shape.remove();
                shape = null;

                for(Marker currMarker: distanceMarkers){
                    currMarker.remove();
                }
                distanceMarkers.clear();
                drawShape();
            }
        }
    });
}

    private void startUpdateLocations() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    private void setMarker(LatLng latLng){

        Geocoder geoCoder = new Geocoder(this);
        Address address = null;

        try{
            List<Address> matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = (matches.isEmpty() ? null : matches.get(0));
        }
        catch (IOException e){
            e.printStackTrace();
        }

        String title = "";
        String snippet = "";

        ArrayList<String> titleString = new ArrayList<>();
        ArrayList<String> snippetString = new ArrayList<>();

        if(address != null){
            if(address.getSubThoroughfare() != null)
            {
                titleString.add(address.getSubThoroughfare());

            }
            if(address.getThoroughfare() != null)
            {

                titleString.add(address.getThoroughfare());

            }
            if(address.getPostalCode() != null)
            {

                titleString.add(address.getPostalCode());

            }
//            if(titleString.isEmpty())
//            {
//                titleString.add("Unknown Location");
//            }
            if(address.getLocality() != null)
            {
                snippetString.add(address.getLocality());

            }
            if(address.getAdminArea() != null)
            {
                snippetString.add(address.getAdminArea());
            }

        }

        title = TextUtils.join(", ",titleString);
        title = (title.equals("") ? "  " : title);

        snippet = TextUtils.join(", ",snippetString);

        MarkerOptions options = new MarkerOptions().position(latLng)
                .draggable(true)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .snippet(snippet);

        // check if there are already the same number of markers, we clear the map
        if (markersList.size() == POLYGON_SIDES)
        {
            clearMap();
        }

        Marker mm = mMap.addMarker(options);
        markersList.add(mm);

        if (markersList.size() == POLYGON_SIDES) {
            drawShape();
        }
    }

    private void drawShape (){
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.argb(75, 0, 255, 0))
                .strokeWidth(0);

        LatLng[] markersConvex = new LatLng[POLYGON_SIDES];
        for (int i = 0; i < POLYGON_SIDES; i++) {
            markersConvex[i] = new LatLng(markersList.get(i).getPosition().latitude,
                    markersList.get(i).getPosition().longitude);
        }


        Vector<LatLng> sortedLatLong = PointPlotter.convexHull(markersConvex, POLYGON_SIDES);

        // get sortedLatLong
        Vector<LatLng> sortedLatLong2 =  new Vector<>();

        // leftmost marker
        int l = 0;
        for (int i = 0; i < markersList.size(); i++)
            if (markersList.get(i).getPosition().latitude < markersList.get(l).getPosition().latitude)
                l = i;

        Marker currentMarker = markersList.get(l);
        sortedLatLong2.add(currentMarker.getPosition());
        System.out.println(currentMarker.getPosition());
        while(sortedLatLong2.size() != POLYGON_SIDES){
            double minDistance = Double.MAX_VALUE;
            Marker nearestMarker  = null;
            for(Marker marker: markersList){
                if(sortedLatLong2.contains(marker.getPosition())){
                    continue;
                }


                double curDistance = distance(currentMarker.getPosition().latitude,
                        currentMarker.getPosition().longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude);

                if(curDistance < minDistance){
                    minDistance = curDistance;
                    nearestMarker = marker;
                }

            }
            if(nearestMarker != null){
                sortedLatLong2.add(nearestMarker.getPosition());
                currentMarker = nearestMarker;
            }
        }
        System.out.println(sortedLatLong);

        // add polygon as per convex hull lat long
        options.addAll(sortedLatLong);
        shape = mMap.addPolygon(options);
        shape.setClickable(true);

        // draw the polyline too
        LatLng[] polyLinePoints = new LatLng[sortedLatLong.size() + 1];
        int index = 0;
        for (LatLng x : sortedLatLong) {
            polyLinePoints[index] = x;

            index++;
            if (index == sortedLatLong.size()) {
                // at last add initial point
                polyLinePoints[index] = sortedLatLong.elementAt(0);
            }
        }

        for(int i =0 ; i<polyLinePoints.length -1 ; i++){

            LatLng[] tempArr = {polyLinePoints[i], polyLinePoints[i+1] };
            Polyline currentPolyline =  mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(tempArr)
                    .color(Color.RED));
            currentPolyline.setClickable(true);
            polylinesList.add(currentPolyline);
        }


    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }
}