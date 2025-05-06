package com.example.glicone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> medicamentos;
    private Context context;

    public MedicamentoAdapter(List<Medicamento> medicamentos, Context context) {
        this.medicamentos = medicamentos;
        this.context = context;
    }

    @Override
    public MedicamentoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = medicamentos.get(position);
        holder.tvNome.setText(medicamento.getNome());
        holder.tvDescricao.setText(medicamento.getDescricao());
        holder.tvDataHora.setText(medicamento.getDataHora());
    }

    @Override
    public int getItemCount() {
        return medicamentos.size();
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNome, tvDescricao, tvDataHora;

        public MedicamentoViewHolder(View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_nome);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            tvDataHora = itemView.findViewById(R.id.tv_data_hora);
        }
    }
}
