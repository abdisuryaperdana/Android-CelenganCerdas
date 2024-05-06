package com.example.celengancerdas;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inisialisasi SessionManager
        SessionManager sessionManager = new SessionManager(this);

        // Periksa status otentikasi
        if (sessionManager.isAuthenticated()) {
            // Pengguna telah login, arahkan ke DashboardActivity
            String name = sessionManager.getName();
            Intent dashboardIntent = new Intent(MainActivity.this, DashboardActivity.class);
            dashboardIntent.putExtra("name", name);
            startActivity(dashboardIntent);
            finish(); // Selesai dengan aktivitas MainActivity
        }
        else {

            Button buttonPindahHalaman = findViewById(R.id.btnedit);
            buttonPindahHalaman.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            Button buttonPindahHalaman2 = findViewById(R.id.pindahsignup);
            buttonPindahHalaman2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}