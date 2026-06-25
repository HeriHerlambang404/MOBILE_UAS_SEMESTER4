package com.apk.catatkeuanganku.ui.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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

public class PemasukanActivity extends AppCompatActivity {

    private EditText etNama, etNominal;
    private TextView tvTanggal;
    private Spinner spKategori;
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
        setContentView(R.layout.activity_pemasukan);

        etNama = findViewById(R.id.et_nama);
        etNominal = findViewById(R.id.et_nominal);
        tvTanggal = findViewById(R.id.tv_tanggal);
        spKategori = findViewById(R.id.sp_kategori);
        btnSimpan = findViewById(R.id.btn_simpan);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        db = AppDatabase.getDatabase(this);
        calendar = Calendar.getInstance();
        updateDateLabel();

        // SETUP DROPDOWN
        String[] kategoriPemasukan = {getString(R.string.cat_salary), getString(R.string.cat_interest)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategoriPemasukan);
        spKategori.setAdapter(adapter);

        findViewById(R.id.btn_pilih_tanggal).setOnClickListener(v -> showDatePicker());
        btnSimpan.setOnClickListener(v -> saveTransaction());
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", getResources().getConfiguration().locale);
        tvTanggal.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String name = etNama.getText().toString().trim();
        String nominalStr = etNominal.getText().toString().trim();
        String category = spKategori.getSelectedItem().toString();

        if (name.isEmpty() || nominalStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.validation_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            long amount = Long.parseLong(nominalStr);
            TransactionEntity transaction = new TransactionEntity(name, amount, tvTanggal.getText().toString(), "pemasukan");
            transaction.setCategory(category);
            db.transactionDao().insert(transaction);

            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}