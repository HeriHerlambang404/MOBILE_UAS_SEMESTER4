package com.apk.catatkeuanganku.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.utils.LocaleHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashScreen extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final long SPLASH_DELAY_MS = 2000;
    private static final long LOCATION_TIMEOUT_MS = 4000;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText, tvCountryFlag, tvAppName;
    private ImageView ivLogo;
    private ProgressBar pbLoading;
    private Handler handler;
    private final AtomicBoolean isTransitioned = new AtomicBoolean(false);

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Binding View
        locationText = findViewById(R.id.locationText);
        tvCountryFlag = findViewById(R.id.tv_country_flag);
        tvAppName = findViewById(R.id.welcomeSplashText);
        ivLogo = findViewById(R.id.app_logo);
        pbLoading = findViewById(R.id.pb_loading);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());

        // Setup Awal
        tvAppName.setText("CatatKeuanganku");
        locationText.setText(getString(R.string.detecting_location));

        // Jalankan Animasi
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeIn);

        // Timeout Guard (Jika GPS lemot, tetap masuk ke app)
        handler.postDelayed(() -> {
            if (!isTransitioned.get()) {
                startTransition();
            }
        }, LOCATION_TIMEOUT_MS);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            startTransition();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) getCountryFromLocation(location);
                    else startTransition();
                }).addOnFailureListener(e -> startTransition());
    }

    private void getCountryFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String countryName = addresses.get(0).getCountryName();
                String countryCode = addresses.get(0).getCountryCode();

                // Simpan Data Negara
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putString("detected_country", countryName)
                        .putString("country_iso", countryCode).apply();

                // Update UI secara real-time
                runOnUiThread(() -> {
                    tvCountryFlag.setText(countryCodeToEmoji(countryCode));
                    tvCountryFlag.setVisibility(View.VISIBLE);
                    locationText.setText(countryName);
                    pbLoading.setVisibility(View.GONE);
                });

                // Set Bahasa otomatis berdasarkan negara
                String localeCode = getLocaleFromISO(countryCode);
                if (localeCode != null) LocaleHelper.setLocale(this, localeCode);
            }
        } catch (IOException e) { e.printStackTrace(); }
        startTransition();
    }

    private String countryCodeToEmoji(String code) {
        if (code == null || code.length() != 2) return "🌐";
        code = code.toUpperCase();
        int firstLetter = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    private String getLocaleFromISO(String isoCode) {
        if (isoCode == null) return null;
        switch (isoCode.toUpperCase()) {
            case "ID": return "in";
            case "DE": return "de";
            case "MY": return "ms";
            case "JP": return "ja";
            case "RU": return "ru";
            default: return "en";
        }
    }

    private synchronized void startTransition() {
        if (isTransitioned.compareAndSet(false, true)) {
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, SPLASH_DELAY_MS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) getUserLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }
}