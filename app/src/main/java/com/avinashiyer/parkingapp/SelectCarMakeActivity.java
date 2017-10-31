package com.avinashiyer.parkingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SelectCarMakeActivity extends AppCompatActivity {

    String client_key = "l7xxb3b9e4cf8ddd499dbee846dce79b595c";
    String secret = "b286504a339644288f0a0e085c93e5ed";
    String url = "https://developer.gm.com/api/v1/oauth/access_token?grant_type=client_credentials";
    SharedPreferences prefs;
    RecyclerView mRecyclerView;
    VehicleAdapter mAdapter;
    List<Vehicle> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_car_make);
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        list = new ArrayList<Vehicle>();
        list.add(new Vehicle("Chevrolet Cruze", "Compact"));
        list.add(new Vehicle("Chevrolet Volt", "Compact"));
        list.add(new Vehicle("Chevrolet Equinox", "SUV"));
        list.add(new Vehicle("Cadillac STS AWD", "Full-size"));
        list.add(new Vehicle("Chevrolet Camaro", "Full-size"));
        list.add(new Vehicle("Chevrolet Malibu", "Full-size"));
        list.add(new Vehicle("Chevrolet Suburban", "SUV"));
        list.add(new Vehicle("Chevrolet Yukon", "SUV"));

        LinearLayoutManager llm = new LinearLayoutManager(SelectCarMakeActivity.this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(new VehicleAdapter(SelectCarMakeActivity.this,list));


        prefs = getSharedPreferences("avinash",MODE_PRIVATE);
        getAccessToken(client_key, secret, url);
        String token = prefs.getString("token",null);
        Log.d("car activity","token obtained in main: "+token);


    }
    private void getVehiclesData(final String token){
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("car activity","Response got: "+response.toString());

                try{
                    JSONObject vehicles = response.getJSONObject("vehicles");
                    int size = Integer.parseInt(vehicles.getString("size"));
                    Log.d("car activity", "Size: "+size);
                }catch(JSONException e){}

                Log.d("car activity", "token received is: "+token);
                prefs = getSharedPreferences("avinash",MODE_PRIVATE);
                prefs.edit().putString("token",token).commit();
            }

        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("car activity","Error in getting response");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                String auth = "Bearer "+token;
                params.put("Authorization", auth);
                Log.d("car activity", "headers auth are: "+auth);
                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(jsonReq);
    }

    private void getAccessToken(final String client_key, final String secret, String url){


        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("car activity","Response got: "+response.toString());
                String token=null;
                try{
                    token = response.getString("access_token");
                }catch(JSONException e){}

                Log.d("car activity", "token received is: "+token);
                prefs = getSharedPreferences("avinash",MODE_PRIVATE);
                prefs.edit().putString("token",token).commit();
            }

        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("car activity","Error in getting response");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                String credentials = client_key+":"+secret;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                params.put("Authorization", auth);
                Log.d("car activity", "headers auth are: "+auth);
                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(jsonReq);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
