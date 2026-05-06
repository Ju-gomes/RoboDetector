package br.edu.ifrs.tcc_novo.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "robo",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "usuarioId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("usuarioId")})
public class Robo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int usuarioId;
    private String nome;
    private String caminhoImagemTrajeto;
    private double percentualIgualdade;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCaminhoImagemTrajeto() { return caminhoImagemTrajeto; }
    public void setCaminhoImagemTrajeto(String caminhoImagemTrajeto) { this.caminhoImagemTrajeto = caminhoImagemTrajeto; }
    public double getPercentualIgualdade() { return percentualIgualdade; }
    public void setPercentualIgualdade(double percentualIgualdade) { this.percentualIgualdade = percentualIgualdade; }
}