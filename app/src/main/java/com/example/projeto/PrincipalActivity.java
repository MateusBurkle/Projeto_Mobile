package com.example.projeto;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PrincipalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);

        // Adicionar Tarefa
        View btnAdicionarTarefa = findViewById(R.id.btnAdicionarTarefa);
        if (btnAdicionarTarefa != null) {
            btnAdicionarTarefa.setOnClickListener(v ->
                    startActivity(new Intent(this, TarefasActivity.class))
            );
        }

        // Progresso
        View btnProgresso = findViewById(R.id.btnProgresso);
        if (btnProgresso != null) {
            btnProgresso.setOnClickListener(v ->
                    startActivity(new Intent(this, ProgressoActivity.class))
            );
        }

        // FAB Água
        ExtendedFloatingActionButton fabAddAgua = findViewById(R.id.btnAguaFAB); // ou R.id.fabAddAgua
        if (fabAddAgua != null) {
            fabAddAgua.setOnClickListener(v -> abrirBottomSheetAgua());
        }
    }
    // Melhorar essa parte do codigo ainda
    private void abrirBottomSheetAgua() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.add_agua, null);
        dialog.setContentView(view);

        RadioGroup rgUnidade = view.findViewById(R.id.rgUnidade);
        MaterialRadioButton rbMl = view.findViewById(R.id.rbMl);
        MaterialRadioButton rbCopos = view.findViewById(R.id.rbCopos);

        TextInputLayout tilQtd = view.findViewById(R.id.tilQuantidade);
        TextInputEditText etQtd = view.findViewById(R.id.etQuantidade);

        TextInputLayout tilTamanhoCopo = view.findViewById(R.id.tilTamanhoCopo);
        TextInputEditText etTamanhoCopo = view.findViewById(R.id.etTamanhoCopo);

        MaterialButton btnCancelar = view.findViewById(R.id.btnCancelar);
        MaterialButton btnAdicionar = view.findViewById(R.id.btnAdicionar);

        // Alterna entre mL e copos
        rgUnidade.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMl) {
                tilQtd.setHint("Quantidade (mL)");
                tilTamanhoCopo.setVisibility(View.GONE);
            } else {
                tilQtd.setHint("Quantidade (copos)");
                tilTamanhoCopo.setVisibility(View.VISIBLE);
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAdicionar.setOnClickListener(v -> {
            String qtdStr = etQtd.getText() != null ? etQtd.getText().toString().trim() : "";
            if (qtdStr.isEmpty()) {
                etQtd.setError("Informe a quantidade");
                return;
            }

            int mlAdicionados;
            if (rbMl.isChecked()) {
                mlAdicionados = Integer.parseInt(qtdStr);
            } else {
                String tamStr = etTamanhoCopo.getText() != null ? etTamanhoCopo.getText().toString().trim() : "250";
                if (tamStr.isEmpty()) tamStr = "250";
                int copos = Integer.parseInt(qtdStr);
                int tamCopoMl = Integer.parseInt(tamStr);
                mlAdicionados = copos * tamCopoMl;
            }

            Toast.makeText(this, "Adicionado: " + mlAdicionados + " mL", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
