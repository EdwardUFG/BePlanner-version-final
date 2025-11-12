package com.example.aplicacion_organizadora.perfil;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "beplanner_session";
    private static final String K_LOGGED_IN = "logged_in";
    private static final String K_USER_EMAIL = "user_email";
    private static final String K_USER_CARNET = "user_carnet";
    private static final String K_LOGIN_AT = "login_at";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void login(String email, String carnet) {
        sp.edit()
                .putBoolean(K_LOGGED_IN, true)
                .putString(K_USER_EMAIL, email)
                .putString(K_USER_CARNET, carnet)
                .putLong(K_LOGIN_AT, System.currentTimeMillis())
                .apply();
    }

    public void loginWithEmail(String email) {
        sp.edit()
                .putBoolean(K_LOGGED_IN, true)
                .putString(K_USER_EMAIL, email)
                .putLong(K_LOGIN_AT, System.currentTimeMillis())
                .apply();
    }

    public void loginWithCarnet(String carnet) {
        sp.edit()
                .putBoolean(K_LOGGED_IN, true)
                .putString(K_USER_CARNET, carnet)
                .putLong(K_LOGIN_AT, System.currentTimeMillis())
                .apply();
    }

    public boolean isLoggedIn() {
        return sp.getBoolean(K_LOGGED_IN, false)
                && (!isEmpty(getEmail()) || !isEmpty(getCarnet()));
    }

    public String getEmail() {
        return sp.getString(K_USER_EMAIL, null);
    }

    public String getCarnet() {
        return sp.getString(K_USER_CARNET, null);
    }

    public void logout() {
        sp.edit().clear().apply();
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}

