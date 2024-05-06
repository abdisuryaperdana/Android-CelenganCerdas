    package com.example.celengancerdas;

    import android.content.DialogInterface;
    import android.content.Intent;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    public class DataInfoActivity extends AppCompatActivity {
        private DatabaseHelper databaseHelper;
        private long userId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.datainfo_account_layout);

            databaseHelper = new DatabaseHelper(this);

            // Ambil ID pengguna dari Intent
            Intent intent = getIntent();
            userId = intent.getLongExtra("user_id", -1);

            // Jika ID valid, ambil data dari database dan tampilkan
            if (userId != -1) {
                displayUserData(userId);
            }
            // Handle klik tombol update
            Button btnUpdate = findViewById(R.id.btnupdate);
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Intent untuk membuka EditAccountActivity
                    Intent editIntent = new Intent(DataInfoActivity.this, EditAccount.class);
                    editIntent.putExtra("user_id", userId);
                    startActivity(editIntent);
                }
            });
            // Handle klik tombol delete
            Button btnDelete = findViewById(R.id.btndelete);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCurrentUserLoggedIn()) {
                        // User is currently logged in, show a message that deletion is not allowed
                        Toast.makeText(DataInfoActivity.this, "Cannot delete the currently logged-in user", Toast.LENGTH_SHORT).show();
                    } else {
                        // User is not currently logged in, show the delete confirmation dialog
                        showDeleteConfirmationDialog();
                    }
                }
            });
        }

        private void displayUserData(long userId) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                    String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));

                    TextView textViewID = findViewById(R.id.textViewID);
                    TextView textViewName = findViewById(R.id.textViewName);
                    TextView textViewEmail = findViewById(R.id.textViewEmail);

                    textViewID.setText(id);
                    textViewName.setText(name);
                    textViewEmail.setText(email);
                }
                cursor.close();
            }
            db.close();
        }
        private void showDeleteConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Apakah anda yakin ingin menghapus data ini?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                // Proceed with deletion
                                deleteUserData(userId);
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User membatalkan operasi delete
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }

        private void deleteUserData(long userId) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String deleteQuery = "DELETE FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";

            db.execSQL(deleteQuery, new String[]{String.valueOf(userId)});
            db.close();

            // Setelah proses delete selesai, kembali ke DashboardActivity
            Intent dashboardIntent = new Intent(DataInfoActivity.this, DashboardActivity.class);
            startActivity(dashboardIntent);
            finish(); // Tutup aktivitas saat ini agar tidak dapat dikembalikan dengan tombol kembali
        }
        private boolean isCurrentUserLoggedIn() {
            SessionManager SessionManager = new SessionManager(this);
            long loggedInUserId = SessionManager.getLoggedInUserId();
            return loggedInUserId != -1 && loggedInUserId == userId;
        }
    }

