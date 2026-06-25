package com.apk.catatkeuanganku.utils; // Pastikan package sesuai

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper extends ContextWrapper {

    public LocaleHelper(Context base) {
        super(base);
    }

    /**
     * Menerapkan Locale baru secara paksa. Ini yang dipanggil dari SplashScreen
     * setelah GPS/Geocoder berhasil mendeteksi negara.
     */
    public static Context setLocale(Context context, String language) {
        if (language != null) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale);
                context = context.createConfigurationContext(config);
            } else {
                config.locale = locale;
                resources.updateConfiguration(config, resources.getDisplayMetrics());
            }
        }
        return new LocaleHelper(context);
    }

    /**
     * Digunakan oleh Activity lain untuk memuat Locale yang sudah disetel
     * sebelumnya. Untuk saat ini, fungsi ini tidak perlu diubah, biarkan
     * mengembalikan Context awal (atau bisa dikosongkan jika tidak ada
     * logika penyimpanan preferensi bahasa user).
     */
    public static Context onAttach(Context context) {
        // Untuk saat ini, kita biarkan logic LocaleHelper.onAttach
        // mengembalikan context awal. Kita akan mengandalkan setLocale
        // yang dipanggil dari SplashScreen.
        return context;
    }
}