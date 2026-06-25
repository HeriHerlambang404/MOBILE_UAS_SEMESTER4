package com.apk.catatkeuanganku.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    // Mengambil semua transaksi, diurutkan dari terbaru (ID besar)
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAllTransactions();

    /**
     * PERBAIKAN KRITIS:
     * Menggunakan COALESCE agar jika data kosong, kembaliannya adalah 0, bukan NULL.
     * Karena pengeluaran lu disimpan sebagai angka NEGATIF, maka SUM langsung sudah benar.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions")
    long getCurrentBalance();

    // Mengambil N transaksi terakhir (digunakan untuk daftar di MainActivity)
    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getLastNTransactions(int limit);

    // Query untuk filter Pemasukan terbaru
    @Query("SELECT * FROM transactions WHERE type = 'pemasukan' ORDER BY id DESC LIMIT 2")
    List<TransactionEntity> getLatestIncome();

    // Query untuk filter Pengeluaran terbaru
    @Query("SELECT * FROM transactions WHERE type = 'pengeluaran' ORDER BY id DESC LIMIT 2")
    List<TransactionEntity> getLatestExpense();

    // Query untuk menghapus transaksi berdasarkan ID
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    void deleteTransaction(int transactionId);

    /**
     * TAMBAHAN: Berguna untuk mengecek apakah saldo cukup sebelum user
     * melakukan transaksi pengeluaran tertentu.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM transactions)")
    boolean hasAnyTransaction();


}