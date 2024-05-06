package com.example.celengancerdas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TambahCelengan extends AppCompatActivity {

    // Deklarasi variabel yang digunakan
    ImageView imageView;
    EditText editTextNamaCelengan, editTextHargaBarang, editTextFillingDaily;
    Button buttonAddCelengan, buttonChooseImage;

    // Kode konstan untuk membuka galeri
    private static final int PICK_IMAGE = 1;
    private byte[] imageBytes;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tambah_celengan);
        // Simpan data celengan ke database atau sumber data lainnya
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi komponen-komponen pada layout
        imageView = findViewById(R.id.imageView);
        editTextNamaCelengan = findViewById(R.id.editTextNamaCelenganedit);
        editTextHargaBarang = findViewById(R.id.editTextHargaBarangedit);
        editTextFillingDaily = findViewById(R.id.editTextNominalFillingDaily);
        buttonAddCelengan = findViewById(R.id.btneditcelangan);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);

        // Menerapkan filter agar hanya angka yang dapat dimasukkan
        InputFilter numericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // Jika input kosong, izinkan
                if (source.length() < 1) {
                    return null;
                }
                // Cek setiap karakter yang dimasukkan, izinkan hanya jika karakter adalah digit
                if (!Character.isDigit(source.charAt(0))) {
                    return "";
                }
                return null;
            }
        };

        // Terapkan filter hanya untuk masukan harga barang dan filling daily
        editTextHargaBarang.setFilters(new InputFilter[] { numericFilter });
        editTextFillingDaily.setFilters(new InputFilter[] { numericFilter });


        // Logic for choosing image from gallery
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        // Logic for adding Celengan on button click
        buttonAddCelengan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String namaCelengan = editTextNamaCelengan.getText().toString().trim();
                String hargaBarangStr = editTextHargaBarang.getText().toString().trim();
                String fillingDailyStr = editTextFillingDaily.getText().toString().trim();

                // Validasi input agar tidak boleh kosong
                if (imageBytes == null || namaCelengan.isEmpty() || hargaBarangStr.isEmpty() || fillingDailyStr.isEmpty()) {
                    Toast.makeText(TambahCelengan.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proses konversi nilai harga barang dan filling daily ke tipe data double
                double hargaBarang = Double.parseDouble(hargaBarangStr);
                double fillingDaily = Double.parseDouble(fillingDailyStr);

                // Validasi bahwa nilai filling daily tidak boleh lebih besar dari setengah dari harga barang
                if (fillingDaily >= (hargaBarang / 2)) {
                    Toast.makeText(TambahCelengan.this, "Nominal menabung per hari tidak boleh lebih dari setengah dari harga barang", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Logic to save Celengan data and create savings logs
                boolean isDataInserted = saveData(namaCelengan, hargaBarang, fillingDaily, imageBytes);

                if (isDataInserted) {
                    // Mengirimkan hasil kembali ke DashboardActivity
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(TambahCelengan.this, "Celengan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TambahCelengan.this, "Gagal menambahkan Celengan", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to save Celengan data and create savings logs
    private boolean saveData(String namaCelengan, double hargaBarang, double fillingDaily, byte[] imageBytes) {
        if (imageBytes == null) {
            // Tidak ada gambar yang dipilih atau ukuran melebihi 1 MB
            Toast.makeText(this, "Pilih gambar dengan ukuran kurang dari 1 MB", Toast.LENGTH_SHORT).show();
            return false;
        }


        SessionManager sessionManager = new SessionManager(getApplicationContext());
        int userId = (int) sessionManager.getLoggedInUserId();

        boolean isCelenganAdded = dbHelper.tambahCelengan(imageBytes, namaCelengan, (int) hargaBarang, (int) fillingDaily, userId);

        if (isCelenganAdded) {
            int idCelenganBaru = dbHelper.getMaksimalIdCelengan();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String tanggalSekarang = dateFormat.format(calendar.getTime());
            Calendar calendar1 = Calendar.getInstance();
            int hour = calendar1.get(Calendar.HOUR_OF_DAY);
            int minute = calendar1.get(Calendar.MINUTE);
            String waktu = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            boolean isTransactionAdded = dbHelper.transaksi(idCelenganBaru, tanggalSekarang, waktu, 0);

            return isTransactionAdded;
        } else {
            return false;
        }
    }

    // Method to handle the result of choosing an image from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            handleImageSelection(imageUri);
        }
    }

    // Method to handle image selection and check image size
    private void handleImageSelection(Uri imageUri) {
        try {
            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            int imageByteSize = selectedImage.getAllocationByteCount();
            if (imageByteSize > 1000000) {
                // Ukuran gambar lebih dari 1 MB
                Toast.makeText(this, "Ukuran gambar melebihi 1 MB", Toast.LENGTH_SHORT).show();
                imageBytes = null; // Set imageBytes ke null
            } else {
                // Resize the selected image to desired dimensions (232x217)
                Bitmap resizedImage = Bitmap.createScaledBitmap(selectedImage, 450, 450, true);

                imageBytes = getByteArrayFromBitmap(resizedImage);
                imageView.setImageBitmap(resizedImage);
            }
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
    }

    // Method to convert Bitmap to byte array
    private byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
