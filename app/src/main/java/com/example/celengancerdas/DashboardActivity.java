package com.example.celengancerdas;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.Serializable;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private ListView listViewCelengan;
    private CelenganAdapter celenganAdapter;
    private List<Celengan> celenganList;
    private static final int ADD_CELENGAN_REQUEST = 1;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);

        SessionManager sessionManager = new SessionManager(this);
        String name = sessionManager.getName();

        // Menggabungkan "Hallo, " dengan nilai "name"
        String greetingText = "Hallo, " + name + "!";

        // Menampilkan teks di TextView
        TextView textView = findViewById(R.id.textViewNama);
        textView.setText(greetingText);
        CardView cardView = findViewById(R.id.accountmanagement);

        // Inisialisasi ListView
        listViewCelengan = findViewById(R.id.listViewCelengan);

        // Mendapatkan data celengan dari DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Mendapatkan data celengan yang terkait dengan pengguna yang sedang masuk
        int userId = (int) sessionManager.getLoggedInUserId();
        // Inisialisasi adapter dengan data celengan yang terkait dengan pengguna yang masuk
        celenganList = databaseHelper.getAllCelengan(userId);
        celenganAdapter = new CelenganAdapter(this, celenganList, databaseHelper);


        // Set adapter ke ListView
        listViewCelengan.setAdapter(celenganAdapter);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tindakan yang akan diambil saat CardView diklik
                Intent intent = new Intent(DashboardActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView1 = findViewById(R.id.cardView1);
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tindakan yang akan diambil saat CardView diklik

                Intent intent = new Intent(DashboardActivity.this, TambahCelengan.class);

                startActivity(intent);
            }
        });

        CardView cardView2 = findViewById(R.id.cardView2);
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tindakan yang akan diambil saat CardView diklik
                int userId = (int) sessionManager.getLoggedInUserId();
                Intent intent = new Intent(DashboardActivity.this, PengaturanNotifikasi.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        listViewCelengan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Dapatkan objek Celengan yang dipilih dari posisi item yang diklik
                DashboardActivity.Celengan celengan = celenganList.get(position);
                // Mengambil hargaPerHari dari objek Celengan yang dipilih
                int hargaPerHari = celengan.getHargaPerHari();
                // Dapatkan id Celengan dari tag view yang diklik
                int celengan_id = celengan.getId();

                // Kirim objek Celengan ke halaman detail menggunakan Intent
                Intent intent = new Intent(DashboardActivity.this, DetailCelenganActivity.class);
                intent.putExtra("CELENGAN_ID", celengan_id);
                intent.putExtra("CELENGAN_ITEM", celengan);
                intent.putExtra("HARGA_PER_HARI", hargaPerHari);
                startActivity(intent);
            }
        });
        celenganAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // Update ListView when the data set has changed
                listViewCelengan.setAdapter(celenganAdapter);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_CELENGAN_REQUEST && resultCode == RESULT_OK) {
            // Update data celengan setelah kembali dari TambahCelengan
            int userId = (int) new SessionManager(this).getLoggedInUserId();
            celenganList = databaseHelper.getAllCelengan(userId);

            // Perbarui data dalam adapter menggunakan metode updateData
            celenganAdapter.updateData(celenganList);

            // Panggil notifyDataSetChanged agar perubahan terlihat di ListView
            celenganAdapter.notifyDataSetChanged();
        }
    }

    // Metode onResume untuk memperbarui ListView setelah kembali dari TambahCelengan
    @Override
    protected void onResume() {
        super.onResume();

        int userId = (int) new SessionManager(this).getLoggedInUserId();
        List<Celengan> updatedList = databaseHelper.getAllCelengan(userId);

        // Perbarui data dalam adapter jika ada perubahan
        if (!celenganList.equals(updatedList)) {
            celenganList = updatedList;
            celenganAdapter.updateData(celenganList);
            listViewCelengan.setAdapter(celenganAdapter);
        }
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Logout
            SessionManager sessionManager = new SessionManager(DashboardActivity.this);
            sessionManager.setAuthenticated(false);
            sessionManager.saveAuthToken(""); // Menghapus token
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan kembali lagi untuk logout", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000); // Waktu dalam milidetik (2 detik)
    }

    // Celengan.java (Model data untuk Celengan)
    public static class Celengan implements Serializable {
        // Properti yang sudah ada
        private int id;
        private byte[] imageBlob;
        private String namaCelengan;
        private int hargaCelengan;
        private int jumlahTerkumpul;
        private int hargaPerHari; // Tambahkan properti untuk harga per hari (Filling Plan)

        // Konstruktor yang sudah ada
        public Celengan(int id, byte[] imageBlob, String namaCelengan, int hargaCelengan, int jumlahTerkumpul, int hargaPerHari) {
            this.id = id;
            this.imageBlob = imageBlob;
            this.namaCelengan = namaCelengan;
            this.hargaCelengan = hargaCelengan;
            this.jumlahTerkumpul = jumlahTerkumpul;
            this.hargaPerHari = hargaPerHari;
        }
        // Metode getter untuk id
        public int getId() {
            return id;
        }
        // Metode getter untuk harga per hari (Filling Plan)
        public int getHargaPerHari() {
            return hargaPerHari;
        }

        // Metode setter untuk harga per hari (Filling Plan)
        public void setHargaPerHari(int hargaPerHari) {
            this.hargaPerHari = hargaPerHari;
        }

        // Metode getter yang sudah ada
        public byte[] getImageBlob() {
            return imageBlob;
        }

        public int getJumlahTerkumpul() {
            return jumlahTerkumpul;
        }

        public String getNamaCelengan() {
            return namaCelengan;
        }

        public int getHargaCelengan() {
            return hargaCelengan;
        }
    }



}
