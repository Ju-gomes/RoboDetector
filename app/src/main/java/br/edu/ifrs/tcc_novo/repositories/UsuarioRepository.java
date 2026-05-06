package br.edu.ifrs.tcc_novo.repositories;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.ifrs.tcc_novo.database.AppDatabase;
import br.edu.ifrs.tcc_novo.models.Usuario;
import br.edu.ifrs.tcc_novo.models.dao.UsuarioDao;

public class UsuarioRepository {
    private UsuarioDao mUsuarioDao;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UsuarioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mUsuarioDao = db.usuarioDao();
    }

    public Usuario getUsuarioPorEmail(String email) {
        // Chamado via 'executor' no LoginActivity
        return mUsuarioDao.getUsuarioPorEmail(email);
    }

    public void insert(Usuario usuario) {
        executor.execute(() -> mUsuarioDao.insert(usuario));
    }
}