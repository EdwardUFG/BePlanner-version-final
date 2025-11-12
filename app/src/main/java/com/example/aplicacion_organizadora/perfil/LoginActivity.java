package com.example.aplicacion_organizadora.perfil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aplicacion_organizadora.InicioActivity;
import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.data.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity {

    private final Context context;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public LoginActivity(Context context) {
        this.context = context;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_login, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
        dialog.show();

        EditText edtCorreo   = dialogView.findViewById(R.id.edtCorreo);
        EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Button btnLogin      = dialogView.findViewById(R.id.btnLoginDialog);

        AppDatabase db = AppDatabase.getDatabase(context);
        UsuarioDao usuarioDao = db.usuarioDao();

        btnLogin.setOnClickListener(v -> {
            String correo   = edtCorreo.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (TextUtils.isEmpty(correo)) {
                edtCorreo.setError("Requerido");
                toast("Ingresa tu correo");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                edtCorreo.setError("Formato de correo inválido");
                toast("Correo inválido");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Requerido");
                toast("Ingresa tu contraseña");
                return;
            }

            dbExecutor.execute(() -> {
                boolean ok = false;
                try {
                    ok = usuarioDao.existePorCorreoYPassword(correo, password);
                } catch (Exception e) {
                    post(() -> toast("Error de base de datos"));
                    return;
                }

                if (ok) {
                    post(() -> {
                        toast("¡Bienvenido!");
                        SessionManager sm = new SessionManager(context);
                        sm.loginWithEmail(correo);
                        Intent intent = new Intent(context, InicioActivity.class);
                        context.startActivity(intent);
                        dialog.dismiss();
                    });

                } else {
                    post(() -> {
                        toast("Debe registrarse");
                        edtCorreo.requestFocus();
                        edtCorreo.setError("No existe cuenta con este correo/contraseña");
                    });
                }
            });
        });
    }

    private void post(Runnable r) { main.post(r); }
    private void toast(String msg) { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show(); }
}
