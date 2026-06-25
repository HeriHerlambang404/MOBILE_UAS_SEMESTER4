package com.apk.catatkeuanganku.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiwayatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private AppDatabase db;
    private BottomNavigationView bottomNav;
    private ImageButton btnBack;
    private List<TransactionEntity> transactionList = new ArrayList<>(); // Simpan list global
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        db = AppDatabase.getDatabase(this);
        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.rv_transactions);
        bottomNav = findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        recyclerView.setAdapter(adapter);

        // FITUR HAPUS: Set listener Long Click dari Adapter
        adapter.setOnTransactionLongClickListener((transaction, position) -> {
            showDeleteDialog(transaction, position);
        });

        btnBack.setOnClickListener(v -> onBackPressed());
        setupBottomNavigation();
    }

    private void showDeleteDialog(TransactionEntity transaction, int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_delete))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.btn_yes), (dialog, which) -> {
                    deleteTransaction(transaction, position);
                })
                .setNegativeButton(getString(R.string.btn_no), null)
                .setIcon(android.R.drawable.ic_delete)
                .show();
    }

    private void deleteTransaction(TransactionEntity transaction, int position) {
        executor.execute(() -> {
            // 1. Hapus dari database
            db.transactionDao().deleteTransaction(transaction.getId());

            runOnUiThread(() -> {
                // 2. Update List lokal dan beri tahu adapter
                transactionList.remove(position);
                adapter.setTransactions(transactionList);
                // Gunakan notifyItemRemoved lebih smooth, tapi setTransactions juga aman karena lu pakai list global

                Toast.makeText(this, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadAllTransactions() {
        executor.execute(() -> {
            transactionList = db.transactionDao().getAllTransactions();
            runOnUiThread(() -> adapter.setTransactions(transactionList));
        });
    }

    // --- SISANYA TETAP SAMA ---

    private void setupBottomNavigation() {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateTo(MainActivity.class);
                return true;
            } else if (id == R.id.nav_history) return true;
            else if (id == R.id.nav_statistic) {
                navigateTo(StatistikActivity.class);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_history).setChecked(true);
        }
        loadAllTransactions();
    }

    private void navigateTo(Class<?> cls) {
        if (this.getClass() == cls) return;
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}