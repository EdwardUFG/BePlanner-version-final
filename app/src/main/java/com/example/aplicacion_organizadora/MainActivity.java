package com.example.aplicacion_organizadora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aplicacion_organizadora.perfil.LoginActivity;
import com.example.aplicacion_organizadora.perfil.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void irAInicio(View view) {
        SessionManager sm = new SessionManager(this);
        boolean hasSession = !TextUtils.isEmpty(sm.getEmail()) || !TextUtils.isEmpty(sm.getCarnet());

        if (hasSession) {
            Intent i = new Intent(MainActivity.this, InicioActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "Debes iniciar sesión o registrarte primero", Toast.LENGTH_SHORT).show();
            new com.example.aplicacion_organizadora.perfil.LoginActivity(this).show();
        }
    }


    public void irARegistro(View view) {
        startActivity(new Intent(this, com.example.aplicacion_organizadora.perfil.RegisterActivity.class));
    }

    public void irALogin(View view) {
        new LoginActivity(this).show();
    }
}