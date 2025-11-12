package com.example.aplicacion_organizadora.perfil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.data.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNombre, edtCorreo, edtCarnet, edtCarrera, edtTelefono, edtPassword;

    private AppDatabase db;
    private UsuarioDao usuarioDao;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtNombre   = findViewById(R.id.edtNombre);
        edtCorreo   = findViewById(R.id.edtCorreo);
        edtCarnet   = findViewById(R.id.edtCarnet);
        edtCarrera  = findViewById(R.id.edtCarrera);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtPassword = findViewById(R.id.edtPassword);

        db = AppDatabase.getDatabase(this);
        usuarioDao = db.usuarioDao();

        setClearErrorOnTyping(edtNombre, edtCorreo, edtCarnet, edtCarrera, edtTelefono, edtPassword);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }

    public void onGuardarClick(View v) {
        if (!validarCamposEnUI()) return;

        final String correo   = edtCorreo.getText().toString().trim();
        final String carnet   = edtCarnet.getText().toString().trim();
        final String telefono = edtTelefono.getText().toString().trim();

        dbExecutor.execute(() -> {
            try {
                if (usuarioDao.existeCarnet(carnet)) {
                    runOnUiThread(() -> {
                        showWarn("Carnet ya registrado. Por favor, inicia sesión.");
                        edtCarnet.requestFocus();
                        edtCarnet.setError("Carnet ya registrado");
                    });
                    return;
                }
                if (!TextUtils.isEmpty(correo) && usuarioDao.existeCorreo(correo)) {
                    runOnUiThread(() -> {
                        showWarn("El correo ya está registrado.");
                        edtCorreo.requestFocus();
                        edtCorreo.setError("Correo ya registrado");
                    });
                    return;
                }
                if (!TextUtils.isEmpty(telefono) && usuarioDao.existeTelefono(telefono)) {
                    runOnUiThread(() -> {
                        showWarn("El teléfono ya está registrado.");
                        edtTelefono.requestFocus();
                        edtTelefono.setError("Teléfono ya registrado");
                    });
                    return;
                }

                Usuario u = new Usuario();
                u.nombre   = normalizeSpaces(edtNombre.getText().toString());
                u.correo   = correo;
                u.carnet   = carnet;
                u.carrera  = normalizeSpaces(edtCarrera.getText().toString());
                u.telefono = telefono;
                u.password = edtPassword.getText().toString().trim();

                usuarioDao.insertarUsuario(u);

                runOnUiThread(() -> {
                    showInfo("¡Registro exitoso! Ahora puedes iniciar sesión.");
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> showWarn("Error de base de datos. Intenta de nuevo."));
            }
        });
    }

    private boolean validarCamposEnUI() {
        String nombre   = normalizeSpaces(edtNombre.getText().toString());
        String correo   = edtCorreo.getText().toString().trim();
        String carnet   = edtCarnet.getText().toString().trim();
        String carrera  = normalizeSpaces(edtCarrera.getText().toString());
        String telefono = edtTelefono.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(nombre)) return fail(edtNombre, "Campo obligatorio");
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))
            return fail(edtNombre, "Solo letras y espacios");
        if (nombre.length() < 15 || nombre.length() > 60)
            return fail(edtNombre, "Debe tener entre 15 y 60 caracteres");

        if (TextUtils.isEmpty(correo)) return fail(edtCorreo, "Campo obligatorio");
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches())
            return fail(edtCorreo, "Formato de correo inválido");
        if (!correo.toLowerCase().endsWith("@beplanner.sv"))
            return fail(edtCorreo, "Debe terminar en @beplanner.sv");
        if (correo.length() > 40)
            return fail(edtCorreo, "Máximo 40 caracteres");

        if (TextUtils.isEmpty(carnet)) return fail(edtCarnet, "Campo obligatorio");
        if (!carnet.matches("^[a-zA-Z0-9]{8}$"))
            return fail(edtCarnet, "Debe tener exactamente 8 letras/números");

        if (TextUtils.isEmpty(carrera)) return fail(edtCarrera, "Campo obligatorio");
        if (!carrera.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))
            return fail(edtCarrera, "No se permiten números ni símbolos");
        if (carrera.length() < 15 || carrera.length() > 40)
            return fail(edtCarrera, "Debe tener entre 15 y 40 caracteres");

        if (TextUtils.isEmpty(telefono)) return fail(edtTelefono, "Campo obligatorio");
        if (!telefono.matches("^\\d{4}-\\d{4}$"))
            return fail(edtTelefono, "Formato debe ser ####-####");

        if (TextUtils.isEmpty(password)) return fail(edtPassword, "Campo obligatorio");
        if (password.contains(" ")) return fail(edtPassword, "No puede contener espacios");
        if (password.length() < 8 || password.length() > 15)
            return fail(edtPassword, "Debe tener entre 8 y 15 caracteres");
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*"))
            return fail(edtPassword, "Incluye letras y números");

        clearErrors(edtNombre, edtCorreo, edtCarnet, edtCarrera, edtTelefono, edtPassword);
        return true;
    }

    private boolean fail(EditText field, String msg) {
        field.setError(msg);
        field.requestFocus();
        showWarn(msg);
        return false;
    }

    private void showWarn(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showInfo(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void clearErrors(EditText... edits) {
        for (EditText e : edits) e.setError(null);
    }

    private String normalizeSpaces(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s{2,}", " ");
    }

    private void setClearErrorOnTyping(EditText... edits) {
        for (EditText e : edits) {
            e.addTextChangedListener(new SimpleTextWatcher(() -> e.setError(null)));
        }
    }
}
