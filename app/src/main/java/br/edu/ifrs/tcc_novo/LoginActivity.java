package br.edu.ifrs.tcc_novo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.ifrs.tcc_novo.R;
import br.edu.ifrs.tcc_novo.models.Usuario;
import br.edu.ifrs.tcc_novo.repositories.UsuarioRepository;
import br.edu.ifrs.tcc_novo.utils.HashSenha;
import br.edu.ifrs.tcc_novo.utils.SessaoManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextSenha;
    private Button buttonLogin, buttonIrParaCadastro, buttonSairApp;
    private UsuarioRepository usuarioRepository;
    private SessaoManager sessaoManager;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout de login
        setContentView(R.layout.activity_login);

        usuarioRepository = new UsuarioRepository(getApplication());
        sessaoManager = new SessaoManager(this);

        // Verifica se já está logado
        if (sessaoManager.estaLogado()) {
            irParaMenu();
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonIrParaCadastro = findViewById(R.id.buttonIrParaCadastro);
        buttonSairApp = findViewById(R.id.buttonSairApp);

        buttonLogin.setOnClickListener(v -> login());
        buttonIrParaCadastro.setOnClickListener(v -> {
            // Chama a CadastroActivity (que agora está em seu próprio arquivo)
            startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
        });
        buttonSairApp.setOnClickListener(v -> finishAffinity());
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashSenha = HashSenha.sha256(senha);
        if (hashSenha == null) {
            Toast.makeText(this, "Erro ao processar senha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Executa a verificação do banco em outra thread
        executor.execute(() -> {
            Usuario usuario = usuarioRepository.getUsuarioPorEmail(email);

            // Volta para a thread principal para atualizar a UI
            runOnUiThread(() -> {
                if (usuario != null && usuario.getSenha().equals(hashSenha)) {
                    // Sucesso
                    sessaoManager.salvarSessao(usuario.getId());
                    irParaMenu();
                } else {
                    // Falha
                    Toast.makeText(LoginActivity.this, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void irParaMenu() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        finish(); // Finaliza a LoginActivity
    }
}