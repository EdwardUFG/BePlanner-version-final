package com.example.aplicacion_organizadora.perfil;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.data.AppDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserDataFragment extends Fragment {

    private ImageView imgPerfil;
    private Button btnGaleria, btnCamara;

    private EditText etNombre, etCorreo, etCarnet, etCarrera, etTelefono, etContrasena;
    private Button btnEditarGuardar;

    private AppDatabase db;
    private UsuarioDao usuarioDao;
    private final ExecutorService dbExec = Executors.newSingleThreadExecutor();

    private boolean editMode = false;
    private Uri cameraOutputUri;
    private String correoSesion;
    private final ActivityResultLauncher<String> pickFromGallery =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) setAndSavePhoto(uri);
            });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraOutputUri != null) {
                    setAndSavePhoto(cameraOutputUri);
                } else {
                    toast("No se tomó la foto");
                }
            });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_user_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        imgPerfil  = v.findViewById(R.id.imgPerfil);
        btnGaleria = v.findViewById(R.id.btnGaleria);
        btnCamara  = v.findViewById(R.id.btnCamara);

        etNombre     = v.findViewById(R.id.etNombre);
        etCorreo     = v.findViewById(R.id.etCorreo);
        etCarnet     = v.findViewById(R.id.etCarnet);
        etCarrera    = v.findViewById(R.id.etCarrera);
        etTelefono   = v.findViewById(R.id.etTelefono);
        etContrasena = v.findViewById(R.id.etContrasena);
        btnEditarGuardar = v.findViewById(R.id.btnEditarGuardar);

        db = AppDatabase.getDatabase(requireContext());
        usuarioDao = db.usuarioDao();

        SessionManager sm = new SessionManager(requireContext());
        correoSesion = sm.getEmail();

        if (TextUtils.isEmpty(correoSesion)) {
            toast("No hay una sesión activa para cargar los datos.");
            setFieldsEnabled(false); 
            return;
        }

        loadUserData();

        btnGaleria.setOnClickListener(v1 -> pickFromGallery.launch("image/*"));
        btnCamara.setOnClickListener(v12 -> {
            Uri out = createImageUri(requireContext());
            if (out != null) {
                cameraOutputUri = out;
                takePicture.launch(out);
            } else {
                toast("No se pudo preparar archivo de cámara");
            }
        });

        btnEditarGuardar.setOnClickListener(view -> {
            if (!editMode) {
                setEditable(true);
                toast("Edita tus datos y pulsa Guardar");
            } else {
                saveIfValid();
            }
        });
    }

    private Uri createImageUri(Context ctx) {
        try {
            File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir == null) dir = ctx.getFilesDir();
            String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(dir, "perfil_" + time + ".jpg");
            return FileProvider.getUriForFile(
                    ctx,
                    ctx.getPackageName() + ".fileprovider",
                    file
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void setAndSavePhoto(Uri uri) {

        imgPerfil.setImageURI(uri);

        if (TextUtils.isEmpty(correoSesion)) {
            toast("No hay sesión activa");
            return;
        }

        dbExec.execute(() -> {
            try {
                usuarioDao.actualizarFotoPorCorreo(correoSesion, uri.toString());
                runOnUiThreadSafe(() -> toast("Foto actualizada"));
            } catch (Exception e) {
                runOnUiThreadSafe(() -> toast("Error al guardar la foto"));
            }
        });
    }

    private void loadUserData() {
        dbExec.execute(() -> {
            Usuario u = null;
            try {
                if (!TextUtils.isEmpty(correoSesion)) {
                    u = usuarioDao.obtenerPorCorreoSimple(correoSesion);
                }
            } catch (Exception e) {
            }

            final Usuario user = u;
            runOnUiThreadSafe(() -> {
                if (user == null) {
                    toast("No se encontraron datos del usuario.");
                    setFieldsEnabled(false); 
                    return;
                }
                if (user.fotoUri != null && !user.fotoUri.isEmpty()) {
                    imgPerfil.setImageURI(Uri.parse(user.fotoUri));
                } else {
                    imgPerfil.setImageResource(R.drawable.logo);
                }

                etNombre.setText(safe(user.nombre));
                etCorreo.setText(safe(user.correo));
                etCarnet.setText(safe(user.carnet));
                etCarrera.setText(safe(user.carrera));
                etTelefono.setText(safe(user.telefono));
                etContrasena.setText(safe(user.password));
                setEditable(false);
            });
        });
    }
    private void setFieldsEnabled(boolean enabled) {
        etNombre.setEnabled(enabled);
        etCorreo.setEnabled(enabled);
        etCarnet.setEnabled(enabled);
        etCarrera.setEnabled(enabled);
        etTelefono.setEnabled(enabled);
        etContrasena.setEnabled(enabled);
        btnEditarGuardar.setEnabled(enabled);
        btnGaleria.setEnabled(enabled);
        btnCamara.setEnabled(enabled);
    }


    private void saveIfValid() {
        clearErrors();

        final String nombre   = etNombre.getText().toString().trim();
        final String carrera  = etCarrera.getText().toString().trim();
        final String telefono = etTelefono.getText().toString().trim();
        final String password = etContrasena.getText().toString();

        if (TextUtils.isEmpty(nombre) || !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
                || nombre.length() < 25 || nombre.length() > 60) {
            etNombre.setError("Nombre 25-60, solo letras");
            toast("Revisa: Nombre");
            etNombre.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(carrera) || !carrera.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
                || carrera.length() < 20 || carrera.length() > 40) {
            etCarrera.setError("Carrera 20-40, solo letras");
            toast("Revisa: Carrera");
            etCarrera.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(telefono) || !telefono.matches("^\\d{4}-\\d{4}$")) {
            etTelefono.setError("Formato ####-####");
            toast("Revisa: Teléfono");
            etTelefono.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.contains(" ")
                || password.length() < 8 || password.length() > 15) {
            etContrasena.setError("Contraseña 8-15 sin espacios");
            toast("Revisa: Contraseña");
            etContrasena.requestFocus();
            return;
        }

        final String correo = etCorreo.getText().toString().trim();
        dbExec.execute(() -> {
            int rows;
            try {
                rows = usuarioDao.actualizarPerfilPorCorreo(correo, nombre, carrera, telefono, password);
            } catch (Exception e) {
                rows = 0;
            }
            final int r = rows;
            runOnUiThreadSafe(() -> {
                if (r > 0) {
                    toast("Datos actualizados");
                    setEditable(false);
                } else {
                    toast("No se pudo actualizar");
                }
            });
        });
    }

    private void setEditable(boolean enable) {
        setEditState(etNombre, enable);
        setEditState(etCarrera, enable);
        setEditState(etTelefono, enable);
        setEditState(etContrasena, enable);

        setEditState(etCorreo, false);
        setEditState(etCarnet, false);

        btnEditarGuardar.setText(enable ? "Guardar" : "Editar");
        editMode = enable;
    }

    private void setEditState(EditText et, boolean e) {
        et.setFocusable(e);
        et.setFocusableInTouchMode(e);
        et.setClickable(e);
    }

    private void clearErrors() {
        etNombre.setError(null);
        etCarrera.setError(null);
        etTelefono.setError(null);
        etContrasena.setError(null);
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(r);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
