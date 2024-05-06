package com.example.celengancerdas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class EditAccount extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editaccount_layout);

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Ambil ID pengguna dari Intent
        Intent intent = getIntent();
        userId = intent.getLongExtra("user_id", -1);

        // Inisialisasi elemen UI
        TextInputEditText editTextName = findViewById(R.id.editTextNameedit);
        TextInputEditText editTextEmail = findViewById(R.id.editTextEmailedit);
        TextInputEditText editTextPassword = findViewById(R.id.editTextPasswordedit);

        // Ambil data celengan dari database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                // Ambil nilai dari database
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));

                // Set nilai ke TextInputEditText
                editTextName.setText(name);
                editTextEmail.setText(email);
            }
        } finally {
            // Pastikan cursor ditutup, bahkan jika ada exception
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        // Handle klik tombol save
        Button btnSave = findViewById(R.id.btnedit);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ambil nilai dari TextInputEditText
                String newName = editTextName.getText().toString();
                String newEmail = editTextEmail.getText().toString();
                String newPassword = editTextPassword.getText().toString();

                // Ambil ulang data pengguna dari database
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor recursor = null;

                try {
                    String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
                    recursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

                    if (recursor != null && recursor.moveToFirst()) {
                        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newPassword)) {
                            Toast.makeText(EditAccount.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                        } else if (isEmailAlreadyInUse(newEmail)) {
                            Toast.makeText(EditAccount.this, "Email is already in use by another user", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proses penyimpanan data ke database
                            SQLiteDatabase updateDb = databaseHelper.getWritableDatabase();
                            String updateQuery = "UPDATE " + DatabaseHelper.TABLE_USERS +
                                    " SET " + DatabaseHelper.COLUMN_NAME + " = ?, " +
                                    DatabaseHelper.COLUMN_EMAIL + " = ?, " +
                                    DatabaseHelper.COLUMN_PASSWORD + " = ?" +
                                    " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
                            updateDb.execSQL(updateQuery, new String[]{newName, newEmail, newPassword, String.valueOf(userId)});
                            updateDb.close();
                            // Update the name in SessionManager
                            SessionManager sessionManager = new SessionManager(EditAccount.this);
                            sessionManager.updateName(newName);
                            // Setelah proses penyimpanan selesai, kembali ke DataInfoActivity
                            Intent backIntent = new Intent(EditAccount.this, DashboardActivity.class);
                            backIntent.putExtra("user_id", userId);
                            startActivity(backIntent);
                            finish(); // Tutup aktivitas saat ini agar tidak dapat dikembalikan dengan tombol kembali
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

    // Helper method to check if the email is already in use
    private boolean isEmailAlreadyInUse(String emailToCheck) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to check if the email is already in use by a different user
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                    " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ? AND " + DatabaseHelper.COLUMN_ID + " != ?";

            cursor = db.rawQuery(query, new String[]{emailToCheck, String.valueOf(userId)});

            // Return true if the cursor has any results, indicating that the email is already in use
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

}
