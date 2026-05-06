package br.edu.ifrs.tcc_novo;

import android.annotation.SuppressLint;
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

import br.edu.ifrs.tcc_novo.models.Usuario; // Corrigido
import br.edu.ifrs.tcc_novo.repositories.UsuarioRepository; // Corrigido
import br.edu.ifrs.tcc_novo.utils.HashSenha; // Corrigido

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText editTextEmailCadastro, editTextSenhaCadastro, editTextSenhaCadastroConfirmacao;
    private Button buttonCadastrar, buttonVoltar;
    private UsuarioRepository usuarioRepository;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout de cadastro (activity_cadastro.xml)
        setContentView(R.layout.activity_cadastro);

        usuarioRepository = new UsuarioRepository(getApplication());

        editTextEmailCadastro = findViewById(R.id.editTextEmailCadastro);
        editTextSenhaCadastro = findViewById(R.id.editTextSenhaCadastro);
        editTextSenhaCadastroConfirmacao = findViewById(R.id.editTextSenhaCadastroConfirmacao);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        buttonCadastrar.setOnClickListener(v -> cadastrar());
        buttonVoltar.setOnClickListener(v -> finish());
    }

    private void cadastrar() {
        String email = editTextEmailCadastro.getText().toString().trim();
        String senha = editTextSenhaCadastro.getText().toString().trim();
        String senhaConfirm = editTextSenhaCadastroConfirmacao.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(senhaConfirm)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashSenha = HashSenha.sha256(senha);
        if (hashSenha == null) {
            Toast.makeText(this, "Erro ao processar senha", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            // Verifica se o usuário já existe
            Usuario usuarioExistente = usuarioRepository.getUsuarioPorEmail(email);
            if (usuarioExistente != null) {
                runOnUiThread(() -> Toast.makeText(CadastroActivity.this, "Este e-mail já está cadastrado", Toast.LENGTH_SHORT).show());
                return;
            }

            // Cria novo usuário
            Usuario novoUsuario = new Usuario();
            novoUsuario.setEmail(email);
            novoUsuario.setSenha(hashSenha);
            novoUsuario.setAdmin(false); // Padrão

            usuarioRepository.insert(novoUsuario);

            runOnUiThread(() -> {
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish(); // Volta para o Login
            });
        });
    }
}