package br.edu.ifrs.tcc_novo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import br.edu.ifrs.tcc_novo.R;
import br.edu.ifrs.tcc_novo.utils.SessaoManager;

public class MenuActivity extends AppCompatActivity {

    private Button buttonGerenciarPista, buttonGerenciarRobo, buttonLogout;
    private SessaoManager sessaoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sessaoManager = new SessaoManager(this);

        buttonGerenciarPista = findViewById(R.id.buttonGerenciarPista);
        buttonGerenciarRobo = findViewById(R.id.buttonGerenciarRobo);
        buttonLogout = findViewById(R.id.buttonLogout);

        buttonGerenciarPista.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, GerenciarPistasActivity.class));
        });

        buttonGerenciarRobo.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, GerenciamentoRoboActivity.class));
        });

        buttonLogout.setOnClickListener(v -> {
            sessaoManager.limparSessao();
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}