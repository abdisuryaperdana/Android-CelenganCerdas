package com.example.celengancerdas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button loginButton;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // Inisialisasi tampilan dan DatabaseHelper

        editTextEmail = findViewById(R.id.loginemail);
        editTextPassword = findViewById(R.id.loginpassword);

        loginButton = findViewById(R.id.btnedit);

        dbHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ambil data dari input pengguna
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validasi data (Anda bisa menambahkan validasi di sini)
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // Menampilkan pesan jika ada kolom yang kosong
                    Toast.makeText(LoginActivity.this, "Harap isi semua kolom.", Toast.LENGTH_SHORT).show();
                } else {
                    // Periksa apakah email dan kata sandi sesuai dengan data dalam database
//                    boolean isValidUser = dbHelper.checkUserLogin(email, password);
                    long userId = dbHelper.checkUserLoginAndGetId(email, password);
//                    if (isValidUser) {
                    if (userId != -1) {
                        // Login berhasil
                        Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                        String name = dbHelper.getNameByEmail(email);
                        SessionManager sessionManager = new SessionManager(LoginActivity.this);
                        sessionManager.saveName(name);
                        sessionManager.saveLoggedInUserId(userId);
                        sessionManager.setAuthenticated(true);
                        sessionManager.saveAuthToken("authToken"); // Ganti dengan token yang sesuai
                        // Arahkan pengguna ke halaman Dashboard
                        Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        dashboardIntent.putExtra("name", name);
                        startActivity(dashboardIntent);
                        finish(); // Selesai dengan aktivitas login
                    } else {
                        // Login gagal
                        Toast.makeText(LoginActivity.this, "Login gagal. Cek email dan kata sandi Anda.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
