package com.apk.catatkeuanganku.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Naikkan version ke 4 untuk memastikan skema baru (ColumnInfo & Category) terpasang sempurna
@Database(entities = {TransactionEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        return getDatabase(context);
    }

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Membuat instance database dengan nama "finance_db"
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "finance_db")
                            // Menghapus database lama dan membuat baru jika versi naik (mencegah crash)
                            .fallbackToDestructiveMigration()
                            // Tetap diizinkan untuk mempermudah pengerjaan project awal
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}