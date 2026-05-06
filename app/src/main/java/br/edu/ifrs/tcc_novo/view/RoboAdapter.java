package br.edu.ifrs.tcc_novo.view;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.tcc_novo.R;
import br.edu.ifrs.tcc_novo.models.Robo; // Importa o model

public class RoboAdapter extends RecyclerView.Adapter<RoboAdapter.RoboViewHolder> {

    public interface OnRoboClickListener {
        void onRoboClick(Robo robo);
    }

    private List<Robo> robos = new ArrayList<>();
    private final OnRoboClickListener clickListener;
    private final Context context;
    private int posicaoSelecionada = RecyclerView.NO_POSITION;

    public RoboAdapter(Context context, OnRoboClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRobos(List<Robo> novosRobos) {
        this.robos = novosRobos;
        notifyDataSetChanged();
    }

    public Robo getRoboSelecionado() {
        if (posicaoSelecionada != RecyclerView.NO_POSITION && posicaoSelecionada < robos.size()) {
            return robos.get(posicaoSelecionada);
        }
        return null;
    }

    @NonNull
    @Override
    public RoboViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_robo, parent, false);
        return new RoboViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoboViewHolder holder, int position) {
        Robo robo = robos.get(position);
        holder.bind(robo, position, posicaoSelecionada);

        holder.itemView.setOnClickListener(v -> {
            int posicaoAntiga = posicaoSelecionada;
            int posAtual = holder.getAdapterPosition();

            if (posicaoSelecionada == posAtual) {
                posicaoSelecionada = RecyclerView.NO_POSITION;
            } else {
                posicaoSelecionada = posAtual;
            }

            if (clickListener != null) {
                clickListener.onRoboClick(getRoboSelecionado());
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
        return robos.size();
    }

    static class RoboViewHolder extends RecyclerView.ViewHolder {
        ImageView imagemTrajeto;
        TextView textViewPosicao, textViewNomeRobo, textViewPercentual;
        MaterialCardView cardView;

        public RoboViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemTrajeto = itemView.findViewById(R.id.imageViewItemTrajeto);
            textViewPosicao = itemView.findViewById(R.id.textViewPosicao);
            textViewNomeRobo = itemView.findViewById(R.id.textViewNomeRobo);
            textViewPercentual = itemView.findViewById(R.id.textViewPercentualRobo);
            cardView = itemView.findViewById(R.id.cardViewItemRobo); // ID do CardView (de item_robo.xml)
        }

        public void bind(Robo robo, int position, int selectedPosition) {
            // Posição (Ranking)
            textViewPosicao.setText(String.format(Locale.getDefault(), "%d", (position + 1) ));
            textViewNomeRobo.setText(robo.getNome());
//            textViewPercentual.setText(String.format(Locale.getDefault(), "%.2f%% de similaridade", robo.getPercentualIgualdade()));

            // Carrega a imagem
            File imgFile = new File(robo.getCaminhoImagemTrajeto());
            if (imgFile.exists()) {
                Glide.with(imagemTrajeto.getContext())
                        .load(Uri.fromFile(imgFile))
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imagemTrajeto);
            }

            // Lógica de Seleção
            if (position == selectedPosition) {
                cardView.setStrokeWidth(8);
                cardView.setStrokeColor(ContextCompat.getColor(cardView.getContext(), com.google.android.material.R.color.design_default_color_primary));
            } else {
                cardView.setStrokeWidth(0);
            }
        }
    }


}
