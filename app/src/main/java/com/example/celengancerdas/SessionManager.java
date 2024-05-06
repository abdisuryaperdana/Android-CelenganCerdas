package com.example.celengancerdas;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_AUTHENTICATED = "isAuthenticated";
    private static final String KEY_AUTH_TOKEN = "authToken"; // Kunci untuk menyimpan token otentikasi
    private static final String KEY_NAME = "name";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Mengatur status otentikasi pengguna
    public void setAuthenticated(boolean isAuthenticated) {
        editor.putBoolean(KEY_AUTHENTICATED, isAuthenticated);
        editor.apply();
    }

    // Memeriksa apakah pengguna telah login
    public boolean isAuthenticated() {
        return sharedPreferences.getBoolean(KEY_AUTHENTICATED, false);
    }

    // Simpan token otentikasi
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    // Simpan nama pengguna
    public void saveName(String name) {
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    // Ambil nama pengguna
    public String getName() {
        return sharedPreferences.getString(KEY_NAME, "");
    }
    public void updateName(String newName) {
        editor.putString(KEY_NAME, newName);
        editor.apply();
    }

    // Simpan ID pengguna yang sedang login
    public void saveLoggedInUserId(long userId) {
        editor.putLong(KEY_LOGGED_IN_USER_ID, userId);
        editor.apply();
    }

    // Ambil ID pengguna yang sedang login
    public long getLoggedInUserId() {
        return sharedPreferences.getLong(KEY_LOGGED_IN_USER_ID, -1);
    }

}
