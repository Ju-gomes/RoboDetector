package br.edu.ifrs.tcc_novo.models.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import br.edu.ifrs.tcc_novo.models.Robo;

@Dao
public interface RoboDao {
    @Insert
    void insert(Robo robo);

    @Delete
    void delete(Robo robo);

    @Query("SELECT * FROM robo WHERE usuarioId = :usuarioId ORDER BY percentualIgualdade DESC")
    LiveData<List<Robo>> getRobosDoUsuario(int usuarioId);
}