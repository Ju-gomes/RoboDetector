package br.edu.ifrs.tcc_novo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

import br.edu.ifrs.tcc_novo.database.AppDatabase;
import br.edu.ifrs.tcc_novo.models.Pista;
import br.edu.ifrs.tcc_novo.repositories.PistaRepository;
import br.edu.ifrs.tcc_novo.utils.SessaoManager;
import br.edu.ifrs.tcc_novo.view.PistaAdapter;

public class GerenciarPistasActivity extends AppCompatActivity implements PistaAdapter.OnPistaClickListener {

    private static final String TAG = "GerenciarPistas";

    private RecyclerView recyclerViewPistas;
    private ExtendedFloatingActionButton buttonAdicionarPista,buttonExcluirPista;
    private ExtendedFloatingActionButton buttonIniciarCaptura;
    private Button buttonVoltar; // ainda não usado, mas OK
    private ImageView imageViewPreview;
    private Uri imagemPistaUri;

    private PistaRepository pistaRepository;
    private PistaAdapter pistaAdapter;
    private SessaoManager sessaoManager;
    private int usuarioIdLogado = -1;

    private final ActivityResultLauncher<String> selecionarImagemLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagemPistaUri = uri;
                    if (imageViewPreview != null) {
                        imageViewPreview.setImageURI(uri);
                        imageViewPreview.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_pistas);

        // === VALIDAÇÃO DE SESSÃO ===
        sessaoManager = new SessaoManager(this);
        usuarioIdLogado = sessaoManager.getUserIdLogado();
        if (usuarioIdLogado == -1) {
            Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
            fazerLogout();
            return;
        }

        // === INICIALIZAÇÃO ===
        pistaRepository = new PistaRepository(getApplication());

        recyclerViewPistas = findViewById(R.id.recyclerViewPistas);
        buttonAdicionarPista = findViewById(R.id.buttonAdicionarPista);     // ID do FAB
        buttonIniciarCaptura = findViewById(R.id.buttonIniciarCaptura);     // ID do FAB
        buttonIniciarCaptura.setVisibility(View.GONE);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        buttonExcluirPista = findViewById(R.id.buttonExcluirPista);
//        buttonExcluirPista.setVisibility(View.GONE);// ID do)

        configurarRecyclerView();
        configurarBotoes();
        observarPistas();
    }

    private void configurarBotoes() {
        buttonAdicionarPista.setOnClickListener(v -> mostrarDialogAdicionarPista());

        // === BOTÃO INICIAR CAPTURA ===
        buttonIniciarCaptura.setOnClickListener(v -> {
            Pista pistaSelecionada = pistaAdapter.getPistaSelecionada();
            if (pistaSelecionada == null || pistaSelecionada.getCaminhoImagem() == null) {
                Toast.makeText(this, "Selecione uma pista primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("DEBUG_PISTA", "Botão Iniciar Captura clicado!");
            Log.d("DEBUG_PISTA", "Enviando caminho: " + pistaSelecionada.getCaminhoImagem());

            Intent intent = new Intent(this, CapturaActivity.class);
            intent.putExtra("PISTA_CAMINHO_IMAGEM", pistaSelecionada.getCaminhoImagem());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // === BOTÃO EXCLUIR ===
        buttonExcluirPista.setOnClickListener(v -> {
            Pista pistaParaExcluir = pistaAdapter.getPistaSelecionada();
            if (pistaParaExcluir != null) {
                // Chama o diálogo de confirmação
                mostrarDialogExcluirPista(pistaParaExcluir);
            } else {
                Toast.makeText(this, "Nenhuma pista selecionada para excluir.", Toast.LENGTH_SHORT).show();
            }
        });


        // === BOTÃO VOLTAR ===
        buttonVoltar.setOnClickListener(v -> finish());
    }

    private void fazerLogout() {
        sessaoManager.limparSessao();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void mostrarDialogAdicionarPista() {
        imagemPistaUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_pista, null);
        builder.setView(dialogView);
        builder.setTitle("Adicionar Nova Pista");

        Button buttonSelecionar = dialogView.findViewById(R.id.buttonDialogSelecionar);
        imageViewPreview = dialogView.findViewById(R.id.imageViewDialogPreview);

        buttonSelecionar.setOnClickListener(v -> selecionarImagem());

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            if (imagemPistaUri != null) {
                salvarNovaPista(imagemPistaUri);
            } else {
                Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Mostra um diálogo de confirmação antes de excluir.
     * @param pistaParaExcluir A pista que será excluída.
     */
    private void mostrarDialogExcluirPista(Pista pistaParaExcluir) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Exclusão");
        builder.setMessage("Deseja realmente excluir a pista ID: " + pistaParaExcluir.getId() + "?\n\nEsta ação não pode ser desfeita.");

        // Botão de confirmação
        builder.setPositiveButton("Excluir", (dialog, which) -> {
            executarExclusaoPista(pistaParaExcluir);
        });

        // Botão de cancelamento
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Chama o repositório para excluir a pista e trata o resultado.
     * (Igual ao salvarNovaPista, mas para exclusão)
     * @param pista A pista a ser excluída.
     */
    private void executarExclusaoPista(Pista pista) {
        Toast.makeText(this, "Excluindo pista...", Toast.LENGTH_SHORT).show();

        // **IMPORTANTE**: Você precisará criar o método "excluirPista" no seu PistaRepository
        pistaRepository.excluirPista(pista, new PistaRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarPistasActivity.this, "Pista excluída com sucesso!", Toast.LENGTH_LONG).show();

                    // Limpa a seleção do adapter para esconder os botões
                    if (pistaAdapter != null) {
                        pistaAdapter.limparSelecao(); // Você precisará criar este método no Adapter
                    }
                    // Chama o onPistaClick com null para esconder os botões
                    onPistaClick(null);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarPistasActivity.this, "Erro ao excluir pista: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Falha ao excluir pista", e);
                });
            }
        });
    }

    private void selecionarImagem() {
        selecionarImagemLauncher.launch("image/*");
    }

    private void salvarNovaPista(Uri uri) {
        Toast.makeText(this, "Salvando pista...", Toast.LENGTH_SHORT).show();

        pistaRepository.salvarPista(uri, usuarioIdLogado, new PistaRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarPistasActivity.this, "Pista salva com sucesso!", Toast.LENGTH_LONG).show();
                    if (recyclerViewPistas != null) {
                        recyclerViewPistas.smoothScrollToPosition(0);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarPistasActivity.this, "Erro ao salvar pista: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Falha ao salvar pista", e);
                });
            }
        });
    }

    private void configurarRecyclerView() {
        pistaAdapter = new PistaAdapter(this, this);
        recyclerViewPistas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPistas.setAdapter(pistaAdapter);
    }

    private void observarPistas() {
        LiveData<List<Pista>> pistasLiveData = pistaRepository.getTodasPistas(usuarioIdLogado);

        if (pistasLiveData != null) {
            pistasLiveData.observe(this, pistas -> {
                pistaAdapter.setPistas(pistas);
                Log.d(TAG, "Lista de pistas atualizada. Total: " + pistas.size());
            });
        } else {
            Log.e(TAG, "Falha ao obter LiveData das pistas.");
        }
    }

    // === CLIQUE NA PISTA (APENAS PARA SELEÇÃO) ===
    @Override
    public void onPistaClick(Pista pista) {
        Log.d(TAG, "onPistaClick: " + (pista != null ? pista.getId() : "null"));
        if (pista != null) {
            buttonIniciarCaptura.setVisibility(View.VISIBLE);
            buttonIniciarCaptura.setEnabled(true);
            buttonExcluirPista.setVisibility(View.VISIBLE);
            buttonExcluirPista.setEnabled(true);
        } else {
            buttonIniciarCaptura.setVisibility(View.GONE);
            buttonIniciarCaptura.setEnabled(false);
            buttonExcluirPista.setVisibility(View.GONE);
            buttonExcluirPista.setEnabled(false);
        }
    }
}