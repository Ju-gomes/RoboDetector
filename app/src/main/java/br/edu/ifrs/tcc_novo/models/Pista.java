package br.edu.ifrs.tcc_novo.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "pista",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "usuarioId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("usuarioId")})
public class Pista {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int usuarioId;
    private String caminhoImagem;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getCaminhoImagem() { return caminhoImagem; }
    public void setCaminhoImagem(String caminhoImagem) { this.caminhoImagem = caminhoImagem; }
}
