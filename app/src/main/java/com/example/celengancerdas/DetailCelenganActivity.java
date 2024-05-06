package com.example.celengancerdas;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailCelenganActivity extends AppCompatActivity {
    private ListView listViewSavingsLogs;
    private SavingsLogAdapter adapter;
    private List<SavingsLog> savingsLogsList;
    private DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailcelengan);

        Button buttonMenabung = findViewById(R.id.buttonMenabung);
        Button buttonMengambil = findViewById(R.id.buttonMengambil);
        ImageButton buttonHapusCelengan = findViewById(R.id.buttonHapusCelengan);
        ImageButton buttonEditCelengan = findViewById(R.id.buttonEditCelengan);

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi ListView dari layout
        listViewSavingsLogs = findViewById(R.id.listviewHistoryFillingDailydetail);

        // Mendapatkan ID celengan dari intent (contoh: "CELENGAN_ID")
        int celenganId = getIntent().getIntExtra("CELENGAN_ID", 0);

        // Mendapatkan data log tabungan berdasarkan ID celengan
        savingsLogsList = databaseHelper.getSavingsLogsByCelenganId(celenganId);

        // Membuat adapter baru dengan data log tabungan
        adapter = new SavingsLogAdapter(this, savingsLogsList);

        // Menetapkan adapter ke ListView
        listViewSavingsLogs.setAdapter(adapter);


        // Menerima data yang dikirim dari halaman Dashboard
        Intent intent = getIntent();
        if (intent != null) {
            // Periksa apakah data yang dikirim ada dan sesuai dengan kunci yang digunakan
            if (intent.hasExtra("CELENGAN_ITEM")) {
                // Menerima objek Celengan dari Intent
                DashboardActivity.Celengan celengan = (DashboardActivity.Celengan) intent.getSerializableExtra("CELENGAN_ITEM");
                int hargaPerHari = intent.getIntExtra("HARGA_PER_HARI", 0);
                // Menemukan elemen tampilan di layout
                TextView textViewNamaCelengan = findViewById(R.id.textViewNamaCelengan2);
                ImageView imageViewCelengan = findViewById(R.id.imageViewCelengan2);
                TextView textViewHargaCelengan = findViewById(R.id.textView8);
                TextView textViewHargaPerHari = findViewById(R.id.textView9);
                TextView textViewNominalTerkumpul = findViewById(R.id.textViewNominalTerkumpul2);
                TextView textViewEstimasi = findViewById(R.id.textView10);

                // Mengisi tampilan dengan data dari objek Celengan
                textViewNamaCelengan.setText(celengan.getNamaCelengan());

                // Dapatkan constraint layout parameters dari imageViewCelengan
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imageViewCelengan.getLayoutParams();
                // Tetapkan dimensi gambar menjadi 450x450
                params.width = 450;
                params.height = 450;
                // Menyesuaikan constraint layout parameters dan tata letak ImageView
                params.startToStart = ConstraintSet.PARENT_ID;
                params.endToEnd = ConstraintSet.PARENT_ID;
                params.topToTop = ConstraintSet.PARENT_ID;
                params.bottomToBottom = ConstraintSet.PARENT_ID;
                // Menetapkan parameter layout yang telah diubah
                imageViewCelengan.setLayoutParams(params);
                // Menata tampilan ImageView menggunakan scaleType CENTER_CROP
                imageViewCelengan.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Memuat gambar menggunakan Glide
                Glide.with(this)
                        .load(celengan.getImageBlob())
                        .override(450, 450) // Set dimensi gambar menjadi 450x450
                        .into(imageViewCelengan);

                textViewHargaCelengan.setText(formatRupiah(celengan.getHargaCelengan()));
                textViewHargaPerHari.setText("Menabung " + formatRupiah(celengan.getHargaPerHari()) + " per Hari");
//                textViewNominalTerkumpul.setText("Nominal Terkumpul: " + formatRupiah(celengan.getJumlahTerkumpul()));
                // Mengambil data terakhir dari savings_logs berdasarkan fk celengan_id
                int lastTransactionAmount = databaseHelper.getLastTransactionAmountByCelenganId(celenganId);
                textViewNominalTerkumpul.setText("Nominal Terkumpul: " + formatRupiah(lastTransactionAmount));
                textViewEstimasi.setText("Estimasi: " + estimasiHari(celengan.getHargaCelengan(), lastTransactionAmount, hargaPerHari) + " Hari");

                buttonMenabung.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCelenganActivity.this);
                        builder.setTitle("Konfirmasi");
                        builder.setMessage("Apakah Anda yakin ingin menabung untuk celengan ini?");

                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tambahTransaksi(celenganId, celengan.getHargaPerHari());
                            }
                        });

                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Aksi jika pengguna memilih untuk tidak menghapus
                                // Misalnya, menutup dialog atau melakukan aksi lainnya
                                dialog.dismiss(); // Menutup dialog
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                buttonMengambil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCelenganActivity.this);
                        builder.setTitle("Konfirmasi");
                        builder.setMessage("Apakah Anda yakin ingin mengambil balance celengan ini sesuai dengan nominal perhari?");

                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                kurangiTransaksi(celenganId, celengan.getHargaPerHari());
                            }
                        });

                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Aksi jika pengguna memilih untuk tidak menghapus
                                // Misalnya, menutup dialog atau melakukan aksi lainnya
                                dialog.dismiss(); // Menutup dialog
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                buttonHapusCelengan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCelenganActivity.this);
                        builder.setTitle("Konfirmasi");
                        builder.setMessage("Apakah Anda yakin ingin menghapus data ini?");

                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Lakukan aksi penghapusan data celengan dan savings_log yang berkaitan
                                // Panggil metode untuk menghapus data dan savings log di sini
                                hapusDataCelengan(celenganId);
                                hapusDataSavingsLogByCelenganId(celenganId);
                                // Tambahkan logika lain yang diperlukan setelah penghapusan
                            }
                        });

                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Aksi jika pengguna memilih untuk tidak menghapus
                                // Misalnya, menutup dialog atau melakukan aksi lainnya
                                dialog.dismiss(); // Menutup dialog
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                buttonEditCelengan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Intent untuk membuka EditAccountActivity
                        Intent editIntent = new Intent(DetailCelenganActivity.this, EditCelengan.class);
                        editIntent.putExtra("CELENGAN_ID", celenganId);
                        startActivity(editIntent);
                    }
                });
            }
        }
    }

    // Metode untuk mengonversi harga menjadi format mata uang Rupiah (IDR)
    private String formatRupiah(int harga) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format((double) harga);
    }
    private int estimasiHari(int hargaCelengan, int lastTransactionAmount, int hargaPerHari) {
        // Periksa agar tidak terjadi pembagian dengan nol
        if (hargaPerHari <= 0) {
            return 0; // Atau nilai default yang sesuai dengan kebutuhan Anda
        }

        int sisaTerkumpul = hargaCelengan - lastTransactionAmount;
        if (sisaTerkumpul <= 0) {
            return 0; // Jika sudah mencapai atau melebihi target, return 0 atau nilai yang sesuai
        }

        // Perhitungan estimasi hari yang diperlukan
        int estimasiHari = sisaTerkumpul / hargaPerHari;
        return estimasiHari;
    }

    private void tambahTransaksi(int celenganId, int hargaPerHari) {
        // Mendapatkan tanggal dan waktu saat ini
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String tanggalSekarang = dateFormat.format(calendar.getTime());
        Calendar calendar1 = Calendar.getInstance();
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);
        String waktu = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        // Mendapatkan jumlah terkumpul dari database berdasarkan celenganId
        int jumlahTerkumpul = databaseHelper.getLastJumlahTerkumpulByCelenganId(celenganId);
        jumlahTerkumpul += hargaPerHari;
        // Menambahkan transaksi baru ke tabel savings_logs
        boolean isSuccess = databaseHelper.transaksi(celenganId, tanggalSekarang, waktu, jumlahTerkumpul);

        if (isSuccess) {
            Toast.makeText(this, "Proses menabung berhasil", Toast.LENGTH_SHORT).show();
            // Proses berhasil, lakukan sesuatu jika diperlukan
            Intent intent = new Intent(DetailCelenganActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Proses menabung gagal", Toast.LENGTH_SHORT).show();
            // Proses gagal, lakukan sesuatu jika diperlukan
        }
    }

    private void kurangiTransaksi(int celenganId, int hargaPerHari) {
        // Mendapatkan tanggal dan waktu saat ini
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String tanggalSekarang = dateFormat.format(calendar.getTime());
        Calendar calendar1 = Calendar.getInstance();
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);
        String waktu = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        // Mendapatkan jumlah terkumpul dari database berdasarkan celenganId
        int jumlahTerkumpul = databaseHelper.getLastJumlahTerkumpulByCelenganId(celenganId);

        // Logic to prevent reduction if jumlahTerkumpul is 0
        if (jumlahTerkumpul == 0) {
            Toast.makeText(this, "Tidak ada jumlah transaksi yang bisa diambil. Dikarenakan Balance Celengan anda Rp.0,00", Toast.LENGTH_SHORT).show();
        } else {
            // Kurangi jumlah terkumpul dengan hargaPerHari
            jumlahTerkumpul -= hargaPerHari;
            // Menambahkan transaksi pengurangan ke tabel savings_logs
            boolean isSuccess = databaseHelper.transaksi(celenganId, tanggalSekarang, waktu, jumlahTerkumpul);

            if (isSuccess) {
                Toast.makeText(this, "Proses pengambilan berhasil", Toast.LENGTH_SHORT).show();
                // Update jumlah terkumpul pada tabel celengan
                Intent intent = new Intent(DetailCelenganActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
                // Proses berhasil, lakukan sesuatu jika diperlukan
            } else {
                Toast.makeText(this, "Proses pengambilan gagal", Toast.LENGTH_SHORT).show();
                // Proses gagal, lakukan sesuatu jika diperlukan
            }
        }
    }
    private void hapusDataCelengan(int celenganId) {
        databaseHelper.hapusDataCelengan(celenganId);
    }

    private void hapusDataSavingsLogByCelenganId(int celenganId) {
        databaseHelper.hapusDataSavingsLogByCelenganId(celenganId);
        Toast.makeText(this, "Proses delete data celengan berhasil", Toast.LENGTH_SHORT).show();
        // Update jumlah terkumpul pada tabel celengan
        Intent intent = new Intent(DetailCelenganActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }


}
