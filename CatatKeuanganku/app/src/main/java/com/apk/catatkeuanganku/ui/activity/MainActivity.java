package com.apk.catatkeuanganku.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.AppDatabase;
import com.apk.catatkeuanganku.database.TransactionEntity;
import com.apk.catatkeuanganku.ui.adapter.TransactionAdapter;
import com.apk.catatkeuanganku.utils.LocaleHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaldo, tvGreeting, tvSeeAll;
    private Button btnPemasukan, btnPengeluaran;
    private RecyclerView rvLastTransactions;
    private TransactionAdapter adapter;
    private AppDatabase db;
    private BottomNavigationView bottomNav;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_CatatKeuanganku);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Inisialisasi View
        tvSaldo = findViewById(R.id.tv_saldo);
        tvGreeting = findViewById(R.id.greeting);
        tvSeeAll = findViewById(R.id.tv_see_all);
        btnPemasukan = findViewById(R.id.btn_pemasukan);
        btnPengeluaran = findViewById(R.id.btn_pengeluaran);
        bottomNav = findViewById(R.id.bottom_navigation);

        db = AppDatabase.getDatabase(this);

        setupRecyclerView();
        setupBottomNavigation();
        displayWelcomeMessage(); // Panggil Logic Greeting

        // Click Listeners
        btnPemasukan.setOnClickListener(v -> navigateTo(PemasukanActivity.class));
        btnPengeluaran.setOnClickListener(v -> navigateTo(PengeluaranActivity.class));
        tvSeeAll.setOnClickListener(v -> navigateTo(RiwayatActivity.class));
    }

    private void setupRecyclerView() {
        rvLastTransactions = findViewById(R.id.rv_last_transactions);
        rvLastTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        rvLastTransactions.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_history) {
                navigateTo(RiwayatActivity.class);
                return true;
            }
            if (id == R.id.nav_statistic) {
                navigateTo(StatistikActivity.class);
                return true;
            }
            if (id == R.id.nav_ai) {
                navigateTo(ChatAiActivity.class);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fix Visual Indikator Ungu
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        }
        loadSaldo();
        loadLast2Transactions();
    }

    // --- LOGIC GREETING & EMOJI (FULL VERSION) ---
    private void displayWelcomeMessage() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String countryCode = prefs.getString("country_iso", "ID");
        String greeting;
        String countryName;

        switch (countryCode.toUpperCase()) {
            case "ID": greeting = "Selamat Datang di"; countryName = "Indonesia"; break;
            case "MY": greeting = "Selamat Datang ke"; countryName = "Malaysia"; break;
            case "SG": greeting = "Welcome to"; countryName = "Singapore"; break;
            case "TH": greeting = "Yindi txnrab su"; countryName = "Thailand"; break;
            case "VN": greeting = "Chao Mung Den"; countryName = "Vietnam"; break;
            case "PH": greeting = "Maligayang pagdating sa"; countryName = "Pilipinas"; break;
            case "BN": greeting = "Selamat Datang ke"; countryName = "Brunei Darussalam"; break;
            case "KH": greeting = "saumosvakom mokkan"; countryName = "Kamboja"; break;
            case "LA": greeting = "nyinditonhab"; countryName = "Laos"; break;
            case "MM": greeting = "mhakyaosopartaal"; countryName = "Myanmar"; break;
            case "TL": greeting = "Benvindo mai"; countryName = "Timor Leste"; break;
            case "RU": greeting = "dobro pozhalovat v"; countryName = "Rusia"; break;
            case "JP": greeting = "Yokoso"; countryName = "Japan"; break;
            case "DE": greeting = "Willkommen in"; countryName = "Germany"; break;
            case "US": greeting = "Welcome to"; countryName = "United States"; break;
            case "GB": greeting = "Welcome to"; countryName = "United Kingdom"; break;
            default:
                greeting = "Welcome to";
                countryName = prefs.getString("detected_country", "your location");
                break;
        }

        String emoji = countryCodeToEmoji(countryCode);
        String welcomeFinal = greeting + " " + countryName + " " + emoji;
        tvGreeting.setText(welcomeFinal);
    }

    private String countryCodeToEmoji(String code) {
        if (code == null || code.length() != 2) return "";
        code = code.toUpperCase();
        int firstLetter = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    private String formatRupiah(long number) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return format.format(number).replace("Rp", "Rp ").replaceAll(",00", "");
    }

    private void loadSaldo() {
        executor.execute(() -> {
            Long saldo = db.transactionDao().getCurrentBalance();
            long finalSaldo = (saldo != null) ? saldo : 0L;
            runOnUiThread(() -> tvSaldo.setText(formatRupiah(finalSaldo)));
        });
    }

    private void loadLast2Transactions() {
        executor.execute(() -> {
            List<TransactionEntity> transactions = db.transactionDao().getLastNTransactions(2);
            runOnUiThread(() -> adapter.setTransactions(transactions));
        });
    }

    private void navigateTo(Class<?> cls) {
        if (this.getClass() == cls) return;
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}