package br.edu.ifrs.tcc_novo.view;

import android.annotation.SuppressLint;
import android.content.Context; // Import necessário
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
// Removido SimpleDateFormat e Locale
import java.util.ArrayList;
import java.util.List;

// Imports corretos (baseado no seu arquivo)
import br.edu.ifrs.tcc_novo.R;
import br.edu.ifrs.tcc_novo.database.AppDatabase;
import br.edu.ifrs.tcc_novo.models.Pista;

/**
 * Adapter para a lista de Pistas (RecyclerView).
 * Gerencia a seleção de itens e o clique.
 */
public class PistaAdapter extends RecyclerView.Adapter<PistaAdapter.PistaViewHolder> {

    // Interface para lidar com o clique
    public interface OnPistaClickListener {
        void onPistaClick(Pista pista);
    }

    private List<Pista> pistas = new ArrayList<>();
    private final OnPistaClickListener clickListener;
    private final Context context; // Necessário para o construtor
    private int posicaoSelecionada = RecyclerView.NO_POSITION;
    // Formatador de data (já que o tcc_local usa datas)
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());


    // CORREÇÃO: Construtor alinhado com o seu modelo (this, this)
    public PistaAdapter(Context context, OnPistaClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPistas(List<Pista> novasPistas) {
        this.pistas = novasPistas;
        notifyDataSetChanged();
    }

    public Pista getPistaSelecionada() {
        if (posicaoSelecionada != RecyclerView.NO_POSITION && posicaoSelecionada < pistas.size()) {
            return pistas.get(posicaoSelecionada);
        }
        return null;
    }

    /**
     * Limpa a seleção atual no adapter.
     * Chamado pela Activity após uma exclusão.
     */
    public void limparSelecao() {
        int posicaoAntiga = posicaoSelecionada;
        posicaoSelecionada = RecyclerView.NO_POSITION;

        // Notifica o item que estava selecionado para que ele seja redesenhado
        // (removendo o destaque)
        if (posicaoAntiga != RecyclerView.NO_POSITION) {
            notifyItemChanged(posicaoAntiga);
        }
    }

    @NonNull
    @Override
    public PistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pista, parent, false);
        return new PistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PistaViewHolder holder, int position) {
        Pista pista = pistas.get(position);
        holder.bind(pista, position, posicaoSelecionada);

        holder.itemView.setOnClickListener(v -> {
            int posicaoAntiga = posicaoSelecionada;
            int posAtual = holder.getAdapterPosition();

            if (posicaoSelecionada == posAtual) {
                posicaoSelecionada = RecyclerView.NO_POSITION;
            } else {
                posicaoSelecionada = posAtual;
            }

            if(clickListener != null) {
                clickListener.onPistaClick(getPistaSelecionada());
            }

            if (posicaoAntiga != RecyclerView.NO_POSITION) {
                notifyItemChanged(posicaoAntiga);
            }
            if(posicaoSelecionada != RecyclerView.NO_POSITION) {
                notifyItemChanged(posicaoSelecionada);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pistas.size();
    }

    static class PistaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagemPista;
        TextView dataPista, nomePista;
        MaterialCardView cardView;

        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPista = itemView.findViewById(R.id.imageViewItemPista);
            dataPista = itemView.findViewById(R.id.textViewDataPista);
            nomePista = itemView.findViewById(R.id.textViewNomePista);
            cardView = itemView.findViewById(R.id.cardViewItemPista);
        }

        public void bind(Pista pista, int position, int selectedPosition) {
            File imgFile = new File(pista.getCaminhoImagem());
            if (imgFile.exists()) {
                Glide.with(imagemPista.getContext())
                        .load(Uri.fromFile(imgFile))
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imagemPista);
            }

            // Mostra a data (já que o tcc_local usa)
//            if (pista.getDataCadastro() != null) {
//                dataPista.setText("Salva em: " + dateFormat.format(pista.getDataCadastro()));
//            }
            else {
                dataPista.setText("Salvando...");
            }
            nomePista.setText("Pista " + pista.getId());

            if (position == selectedPosition) {
                cardView.setStrokeWidth(8);
                cardView.setStrokeColor(ContextCompat.getColor(cardView.getContext(), com.google.android.material.R.color.design_default_color_primary));
            } else {
                cardView.setStrokeWidth(0);
            }
        }
    }
}