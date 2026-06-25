package com.apk.catatkeuanganku.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "amount")
    public long amount;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "category")
    public String category;

    // 1. Tambahkan Constructor KOSONG (Wajib untuk Room)
    public TransactionEntity() {
    }

    // 2. Constructor ini tetap lu pakai di Activity (Tambahkan @Ignore agar Room tidak bingung)
    @Ignore
    public TransactionEntity(String name, long amount, String date, String type) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    // --- Getter ---
    public int getId() { return id; }
    public String getName() { return name; }
    public long getAmount() { return amount; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getCategory() { return category; }

    // --- Setter ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }

    // Alias: Supaya Adapter lu yang panggil getNote() tetap jalan
    public String getNote() { return name; }
    public void setNote(String note) { this.name = note; }
}