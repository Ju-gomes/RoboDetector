package br.edu.ifrs.tcc_novo.repositories;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import br.edu.ifrs.tcc_novo.database.AppDatabase;
import br.edu.ifrs.tcc_novo.models.Pista;
import br.edu.ifrs.tcc_novo.models.dao.PistaDao;

public class PistaRepository {
    private PistaDao mPistaDao;
    private Context context; // Contexto para lidar com arquivos
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Interface de Callback (como no seu modelo)
    public interface RepositoryCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    // Construtor (como no seu modelo)
    public PistaRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPistaDao = db.pistaDao();
        this.context = application.getApplicationContext();
    }

    // Método de busca (como no seu modelo)
    public LiveData<List<Pista>> getTodasPistas(int usuarioId) {
        if (mPistaDao != null) {
            return mPistaDao.getPistasDoUsuario(usuarioId);
        }
        return null;
    }

    // Método de salvar (como no seu modelo, com lógica de cópia de arquivo)
    public void salvarPista(Uri uri, int usuarioId, final RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Copia o arquivo da URI (Galeria) para o armazenamento interno
                String novoCaminho = copyFileToAppStorage(uri, "PISTA_");
                if (novoCaminho == null) {
                    throw new IOException("Falha ao copiar arquivo da URI.");
                }

                // 2. Cria a entidade Pista com o *novo* caminho
                Pista novaPista = new Pista();
                novaPista.setUsuarioId(usuarioId);
                novaPista.setCaminhoImagem(novoCaminho);
//                novaPista.setDataCadastro(new Date()); // Adiciona a data (baseado no tcc_local)

                // 3. Insere no banco
                mPistaDao.insert(novaPista);

                // 4. Notifica sucesso
                if (callback != null) {
                    callback.onSuccess();
                    // Dentro do onSuccess()
                    Log.d("PistaRepository", "Pista salva com sucesso!");
                    Log.d("PistaRepository", "Caminho: " + novoCaminho);
                    Log.d("PistaRepository", "Arquivo existe? " + new File(novoCaminho).exists());
                }

            } catch (Exception e) {
                Log.e("PistaRepository", "Erro ao salvar pista da URI", e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    // Método auxiliar para copiar o arquivo da galeria para o app
    private String copyFileToAppStorage(Uri uri, String prefix) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = prefix + timeStamp + ".jpg";

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) {
            directory = new File(context.getFilesDir(), "Pictures");
        }
        if (!directory.exists()) directory.mkdirs();

        File file = new File(directory, fileName);

        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } finally {
            inputStream.close();
        }

        String caminho = file.getAbsolutePath();
        Log.d("PistaRepository", "Pista salva em: " + caminho);
        return caminho;
    }

    // === MÉTODO DE EXCLUSÃO COM CALLBACK ADICIONADO ===
    /**
     * Exclui uma pista do banco de dados e seu arquivo de imagem associado.
     * Roda na mesma thread 'executor' e notifica o resultado via callback.
     *
     * @param pista O objeto Pista a ser excluído.
     * @param callback O callback para notificar o sucesso ou falha.
     */
    public void excluirPista(Pista pista, final RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                // --- Etapa 1: Excluir o arquivo de imagem do armazenamento ---
                String caminhoImagem = pista.getCaminhoImagem();
                if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                    File arquivoImagem = new File(caminhoImagem);

                    if (arquivoImagem.exists()) {
                        if (arquivoImagem.delete()) {
                            Log.d("PistaRepository", "Arquivo de imagem excluído: " + caminhoImagem);
                        } else {
                            Log.w("PistaRepository", "Falha ao excluir arquivo de imagem: " + caminhoImagem);
                            // Mesmo que o arquivo falhe, continuamos para excluir o registro do DB
                        }
                    } else {
                        Log.w("PistaRepository", "Arquivo de imagem não encontrado para exclusão: " + caminhoImagem);
                    }
                }

                // --- Etapa 2: Excluir o registro do banco de dados ---
                mPistaDao.delete(pista);

                // --- Etapa 3: Notificar sucesso ---
                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                // --- Etapa 4: Notificar falha ---
                Log.e("PistaRepository", "Erro ao excluir pista", e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

}