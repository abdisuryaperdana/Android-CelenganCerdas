package com.example.celengancerdas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {
    TextInputLayout textInputLayoutName, textInputLayoutEmail, textInputLayoutPassword;
    TextInputEditText editTextName, editTextEmail, editTextPassword;
    Button createAccountButton;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        // Inisialisasi tampilan dan DatabaseHelper
        textInputLayoutName = findViewById(R.id.textInputLayoutNameCelengan);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutHargaBarang);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutFillingDailyNominal);

        editTextName = findViewById(R.id.editTextNamaCelenganedit);
        editTextEmail = findViewById(R.id.editTextHargaBarangedit);
        editTextPassword = findViewById(R.id.editTextNominalFillingDaily);

        createAccountButton = findViewById(R.id.btnedit);

        dbHelper = new DatabaseHelper(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ambil data dari input pengguna
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validasi data (Anda bisa menambahkan validasi di sini)
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // Menampilkan pesan jika ada kolom yang kosong
                    Toast.makeText(SignupActivity.this, "Harap isi semua kolom.", Toast.LENGTH_SHORT).show();
                } else {
                    // Periksa apakah email sudah ada dalam database
                    if (dbHelper.isEmailExists(email)) {
                        // Email sudah digunakan, tampilkan pesan kesalahan
                        Toast.makeText(SignupActivity.this, "Email sudah digunakan.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Tambahkan data pengguna ke database
                        boolean isUserAdded = dbHelper.addUser(name, email, password);

                        if (isUserAdded) {
                            // Registrasi berhasil
                            Toast.makeText(SignupActivity.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                            // Arahkan pengguna ke halaman login
                            Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish(); // Selesai dengan aktivitas pendaftaran
                        } else {
                            // Registrasi gagal
                            Toast.makeText(SignupActivity.this, "Registrasi gagal.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
}
