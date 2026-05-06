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
import br.edu.ifrs.tcc_novo.models.Robo;

public class RoboAdapter extends RecyclerView.Adapter<RoboAdapter.RoboViewHolder> {

    private List<Robo> robos = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setRobos(List<Robo> novosRobos) {
        this.robos = novosRobos;
        notifyDataSetChanged();
    }

    public Robo getRoboAt(int position) {
        if (position >= 0 && position < robos.size()) {
            return robos.get(position);
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
        holder.bind(robo, position + 1);
    }

    @Override
    public int getItemCount() {
        return robos.size();
    }

    static class RoboViewHolder extends RecyclerView.ViewHolder {
        ImageView imagemTrajeto;
        TextView textViewPosicao, textViewNomeRobo, textViewPercentual;

        public RoboViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemTrajeto = itemView.findViewById(R.id.imageViewItemTrajeto);
            textViewPosicao = itemView.findViewById(R.id.textViewPosicao);
            textViewNomeRobo = itemView.findViewById(R.id.textViewNomeRobo);
            textViewPercentual = itemView.findViewById(R.id.textViewPercentualRobo);
        }

        public void bind(Robo robo, int posicao) {
            textViewPosicao.setText(String.format(Locale.getDefault(), "%dº", posicao));
            textViewNomeRobo.setText(robo.getNome());
            textViewPercentual.setText(String.format(Locale.getDefault(), "%.2f%% de similaridade", robo.getPercentualIgualdade()));

            File imgFile = new File(robo.getCaminhoImagemTrajeto());
            if (imgFile.exists()) {
                Glide.with(imagemTrajeto.getContext())
                        .load(Uri.fromFile(imgFile))
                        .placeholder(R.drawable.ic_launcher_background) // Substituir por placeholder
                        .into(imagemTrajeto);
            }
        }
    }
}