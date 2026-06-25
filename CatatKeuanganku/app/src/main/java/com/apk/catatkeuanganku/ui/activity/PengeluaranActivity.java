package com.apk.catatkeuanganku.ui.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.AppDatabase;
import com.apk.catatkeuanganku.database.TransactionEntity;
import com.apk.catatkeuanganku.utils.LocaleHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PengeluaranActivity extends AppCompatActivity {

    private EditText etNama, etNominal;
    private TextView tvTanggal;
    private Spinner spKategori;
    private ImageButton btnPilihTanggal, btnBack;
    private Button btnSimpan;
    private AppDatabase db;
    private Calendar calendar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengeluaran);

        etNama = findViewById(R.id.et_nama);
        etNominal = findViewById(R.id.et_nominal);
        tvTanggal = findViewById(R.id.tv_tanggal);
        spKategori = findViewById(R.id.sp_kategori);
        btnPilihTanggal = findViewById(R.id.btn_pilih_tanggal);
        btnSimpan = findViewById(R.id.btn_simpan);
        btnBack = findViewById(R.id.btn_back);

        db = AppDatabase.getDatabase(this);
        calendar = Calendar.getInstance();
        updateDateLabel();

        String[] kategoriPengeluaran = {
                getString(R.string.cat_food),
                getString(R.string.cat_transport),
                getString(R.string.cat_shopping),
                getString(R.string.cat_entertainment)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategoriPengeluaran);
        spKategori.setAdapter(adapter);

        btnPilihTanggal.setOnClickListener(v -> showDatePicker());
        btnSimpan.setOnClickListener(v -> saveTransaction());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        Locale currentLocale = getResources().getConfiguration().locale;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", currentLocale);
        tvTanggal.setText(sdf.format(calendar.getTime()));
    }

    private void saveTransaction() {
        String name = etNama.getText().toString().trim();
        String nominalStr = etNominal.getText().toString().trim();
        String date = tvTanggal.getText().toString();
        String category = spKategori.getSelectedItem().toString();

        if (name.isEmpty() || nominalStr.isEmpty() || nominalStr.equals("0")) {
            Toast.makeText(this, getString(R.string.validation_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long amount = Long.parseLong(nominalStr);
            // Panggil proses validasi saldo di thread terpisah
            validateAndInsert(name, amount, date, category);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.validation_invalid_nominal), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndInsert(String name, long amount, String date, String category) {
        executor.execute(() -> {
            // 1. Ambil saldo terakhir dari DB
            long currentBalance = db.transactionDao().getCurrentBalance();

            // 2. Cek apakah saldo cukup
            if (amount > currentBalance) {
                runOnUiThread(() -> {
                    // Tampilkan Dialog Saldo Kurang
                    showInsufficientBalanceDialog();
                });
            } else {
                // 3. Jika cukup, proses insert
                long finalAmount = -amount; // Disimpan negatif
                TransactionEntity transaction = new TransactionEntity(name, finalAmount, date, "pengeluaran");
                transaction.setCategory(category);

                db.transactionDao().insert(transaction);

                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                });
            }
        });
    }

    private void showInsufficientBalanceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Saldo Tidak Cukup") // Gunakan string resource jika ingin multibahasa
                .setMessage("Maaf, saldo kamu saat ini tidak mencukupi untuk melakukan transaksi ini.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
    }
}