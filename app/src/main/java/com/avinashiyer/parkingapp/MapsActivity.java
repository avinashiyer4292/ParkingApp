package com.avinashiyer.parkingapp;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private String time;
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    DataParser dataparser;
    private  static  final String TAG = "MapsActivity";
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    LocationManager locationManager;
    Location loc;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toast.makeText(MapsActivity.this, "Please enter a destination!", Toast.LENGTH_LONG).show();
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Initializing
        MarkerPoints = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already two locations
                if (MarkerPoints.size() > 0) {
                    MarkerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                MarkerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (MarkerPoints.size() == 1) {
                    options.icon(getMarkerIcon("#F6BF26")); //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (MarkerPoints.size() >= 1) {
                    //LatLng origin = MarkerPoints.get(0);
                    LatLng dest = MarkerPoints.get(0);
                    LatLng origin = new LatLng(33.777176, -84.396142);
                    // Getting URL to the Google Directions API
                    String urlString = getUrl(origin, dest);
                    Log.d("onMapClick", urlString.toString());
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(urlString);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }

            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG,"ParkMarker clicked!");
                if(marker.getTag().equals("green"))
                    showTimeSetDialog(marker);
                else
                    Toast.makeText(MapsActivity.this,"Sorry, this parking spot is already occupied for "+marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }



    public void showTimeSetDialog(final Marker marker) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner1);

        dialogBuilder.setTitle("Parking time");
        dialogBuilder.setMessage("Select time required for parking");
        //dialogBuilder.setMessage("Enter text below");
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("10 mins");
        spinnerArray.add("20 mins");
        spinnerArray.add("30 mins");
        spinnerArray.add("1 hour");

        ;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Selected item is: "+selectedItem);
                time = selectedItem;
//                if(position!=0)
//                    showParkingConfirmationDialog(marker);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.dismiss();
                showParkingConfirmationDialog(time,marker);
                //marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.park_red));
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showParkingConfirmationDialog(final String time,final Marker marker) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_confirm, null);
        dialogBuilder.setView(dialogView);


        dialogBuilder.setTitle("Parking confirmation");
        dialogBuilder.setMessage("Would you like to confirm this parking spot?");
        //dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                dialog.dismiss();
                marker.setTag("red");
                marker.setTitle(time);
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.park_red));
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor("#F6BF26"));

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
                //call the marker move method here
                Toast.makeText(MapsActivity.this, "Moving to destination...",Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        LatLng dest = MarkerPoints.get(0);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(MarkerPoints.get(0)));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                        MarkerPoints.clear();
                        //mMap.clear();
                        getAllParkingSpots(dest);
                    }
                }, 5000);

            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    public static BitmapDescriptor getCustomMarker(Context context, Icon icon, double scale, int color){
        IconDrawable id=new IconDrawable(context,icon).actionBarSize();
        id.color(color);
        Drawable d=id.getCurrent();
        Bitmap bm=Bitmap.createBitmap(id.getIntrinsicWidth(),id.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        if (scale != 1)   bm=Bitmap.createScaledBitmap(bm,id.getIntrinsicWidth(),id.getIntrinsicHeight(),false);
        Canvas c=new Canvas(bm);
        d.draw(c);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }
    private void getAllParkingSpots(LatLng dest){
        LatLng l = getRandomLocation(dest,1000);//new LatLng(dest.latitude+0.016, dest.longitude);
        Marker marker = mMap.addMarker(new MarkerOptions().position(l).title("1").
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.park_green)));
        marker.setTag("green");

        l = getRandomLocation(dest,1000);//new LatLng(dest.latitude-0.0102, dest.longitude-0.0015);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("2").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.park_green)));
        marker.setTag("green");

        l = getRandomLocation(dest,1000);//new LatLng(dest.latitude-0.022, dest.longitude-0.011);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("3").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.park_green)));
        marker.setTag("green");

        l = getRandomLocation(dest,1000);//new LatLng(dest.latitude+0.013, dest.longitude+0.01345);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("4").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.park_green)));
        marker.setTag("green");

        l = getRandomLocation(dest,1000);//new LatLng(dest.latitude, dest.longitude+0.034);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("5").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.park_green)));
        marker.setTag("green");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    public LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        //This is to generate 10 random points
        for(int i = 0; i<10; i++) {
            double x0 = point.latitude;
            double y0 = point.longitude;

            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 11100f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
            randomPoints.add(randomLatLng);
            Location l1 = new Location("");
            l1.setLatitude(randomLatLng.latitude);
            l1.setLongitude(randomLatLng.longitude);
            randomDistances.add(l1.distanceTo(myLocation));
        }
        //Get nearest point to the centre
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


//    private void getLocation() {
//        try {
//            if (canGetLocation) {
//                Log.d(TAG, "Can get location");
//                if (isGPS) {
//                    // from GPS
//                    Log.d(TAG, "GPS on");
//                    locationManager.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//
//                    if (locationManager != null) {
//                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        if (loc != null)
//                            updateUI(loc);
//                    }
//                } else if (isNetwork) {
//                    // from Network Provider
//                    Log.d(TAG, "NETWORK_PROVIDER on");
//                    locationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//
//                    if (locationManager != null) {
//                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if (loc != null)
//                            updateUI(loc);
//                    }
//                } else {
//                    loc.setLatitude(0);
//                    loc.setLongitude(0);
//                    updateUI(loc);
//                }
//            } else {
//                Log.d(TAG, "Can't get location");
//            }
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

//    private void addFriendMarkers() {
////        LatLng l = new LatLng(35.905154, -79.051382);
////        MarkerOptions options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face1)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.912019, -79.045052);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face2)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.902432, -79.046435);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face3)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.908336, -79.039329);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face4)));
////        mMap.addMarker(options);
//
//
//    }
//
//    private void drawCircle(LatLng location) {
////        CircleOptions options = new CircleOptions();
////        options.center(location);
////        //Radius in meters
////        options.radius(1300);
////        options.fillColor(getResources()
////                .getColor(R.color.fillColor));
////        options.strokeColor(getResources()
////                .getColor(R.color.strokeColor));
////        options.strokeWidth(10);
////        mMap.addCircle(options);
//    }
//
//
//    public LatLng getCurrentLocation(Context context) {
//        try {
//            LocationManager locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            Criteria criteria = new Criteria();
//            String locProvider = locMgr.getBestProvider(criteria, false);
//
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return new LatLng(0, 0);
//            }
//            Location location = locMgr.getLastKnownLocation(locProvider);
//
//            // getting GPS status
//            boolean isGPSEnabled = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            // getting network status
//            boolean isNWEnabled = locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (!isGPSEnabled && !isNWEnabled) {
//                // no network provider is enabled
//                return null;
//            } else {
//                // First get location from Network Provider
//                if (isNWEnabled)
//                    if (locMgr != null)
//                        location = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//                // if GPS Enabled get lat/long using GPS Services
//                if (isGPSEnabled)
//                    if (location == null)
//                        if (locMgr != null)
//                            location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            }
//
//            return new LatLng(location.getLatitude(), location.getLongitude());
//        } catch (NullPointerException ne) {
//            Log.e("Current Location", "Current Lat Lng is Null");
//            return new LatLng(0, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new LatLng(0, 0);
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//    }
//
//    // Fetches data from url passed
//    private class FetchUrl extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... url) {
//
//            // For storing data from web service
//            String data = "";
//
//            try {
//                // Fetching the data from web service
//                data = downloadUrl(url[0]);
//                Log.d("Background Task data", data.toString());
//            } catch (Exception e) {
//                Log.d("Background Task", e.toString());
//            }
//            return data;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            ParserTask parserTask = new ParserTask();
//
//            // Invokes the thread for parsing the JSON data
//            parserTask.execute(result);
//
//        }
//    }


//    public class DataParser {
//
//        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
//        public List<List<HashMap<String,String>>> parse(JSONObject jObject){
//
//            List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
//            JSONArray jRoutes;
//            JSONArray jLegs;
//            JSONArray jSteps;
//
//            try {
//
//                jRoutes = jObject.getJSONArray("routes");
//
//                /** Traversing all routes */
//                for(int i=0;i<jRoutes.length();i++){
//                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
//                    List path = new ArrayList<>();
//
//                    /** Traversing all legs */
//                    for(int j=0;j<jLegs.length();j++){
//                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
//
//                        /** Traversing all steps */
//                        for(int k=0;k<jSteps.length();k++){
//                            String polyline = "";
//                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
//                            List<LatLng> list = decodePoly(polyline);
//
//                            /** Traversing all points */
//                            for(int l=0;l<list.size();l++){
//                                HashMap<String, String> hm = new HashMap<>();
//                                hm.put("lat", Double.toString((list.get(l)).latitude) );
//                                hm.put("lng", Double.toString((list.get(l)).longitude) );
//                                path.add(hm);
//                            }
//                        }
//                        routes.add(path);
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }catch (Exception e){
//            }
//
//
//            return routes;
//        }
//    }
//
//}
