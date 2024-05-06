package com.example.celengancerdas;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditCelengan extends AppCompatActivity {
    private DatabaseHelper databaseHelper;

    Button buttonChooseImage;
    private byte[] imageBytes;

    ImageView imageView;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_celengan);

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Ambil ID celengan dari Intent
      //  Intent intent = getIntent();
        // celenganId = intent.getLongExtra("celengan_id", 0);
        int celenganId = getIntent().getIntExtra("CELENGAN_ID", 0);

        // Inisialisasi elemen UI
        TextInputEditText editTextNameCelengan = findViewById(R.id.editTextNamaCelenganedit);
        TextInputEditText editTextHargaBarang = findViewById(R.id.editTextHargaBarangedit);
        TextInputEditText editTextNominalFillingDaily = findViewById(R.id.editTextNominalFillingDaily);
        imageView = findViewById(R.id.imageView);

        // Ambil data celengan dari database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_CELENGAN + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(celenganId)});

            if (cursor != null && cursor.moveToFirst()) {
                // Ambil nilai dari database
                String namabarang = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAMA_CELENGAN));
                String hargabarang = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_HARGA_BARANG));
                String nominalfillingdailyplan = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FILLING_PLAN));
                byte[] imageData = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_GAMBAR_BARANG));

                // Set nilai ke TextInputEditText
                editTextNameCelengan.setText(namabarang);
                editTextHargaBarang.setText(hargabarang);
                editTextNominalFillingDaily.setText(nominalfillingdailyplan);

                // Dapatkan constraint layout parameters dari imageViewCelengan
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
                // Tetapkan dimensi gambar menjadi 450x450
                params.width = 450;
                params.height = 450;
                // Menyesuaikan constraint layout parameters dan tata letak ImageView
                params.startToStart = ConstraintSet.PARENT_ID;
                params.endToEnd = ConstraintSet.PARENT_ID;
                params.topToTop = ConstraintSet.PARENT_ID;
                params.bottomToBottom = ConstraintSet.PARENT_ID;
                // Menetapkan parameter layout yang telah diubah
                imageView.setLayoutParams(params);
                // Menata tampilan ImageView menggunakan scaleType CENTER_CROP
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Memuat gambar menggunakan Glide
                Glide.with(this)
                        .load(imageData)
                        .override(450, 450) // Set dimensi gambar menjadi 450x450
                        .into(imageView);

                // Simpan byte array gambar untuk memungkinkan perubahan gambar
                imageBytes = imageData;
            }
        } finally {
            // Pastikan cursor ditutup, bahkan jika ada exception
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        // Inisialisasi buttonChooseImage dan imageView
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        imageView = findViewById(R.id.imageView);

        // Logic for choosing image from gallery
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        // Handle klik tombol save
        Button btnSave = findViewById(R.id.btneditcelangan);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ambil nilai dari TextInputEditText
                String newNameCelengan = editTextNameCelengan.getText().toString();
                String newHargaBarang = editTextHargaBarang.getText().toString();
                String newNominalFillingDaily = editTextNominalFillingDaily.getText().toString();

                // Ambil ulang data celengan dari database
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor recursor = null;

                try {
                    String query = "SELECT * FROM " + DatabaseHelper.TABLE_CELENGAN + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
                    recursor = db.rawQuery(query, new String[]{String.valueOf(celenganId)});

                    if (recursor != null && recursor.moveToFirst()) {
                        if (TextUtils.isEmpty(newNameCelengan) || TextUtils.isEmpty(newHargaBarang) || TextUtils.isEmpty(newNominalFillingDaily)) {
                            Toast.makeText(EditCelengan.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                        } else {
                            if (imageBytes == null) {
                                // Tidak ada perubahan pada gambar celengan
                                updateCelenganData(newNameCelengan, newHargaBarang, newNominalFillingDaily, imageBytes);
                            } else {
                                // Ada perubahan pada gambar celengan
                                updateCelenganDataWithImage(newNameCelengan, newHargaBarang, newNominalFillingDaily, imageBytes);
                            }
                        }
                    }
                } finally {
                    // Pastikan cursor ditutup, bahkan jika ada exception
                    if (recursor != null) {
                        recursor.close();
                    }
                    db.close();
                }
            }
        });
    }

    // Method untuk memperbarui data celengan tanpa perubahan gambar
    private void updateCelenganData(String newNameCelengan, String newHargaBarang, String newNominalFillingDaily, byte[] imageData) {
        // Proses penyimpanan data ke database
        SQLiteDatabase updateDb = databaseHelper.getWritableDatabase();
        String updateQuery = "UPDATE " + DatabaseHelper.TABLE_CELENGAN +
                " SET " + DatabaseHelper.COLUMN_NAMA_CELENGAN + " = ?, " +
                DatabaseHelper.COLUMN_HARGA_BARANG + " = ?, " +
                DatabaseHelper.COLUMN_FILLING_PLAN + " = ?" +
                " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
        int celenganId = getIntent().getIntExtra("CELENGAN_ID", 0);
        updateDb.execSQL(updateQuery, new String[]{newNameCelengan, newHargaBarang, newNominalFillingDaily, String.valueOf(celenganId)});
        updateDb.close();
        // Kembali ke DashboardActivity setelah proses penyimpanan selesai
        Intent backIntent = new Intent(EditCelengan.this, DashboardActivity.class);
        startActivity(backIntent);
        finish(); // Tutup aktivitas saat ini agar tidak dapat dikembalikan dengan tombol kembali
    }

    // Method untuk memperbarui data celengan dengan perubahan gambar
    private void updateCelenganDataWithImage(String newNameCelengan, String newHargaBarang, String newNominalFillingDaily, byte[] newImageBytes) {
        // Proses penyimpanan data (termasuk gambar baru) ke database
        SQLiteDatabase updateDb = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAMA_CELENGAN, newNameCelengan);
        values.put(DatabaseHelper.COLUMN_HARGA_BARANG, newHargaBarang);
        values.put(DatabaseHelper.COLUMN_FILLING_PLAN, newNominalFillingDaily);
        values.put(DatabaseHelper.COLUMN_GAMBAR_BARANG, newImageBytes);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        int celenganId = getIntent().getIntExtra("CELENGAN_ID", 0);
        String[] whereArgs = {String.valueOf(celenganId)};

        updateDb.update(DatabaseHelper.TABLE_CELENGAN, values, whereClause, whereArgs);
        updateDb.close();

        // Kembali ke DashboardActivity setelah proses penyimpanan selesai
        Intent backIntent = new Intent(EditCelengan.this, DashboardActivity.class);
        startActivity(backIntent);
        finish(); // Tutup aktivitas saat ini agar tidak dapat dikembalikan dengan tombol kembali
    }


    // Method untuk meng-handle hasil pemilihan gambar dari galeri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            handleImageSelection(imageUri);
        }
    }

    // Method untuk menangani pemilihan gambar dan memeriksa ukuran gambar
    private void handleImageSelection(Uri imageUri) {
        try {
            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            int imageByteSize = selectedImage.getAllocationByteCount();
            if (imageByteSize > 1000000) {
                // Ukuran gambar lebih dari 1 MB
                Toast.makeText(this, "Ukuran gambar melebihi 1 MB", Toast.LENGTH_SHORT).show();
                imageBytes = null; // Set imageBytes ke null
            } else {
                // Resize gambar yang dipilih ke dimensi yang diinginkan (450x450)
                Bitmap resizedImage = Bitmap.createScaledBitmap(selectedImage, 450, 450, true);

                // Konversi gambar menjadi byte array
                imageBytes = getByteArrayFromBitmap(resizedImage);

                // Tampilkan gambar yang dipilih di ImageView
                imageView.setImageBitmap(resizedImage);
            }
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
    }

    // Method untuk mengubah Bitmap menjadi byte array
    private byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}

