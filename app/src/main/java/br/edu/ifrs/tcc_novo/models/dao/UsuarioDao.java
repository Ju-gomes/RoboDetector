package br.edu.ifrs.tcc_novo.models.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import br.edu.ifrs.tcc_novo.models.Usuario;

@Dao
public interface UsuarioDao {
    @Insert
    void insert(Usuario usuario);

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    Usuario getUsuarioPorEmail(String email);

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    Usuario getUsuarioPorId(int id);
}