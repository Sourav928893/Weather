package com.example.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Weather URL to get JSON
    private String weatherUrl = "";

    // API key for the URL (your provided key)
    private String apiKey = "4252dd5942eb3bc7597e34a809c396ce";

    private Button btVar1;
    private TextView textView;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the TextView to display the temperature
        textView = findViewById(R.id.textView);

        btVar1 = findViewById(R.id.btVar1);

        // Create an instance of the Fused Location Provider Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set onClickListener to fetch the location when the button is clicked
        btVar1.setOnClickListener(v -> checkForPermission());
    }

    private void checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted, obtain the location
            obtainLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, obtain the location
                obtainLocation();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void obtainLocation() {
        // Get the last location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // Get the latitude and longitude and create the HTTP URL
                    if (location != null) {
                        weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() +
                                "&lon=" + location.getLongitude() + "&units=metric&appid=" + apiKey;
                    }
                    // Fetch data from URL
                    getTemp();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show();
                });
    }

    private void getTemp() {
        // Instantiate the RequestQueue
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(this);
        String url = weatherUrl;

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Get the JSON object
                        JSONObject obj = new JSONObject(response);

                        // Get the temperature readings from the response
                        JSONObject main = obj.getJSONObject("main");
                        String temperature = main.getString("temp");

                        // Get the city name
                        String city = obj.getString("name");

                        // Set the temperature and city name
                        textView.setText(temperature + " deg Celsius in " + city);

                        System.out.println(obj.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        textView.setText("Error parsing response!");
                    }
                },
                error -> textView.setText("That didn't work!"));

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }
}
