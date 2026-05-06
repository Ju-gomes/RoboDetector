package br.edu.ifrs.tcc_novo.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.ifrs.tcc_novo.database.AppDatabase;
import br.edu.ifrs.tcc_novo.models.Robo;
import br.edu.ifrs.tcc_novo.models.dao.RoboDao;

public class RoboRepository {
    private RoboDao mRoboDao;
    private LiveData<List<Robo>> mTodosRobos;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Construtor usado pela RoboViewModel
    public RoboRepository(Application application, int usuarioId) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRoboDao = db.roboDao();
        mTodosRobos = mRoboDao.getRobosDoUsuario(usuarioId);
    }

    public LiveData<List<Robo>> getTodosRobos() {
        return mTodosRobos;
    }

    public void insert(Robo robo) {
        executor.execute(() -> mRoboDao.insert(robo));
    }

    public void delete(Robo robo) {
        executor.execute(() -> mRoboDao.delete(robo));
    }
}