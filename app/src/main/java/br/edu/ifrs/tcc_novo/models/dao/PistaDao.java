package br.edu.ifrs.tcc_novo.models.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import br.edu.ifrs.tcc_novo.models.Pista;

@Dao
public interface PistaDao {
    @Insert
    void insert(Pista pista);

    @Delete
    void delete(Pista pista);

    @Query("SELECT * FROM pista WHERE usuarioId = :usuarioId")
    LiveData<List<Pista>> getPistasDoUsuario(int usuarioId);
}