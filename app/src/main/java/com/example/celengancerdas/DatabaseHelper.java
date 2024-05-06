package com.example.celengancerdas;

// Import pernyataan dan kode kelas Anda di sini
import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "celengancerdas.db";
    private static final int DATABASE_VERSION = 1;

    // Nama tabel dan kolom Untuk users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Nama tabel dan kolom untuk Celengan
    public static final String TABLE_CELENGAN = "celengan";
    public static final String COLUMN_ID_CELENGAN = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_GAMBAR_BARANG = "gambar_barang";
    public static final String COLUMN_NAMA_CELENGAN = "nama_celengan";
    public static final String COLUMN_HARGA_BARANG = "harga_barang";
    public static final String COLUMN_FILLING_PLAN = "filling_plan_daily_nominal";
    public static final String COLUMN_NOTIFIKASI_JAM = "notifikasi_jam";

    // Nama tabel dan kolom untuk Savings Logs
    public static final String TABLE_SAVINGS_LOGS = "savings_logs";
    public static final String COLUMN_ID_SAVINGS = "id";
    public static final String COLUMN_CELENGAN_ID = "celengan_id";
    public static final String COLUMN_TANGGAL_TRANSAKSI = "tanggal_transaksi";
    public static final String COLUMN_WAKTU_TRANSAKSI = "waktu_transaksi";
    public static final String COLUMN_JUMLAH_TRANSAKSI = "jumlah_transaksi";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Query untuk membuat tabel Celengan
        String CREATE_CELENGAN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CELENGAN + "("
                + COLUMN_ID_CELENGAN + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GAMBAR_BARANG + " BLOB,"
                + COLUMN_NAMA_CELENGAN + " TEXT,"
                + COLUMN_HARGA_BARANG + " INTEGER,"
                + COLUMN_FILLING_PLAN + " INTEGER,"
                + COLUMN_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_CELENGAN_TABLE);

        // Query untuk membuat tabel savings_logs
        String CREATE_SAVINGS_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SAVINGS_LOGS + "("
                + COLUMN_ID_SAVINGS + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CELENGAN_ID + " INTEGER,"
                + COLUMN_TANGGAL_TRANSAKSI + " TEXT,"
                + COLUMN_WAKTU_TRANSAKSI + " TEXT,"
                + COLUMN_JUMLAH_TRANSAKSI + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CELENGAN_ID + ") REFERENCES " + TABLE_CELENGAN + "(" + COLUMN_ID_CELENGAN + ")"
                + ")";
        db.execSQL(CREATE_SAVINGS_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CELENGAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_LOGS);
        onCreate(db);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean emailExists = false;
        if (cursor != null) {
            emailExists = cursor.getCount() > 0;
            cursor.close();
        }
        db.close();
        return emailExists;
    }

    // Fungsi untuk menambahkan akun ke database
    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public String getNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        String name = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            }
            cursor.close();
        }
        db.close();
        return name;
    }

    public long checkUserLoginAndGetId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
        }

        db.close();
        return userId;
    }

    // Fungsi untuk menambahkan data Celengan ke database
    public boolean tambahCelengan(byte[] gambar, String nama, int harga, int fillingPlan, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GAMBAR_BARANG, gambar);
        values.put(COLUMN_NAMA_CELENGAN, nama);
        values.put(COLUMN_HARGA_BARANG, harga);
        values.put(COLUMN_FILLING_PLAN, fillingPlan);
        values.put(COLUMN_USER_ID, userId); // Menambahkan ID pengguna
        long result = db.insert(TABLE_CELENGAN, null, values);
        return result != -1;
    }


    // Fungsi untuk menambahkan data transaksi ke savings_logs
    public boolean transaksi(int celenganId, String tanggal, String waktu, int jumlah) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CELENGAN_ID, celenganId);
        values.put(COLUMN_TANGGAL_TRANSAKSI, tanggal);
        values.put(COLUMN_WAKTU_TRANSAKSI, waktu);
        values.put(COLUMN_JUMLAH_TRANSAKSI, jumlah);
        long result = db.insert(TABLE_SAVINGS_LOGS, null, values);
        return result != -1;
    }

    public int getMaksimalIdCelengan() {
        SQLiteDatabase db = this.getReadableDatabase();
        int maksimalId = -1;
        String query = "SELECT MAX(" + COLUMN_ID_CELENGAN + ") AS MaxId FROM " + TABLE_CELENGAN;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            maksimalId = cursor.getInt(cursor.getColumnIndex("MaxId"));
            cursor.close();
        }
        db.close();
        return maksimalId;
    }

    // Mengambil semua data Celengan dan Jumlah Terkumpul dari tabel transaksi
    public List<DashboardActivity.Celengan> getAllCelengan(int userId) {
        List<DashboardActivity.Celengan> celenganList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        // Query untuk mengambil data dari tabel transaksi sesuai userId
        String selectQuery = "SELECT c." + COLUMN_ID_CELENGAN + ", c." + COLUMN_GAMBAR_BARANG + ", c." + COLUMN_NAMA_CELENGAN
                + ", c." + COLUMN_HARGA_BARANG + ", " + COLUMN_JUMLAH_TRANSAKSI + " AS totalAmount"
                + ", c." + COLUMN_FILLING_PLAN // Tambahkan kolom FillingPlan
                + " FROM " + TABLE_CELENGAN + " c"
                + " INNER JOIN " + TABLE_SAVINGS_LOGS + " s ON c." + COLUMN_ID_CELENGAN + " = s." + COLUMN_CELENGAN_ID
                + " WHERE c." + COLUMN_USER_ID + " = ?"
                + " GROUP BY c." + COLUMN_ID_CELENGAN;

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        // Loop melalui semua baris dan menambahkan data ke list
        if (cursor.moveToFirst()) {
            do {
                int celenganId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID_CELENGAN));
                byte[] imageBlob = cursor.getBlob(cursor.getColumnIndex(COLUMN_GAMBAR_BARANG));
                String namaCelengan = cursor.getString(cursor.getColumnIndex(COLUMN_NAMA_CELENGAN));
                int hargaCelengan = cursor.getInt(cursor.getColumnIndex(COLUMN_HARGA_BARANG));
                int jumlahTerkumpul = cursor.getInt(cursor.getColumnIndex("totalAmount")); // Ambil jumlah terkumpul
                int hargaPerHari = cursor.getInt(cursor.getColumnIndex(COLUMN_FILLING_PLAN)); // Ambil harga per hari

                DashboardActivity.Celengan celengan = new DashboardActivity.Celengan(celenganId, imageBlob, namaCelengan, hargaCelengan, jumlahTerkumpul, hargaPerHari);
                celengan.setHargaPerHari(hargaPerHari); // Atur harga per hari

                celenganList.add(celengan);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // Tidak perlu menutup database di sini karena akan digunakan lagi

        return celenganList;
    }


    // Method untuk mendapatkan data saving_logs berdasarkan ID Celengan
    public List<SavingsLog> getSavingsLogsByCelenganId(int celenganId) {
        List<SavingsLog> savingsLogs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Query untuk mendapatkan data saving_logs berdasarkan ID Celengan
        String selectQuery = "SELECT " + COLUMN_TANGGAL_TRANSAKSI + ", " +
                COLUMN_WAKTU_TRANSAKSI + ", " +
                COLUMN_JUMLAH_TRANSAKSI +
                " FROM " + TABLE_SAVINGS_LOGS +
                " WHERE " + COLUMN_CELENGAN_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(celenganId)});

        // Loop melalui hasil query dan tambahkan data ke dalam list SavingsLog
        if (cursor.moveToFirst()) {
            do {
                String tanggalTransaksi = cursor.getString(cursor.getColumnIndex(COLUMN_TANGGAL_TRANSAKSI));
                String waktuTransaksi = cursor.getString(cursor.getColumnIndex(COLUMN_WAKTU_TRANSAKSI));
                int jumlahTransaksi = cursor.getInt(cursor.getColumnIndex(COLUMN_JUMLAH_TRANSAKSI));

                SavingsLog log = new SavingsLog(tanggalTransaksi, waktuTransaksi, jumlahTransaksi);
                savingsLogs.add(log);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // Tidak perlu menutup database di sini karena akan digunakan lagi

        return savingsLogs;
    }

    // Metode untuk mendapatkan jumlah_terkumpul terakhir dari tabel savings_logs berdasarkan celenganId
    public int getLastJumlahTerkumpulByCelenganId(int celenganId) {
        int lastJumlahTerkumpul = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Query untuk mengambil jumlah_terkumpul terakhir dari tabel savings_logs berdasarkan celenganId
        String query = "SELECT jumlah_transaksi FROM savings_logs WHERE celengan_id = ? ORDER BY id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(celenganId)});

        // Check jika cursor tidak kosong dan pindahkan cursor ke posisi pertama
        if (cursor != null && cursor.moveToFirst()) {
            lastJumlahTerkumpul = cursor.getInt(cursor.getColumnIndex(COLUMN_JUMLAH_TRANSAKSI));
            cursor.close();
        }

        // Mengembalikan nilai jumlah_terkumpul terakhir
        return lastJumlahTerkumpul;
    }
    // Mendapatkan data terakhir dari tabel savings_logs berdasarkan fk celengan_id
    public int getLastTransactionAmountByCelenganId(int celenganId) {
        int lastTransactionAmount = 0;

        SQLiteDatabase db = this.getWritableDatabase();

        // Query untuk mendapatkan jumlah transaksi terakhir dari savings_logs berdasarkan fk celengan_id
        String selectQuery = "SELECT " + COLUMN_JUMLAH_TRANSAKSI +
                " FROM " + TABLE_SAVINGS_LOGS +
                " WHERE " + COLUMN_CELENGAN_ID + " = ?" +
                " ORDER BY " + COLUMN_ID_SAVINGS+ " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(celenganId)});

        // Ambil jumlah transaksi terakhir jika ada hasil dari query
        if (cursor.moveToFirst()) {
            lastTransactionAmount = cursor.getInt(cursor.getColumnIndex(COLUMN_JUMLAH_TRANSAKSI));
        }

        cursor.close();
        // Tidak perlu menutup database di sini karena akan digunakan lagi

        return lastTransactionAmount;
    }

    // Method untuk menghapus data dari tabel celengan berdasarkan celenganId
    public void hapusDataCelengan(int celenganId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM celengan WHERE id = ?";
        db.execSQL(query, new String[]{String.valueOf(celenganId)});
        db.close();
    }

    // Method untuk menghapus data dari tabel savings_log berdasarkan celenganId
    public void hapusDataSavingsLogByCelenganId(int celenganId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM savings_logs WHERE celengan_id = ?";
        db.execSQL(query, new String[]{String.valueOf(celenganId)});
        db.close();
    }

    // Mendapatkan nama-nama celengan berdasarkan user ID
    // Mendapatkan nama-nama celengan berdasarkan user ID
    public List<String> getNameCelenganByUserId(int userId) {
        List<String> celenganNames = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + COLUMN_NAMA_CELENGAN + " FROM " + TABLE_CELENGAN +
                " WHERE " + COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String namaCelengan = cursor.getString(cursor.getColumnIndex(COLUMN_NAMA_CELENGAN));
                celenganNames.add(namaCelengan);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return celenganNames;
    }



}


