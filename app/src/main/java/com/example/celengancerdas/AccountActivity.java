package com.example.celengancerdas;
import static com.example.celengancerdas.DatabaseHelper.COLUMN_ID;
import static com.example.celengancerdas.DatabaseHelper.COLUMN_NAME;
import static com.example.celengancerdas.DatabaseHelper.TABLE_USERS;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {
    private ListView dataAkunListView;
    private DatabaseHelper databaseHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<Long> userIdList; // ArrayList untuk menyimpan ID pengguna

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_layout);

        dataAkunListView = findViewById(R.id.dataakun);
        databaseHelper = new DatabaseHelper(this);
        userIdList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dataAkunListView.setAdapter(adapter);

        // Panggil metode untuk menampilkan data dari database
        displayUserData();

        dataAkunListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ambil ID pengguna dari ArrayList berdasarkan posisi yang diklik
                long userId = userIdList.get(position);

                // Panggil metode untuk menampilkan informasi data berdasarkan ID
                showDataInfo(userId);
            }
        });

        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                SessionManager sessionManager = new SessionManager(AccountActivity.this);
                sessionManager.setAuthenticated(false);
                sessionManager.saveAuthToken(""); // Menghapus token
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });    }

    private void displayUserData() {
        // Ambil data dari database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + " FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long userId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    String username = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    userIdList.add(userId);
                    adapter.add(username);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
    }

    private void showDataInfo(long id) {
        Intent intent = new Intent(this, DataInfoActivity.class);
        intent.putExtra("user_id", id);
        startActivity(intent);
    }
}
