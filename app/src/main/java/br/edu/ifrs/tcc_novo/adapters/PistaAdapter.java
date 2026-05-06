package br.edu.ifrs.tcc_novo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.tcc_novo.R;
import br.edu.ifrs.tcc_novo.models.Pista;

public class PistaAdapter extends RecyclerView.Adapter<PistaAdapter.PistaViewHolder> {

    public interface OnPistaClickListener {
        void onPistaClick(Pista pista);
    }

    private List<Pista> pistas = new ArrayList<>();
    private final OnPistaClickListener clickListener;
    private int posicaoSelecionada = RecyclerView.NO_POSITION;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public PistaAdapter(OnPistaClickListener clickListener) {
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

    @NonNull
    @Override
    public PistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pista, parent, false);
        return new PistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PistaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Pista pista = pistas.get(position);
        holder.bind(pista, position, posicaoSelecionada, dateFormat);

        holder.itemView.setOnClickListener(v -> {
            int posicaoAntiga = posicaoSelecionada;

            if (posicaoSelecionada == position) {
                posicaoSelecionada = RecyclerView.NO_POSITION;
            } else {
                posicaoSelecionada = position;
            }

            clickListener.onPistaClick(getPistaSelecionada());

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
//            dataPista = itemView.findViewById(R.id.textViewDataPista);
            nomePista = itemView.findViewById(R.id.textViewNomePista); // Assumindo que item_pista.xml tenha isso
            cardView = itemView.findViewById(R.id.cardViewItemPista);
        }

        public void bind(Pista pista, int position, int selectedPosition, SimpleDateFormat dateFormat) {
            File imgFile = new File(pista.getCaminhoImagem());
            if (imgFile.exists()) {
                Glide.with(imagemPista.getContext())
                        .load(Uri.fromFile(imgFile))
                        .placeholder(R.drawable.ic_launcher_background) // Substituir por um placeholder real
                        .into(imagemPista);
            }else {
                dataPista.setText("Salvando...");
            }

            // Adicionando um nome para a pista (ex: Pista 1)
            nomePista.setText("Pista " + pista.getId());

            if (position == selectedPosition) {
                cardView.setStrokeWidth(8);
                cardView.setStrokeColor(ContextCompat.getColor(cardView.getContext(), R.color.black)); // Substituir por sua cor
            } else {
                cardView.setStrokeWidth(0);
            }
        }
    }
}
