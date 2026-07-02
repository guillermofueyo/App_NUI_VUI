package com.example.appnui_vui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import com.example.appnui_vui.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView textViewBrillo;
    private ActivityResultLauncher<Intent> voiceInputLauncher;
    private int brilloActual = 38;

    private void actualizarColorFondo(int porcentaje) {
        int color;
        int colorTexto;

        if (porcentaje == 0) {
            color = android.graphics.Color.parseColor("#212121");
            colorTexto = android.graphics.Color.WHITE;
        } else if (porcentaje <= 20) {
            color = android.graphics.Color.parseColor("#4A148C");
            colorTexto = android.graphics.Color.WHITE;
        } else if (porcentaje <= 40) {
            color = android.graphics.Color.parseColor("#7B1FA2");
            colorTexto = android.graphics.Color.WHITE;
        } else if (porcentaje <= 60) {
            color = android.graphics.Color.parseColor("#CE93D8");
            colorTexto = android.graphics.Color.BLACK;
        } else if (porcentaje <= 80) {
            color = android.graphics.Color.parseColor("#F3E5F5");
            colorTexto = android.graphics.Color.BLACK;
        } else {
            color = android.graphics.Color.parseColor("#FFFFFF");
            colorTexto = android.graphics.Color.BLACK;
        }

        ConstraintLayout layout = findViewById(R.id.main);
        layout.setBackgroundColor(color);
        textViewBrillo.setTextColor(colorTexto);
    }

    private void setBrillo(int porcentaje) {
        int valor = (int) ((porcentaje / 100.0) * 255);
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, valor);
        textViewBrillo.setText("Brillo: " + porcentaje + "%");
        actualizarColorFondo(porcentaje);
        Log.d("Brillo", "setBrillo llamado con: " + porcentaje + "%");
    }

    private void processVoiceCommand(String command) {
        Toast.makeText(this, "Escuché: " + command, Toast.LENGTH_SHORT).show();

        if (command.contains("más") || command.contains("mas")) {
            brilloActual += 10;
        } else if (command.contains("menos")) {
            brilloActual -= 10;
        } else if (command.contains("silenciar") || command.contains("mínimo")) {
            brilloActual = 0;
        } else {
            Toast.makeText(this, "Comando no reconocido: " + command,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        brilloActual = Math.max(0, Math.min(100, brilloActual));

        Log.i("Brillo", "Nuevo brillo: " + brilloActual);
        setBrillo(brilloActual);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Di 'MÁS' para subir el brillo o 'MENOS' para bajarlo");
        voiceInputLauncher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVoiceCommand = findViewById(R.id.btnVoiceCommand);
        textViewBrillo = findViewById(R.id.textViewBrillo);

        if (Settings.System.canWrite(this)) {
            setBrillo(brilloActual);
            actualizarColorFondo(brilloActual);
        } else {
            textViewBrillo.setText("Brillo: ");
        }

        voiceInputLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> resultData =
                                result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (resultData != null && !resultData.isEmpty()) {
                            String comandoLeido = resultData.get(0).toLowerCase();
                            Log.d("RecognizerIntent", "Comando recibido: " + comandoLeido);
                            processVoiceCommand(comandoLeido);
                        }
                    }
                }
        );

        btnVoiceCommand.setOnClickListener(v -> {
            if (Settings.System.canWrite(this)) {
                startVoiceInput();
            } else {
                Toast.makeText(this,
                        "Habilita el permiso para modificar la configuracion en el sistema",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
    }
}