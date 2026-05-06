package br.edu.ifrs.tcc_novo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Importado para confirmação
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
// import androidx.recyclerview.widget.ItemTouchHelper; // REMOVIDO
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

import br.edu.ifrs.tcc_novo.view.RoboAdapter;
import br.edu.ifrs.tcc_novo.models.Robo;
import br.edu.ifrs.tcc_novo.repositories.RoboRepository;
import br.edu.ifrs.tcc_novo.utils.SessaoManager;
// --- ATUALIZAÇÃO: Implementa a interface do Adapter ---
public class GerenciamentoRoboActivity extends AppCompatActivity implements RoboAdapter.OnRoboClickListener {

    private static final String TAG = "GerenciamentoRobo";

    private RoboRepository roboRepository;
    private RoboAdapter adapter;
    private SessaoManager sessaoManager;
    private Robo roboSelecionado;

    private RecyclerView recyclerViewRobos;
    private Button buttonAdicionarRobo, buttonExcluirRobo, buttonVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//      Não foi utilizado o Toast pois é um texto longo
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Atenção!\nLinha vermelha: desenho da pista\nLinha verde: trajeto do robô",
                Snackbar.LENGTH_LONG
        );

        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);

// Permite multiline
        textView.setMaxLines(5);
        textView.setSingleLine(false);
        snackbar.setDuration(5000);
        snackbar.show();


        setContentView(R.layout.activity_gerenciar_robos);

        sessaoManager = new SessaoManager(this);
        int usuarioId = sessaoManager.getUserIdLogado();

        if (usuarioId == -1) {
            Toast.makeText(this, "Erro de sessão, faça login novamente", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        roboRepository = new RoboRepository(getApplication(), usuarioId);

        recyclerViewRobos = findViewById(R.id.recyclerViewRobos);
//        buttonAdicionarRobo = findViewById(R.id.buttonAdicionarRobo);
        buttonExcluirRobo = findViewById(R.id.buttonExcluirRobo);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        configurarRecyclerView();
        configurarBotoes();
        observarRobos(usuarioId);
    }

    private void configurarRecyclerView() {
        adapter = new RoboAdapter(this, this);
        recyclerViewRobos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRobos.setAdapter(adapter);
    }

    private void configurarBotoes() {
        // Botão Adicionar não faz sentido aqui, mas desabilitamos
//        buttonAdicionarRobo.setEnabled(false);
//        buttonAdicionarRobo.setOnClickListener(v -> {
//            Toast.makeText(this, "Para adicionar um robô, inicie uma captura pela tela de Pistas.", Toast.LENGTH_LONG).show();
//        });

        // Botão Excluir
        buttonExcluirRobo.setEnabled(false); // Começa desabilitado
        buttonExcluirRobo.setOnClickListener(v -> confirmarExclusao());
        buttonVoltar.setOnClickListener(v -> finish());

    }

    private void observarRobos(int usuarioId) {
        LiveData<List<Robo>> robosLiveData = roboRepository.getTodosRobos();

        if (robosLiveData != null) {
            robosLiveData.observe(this, robos -> {
                adapter.setRobos(robos);
                Log.d(TAG, "Lista de robôs atualizada. Total: " + robos.size());
            });
        } else {
            Log.e(TAG, "Falha ao obter LiveData dos robôs.");
        }
    }

    @Override
    public void onRoboClick(Robo robo) {
        this.roboSelecionado = robo;
        buttonExcluirRobo.setEnabled(robo != null);

        String caminho = robo.getCaminhoImagemTrajeto();
        Log.d("CLICK", "Clicou no robô: " + robo.getNome() + " | Caminho: " + caminho);

        Intent intent = new Intent(this, ExibirTrajetoActivity.class);
        intent.putExtra("CAMINHO_TRAJETO", caminho);
        startActivity(intent);
    }

    private void confirmarExclusao() {
        if (roboSelecionado == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Excluir Robô")
                .setMessage("Tem certeza que deseja excluir o robô '" + roboSelecionado.getNome() + "'?")
                .setPositiveButton("Sim", (dialog, which) -> excluirRoboSelecionado())
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirRoboSelecionado() {
        if (roboSelecionado == null) return;

        // Tenta excluir o arquivo de imagem do trajeto
        try {
            File file = new File(roboSelecionado.getCaminhoImagemTrajeto());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir arquivo de imagem do trajeto", e);
        }

        // Exclui do banco
        roboRepository.delete(roboSelecionado);

        roboSelecionado = null;
        onRoboClick(null); // Atualiza os botões

        Toast.makeText(this, "Robô excluído", Toast.LENGTH_SHORT).show();
    }
}