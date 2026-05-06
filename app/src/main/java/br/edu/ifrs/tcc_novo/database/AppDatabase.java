package br.edu.ifrs.tcc_novo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.edu.ifrs.tcc_novo.models.Pista;
import br.edu.ifrs.tcc_novo.models.Robo;
import br.edu.ifrs.tcc_novo.models.Usuario;
import br.edu.ifrs.tcc_novo.models.dao.PistaDao;
import br.edu.ifrs.tcc_novo.models.dao.RoboDao;
import br.edu.ifrs.tcc_novo.models.dao.UsuarioDao;

@Database(entities = {Pista.class, Robo.class, Usuario.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PistaDao pistaDao();
    public abstract RoboDao roboDao();
    public abstract UsuarioDao usuarioDao();

    private static volatile AppDatabase DATABASE;

    public static AppDatabase getDatabase(final Context context) {
        if (DATABASE == null) {
            synchronized (AppDatabase.class) {
                if (DATABASE == null) {
                    DATABASE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sistema_db")
                            // Permite migração destrutiva (apaga dados) se a versão do schema mudar
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return DATABASE;
    }
}