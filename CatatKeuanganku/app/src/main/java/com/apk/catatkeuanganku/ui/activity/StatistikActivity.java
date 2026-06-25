package com.apk.catatkeuanganku.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.AppDatabase;
import com.apk.catatkeuanganku.database.TransactionEntity;
import com.apk.catatkeuanganku.utils.LocaleHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatistikActivity extends AppCompatActivity {

    private PieChart pieChart;
    private RecyclerView rvStatsDetail;
    private AppDatabase db;
    private BottomNavigationView bottomNav;
    private StatsAdapter statsAdapter;
    private ImageButton btnBack;
    private TextView tvHeaderTitle, tvLabelKategori;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static class CategoryStat {
        String category;
        long amount;
        int color;
        CategoryStat(String category, long amount, int color) {
            this.category = category;
            this.amount = amount;
            this.color = color;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistik);

        // Inisialisasi View
        pieChart = findViewById(R.id.pieChart);
        rvStatsDetail = findViewById(R.id.rv_stats_detail);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnBack = findViewById(R.id.btn_back);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvLabelKategori = findViewById(R.id.tv_label_kategori);

        db = AppDatabase.getDatabase(this);

        rvStatsDetail.setLayoutManager(new LinearLayoutManager(this));
        statsAdapter = new StatsAdapter();
        rvStatsDetail.setAdapter(statsAdapter);

        btnBack.setOnClickListener(v -> finish());

        setupPieChart();
        setupBottomNavigation();
        applyLanguageStrings(); // Logic Bahasa
    }

    private void applyLanguageStrings() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String countryCode = prefs.getString("country_iso", "ID").toLowerCase();

        Locale locale = new Locale(countryCode);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        Context localizedContext = createConfigurationContext(config);
        Resources res = localizedContext.getResources();

        tvHeaderTitle.setText(res.getString(R.string.statistik_expense));
        tvLabelKategori.setText(res.getString(R.string.kategori));
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("Expense");
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.getMenu().findItem(R.id.nav_statistic).setChecked(true);
        loadData();
    }

    private void loadData() {
        executor.execute(() -> {
            List<TransactionEntity> list = db.transactionDao().getAllTransactions();
            Map<String, Long> categoryMap = new HashMap<>();

            for (TransactionEntity t : list) {
                if ("pengeluaran".equals(t.getType())) {
                    String cat = (t.getCategory() != null) ? t.getCategory() : "Other";
                    categoryMap.put(cat, categoryMap.getOrDefault(cat, 0L) + Math.abs(t.getAmount()));
                }
            }

            ArrayList<PieEntry> entries = new ArrayList<>();
            List<CategoryStat> statsList = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
            for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);

            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
                int color = colors.get(colorIndex % colors.size());
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                statsList.add(new CategoryStat(entry.getKey(), entry.getValue(), color));
                colorIndex++;
            }

            runOnUiThread(() -> {
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);
                pieChart.setData(new PieData(dataSet));
                pieChart.invalidate();
                statsAdapter.setList(statsList);
            });
        });
    }

    private class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {
        private List<CategoryStat> list = new ArrayList<>();

        void setList(List<CategoryStat> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistik, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryStat item = list.get(position);

            // Logic Translate Kategori dari Strings.xml
            String resName = "cat_" + item.category.toLowerCase().replace(" ", "_");
            int resId = getResources().getIdentifier(resName, "string", getPackageName());
            holder.tvCategory.setText(resId != 0 ? getString(resId) : item.category);

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            holder.tvAmount.setText(format.format(item.amount).replace("Rp", "Rp "));

            // Logic Icon
            String iconName = item.category.toLowerCase().replace(" ", "_");
            int iconId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            holder.ivIcon.setImageResource(iconId != 0 ? iconId : R.drawable.makan);
            holder.ivIcon.getBackground().setTint(item.color);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory, tvAmount;
            ImageView ivIcon;
            ViewHolder(View v) {
                super(v);
                tvCategory = v.findViewById(R.id.tv_category_name);
                tvAmount = v.findViewById(R.id.tv_category_amount);
                ivIcon = v.findViewById(R.id.iv_category_icon);
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, MainActivity.class)); return true; }
            if (id == R.id.nav_history) { startActivity(new Intent(this, RiwayatActivity.class)); return true; }
            return id == R.id.nav_statistic;
        });
    }
}