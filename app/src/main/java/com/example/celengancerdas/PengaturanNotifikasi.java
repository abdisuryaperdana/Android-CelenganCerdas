package com.example.celengancerdas;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Calendar;
import java.util.List;

public class PengaturanNotifikasi extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private Spinner spinnerCelengan;
    private static final String PREFS_NAME = "MyPrefsFile1";
    private PendingIntent pendingIntent;
    private SharedPreferences sharedPreferences;
    private TextView textViewStatenotif;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pengaturan_notifikasi);

        databaseHelper = new DatabaseHelper(this);
        spinnerCelengan = findViewById(R.id.spinnerCelengan);

        textViewStatenotif = findViewById(R.id.textViewStatenotif);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Cek status terakhir dari SharedPreferences dan atur teks textViewStatenotif
        boolean isNotificationSet = sharedPreferences.getBoolean("NOTIFICATION_SET", false);
        if (isNotificationSet) {
            textViewStatenotif.setText("Ada Notifikasi");
        } else {
            textViewStatenotif.setText("Tidak Ada Notifikasi");
        }

        // Ambil user_id dari sesi atau aktivitas sebelumnya
        int userId = getIntent().getIntExtra("USER_ID", 0);
        // Ambil data celengan dari DatabaseHelper berdasarkan user_id
        List<String> celenganList = databaseHelper.getNameCelenganByUserId(userId);

        // Tampilkan data dalam Spinner untuk memilih celengan
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, celenganList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCelengan.setAdapter(adapter);

        // Setelah pengguna memilih celengan, simpan id celengan yang dipilih
        spinnerCelengan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Implementasi jika tidak ada yang dipilih
            }
        });
        Button buttonSetNotification = findViewById(R.id.buttonsetnotifikasi);
        buttonSetNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker timePicker = findViewById(R.id.timePicker);
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                // Ambil data celengan dari DatabaseHelper berdasarkan user_id
                List<String> celenganList = databaseHelper.getNameCelenganByUserId(userId);

                // Tampilkan toast jika list celengan kosong
                if (celenganList.isEmpty()) {
                    Toast.makeText(PengaturanNotifikasi.this, "List celengan kosong", Toast.LENGTH_SHORT).show();
                } else {
                    setNotificationForCelengan(hour, minute);
                }
            }
        });
        Button buttonDeleteNotification = findViewById(R.id.buttondeletenotif);
        buttonDeleteNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNotification();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cek status terakhir dari SharedPreferences
        boolean isNotificationSet = sharedPreferences.getBoolean("NOTIFICATION_SET", false);

        // Atur teks textViewStatenotif berdasarkan status notifikasi
        if (isNotificationSet) {
            textViewStatenotif.setText("Ada Notifikasi");
        } else {
            textViewStatenotif.setText("Tidak Ada Notifikasi");
        }
    }

    private void setNotificationForCelengan(int hour, int minute) {
        // Buat intent yang akan dipicu oleh alarm
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("CELENGAN_NAME", spinnerCelengan.getSelectedItem().toString());
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Atur waktu notifikasi menggunakan AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long alarmStartTime = calendar.getTimeInMillis();
        long interval = AlarmManager.INTERVAL_DAY;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, interval, pendingIntent);

        // Simpan status notifikasi ke SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NOTIFICATION_SET", true);
        editor.apply();

        // Tampilkan pesan bahwa notifikasi telah diatur
        Toast.makeText(this, "Notifikasi diatur di jam " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
    }
    private void deleteNotification() {
        // Batalkan PendingIntent yang memiliki ID yang sama dengan yang digunakan saat pertama kali membuatnya
        Intent intent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Lakukan pembatalan notifikasi
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        // Setelah menghapus notifikasi, ubah statusnya ke "Tidak Ada Notifikasi" di SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NOTIFICATION_SET", false);
        editor.apply();

        // Atur teks textViewStatenotif
        textViewStatenotif.setText("Tidak Ada Notifikasi");

        // Tampilkan pesan bahwa notifikasi telah dihapus
        Toast.makeText(this, "Notifikasi Telah Dihapus", Toast.LENGTH_SHORT).show();
    }

}