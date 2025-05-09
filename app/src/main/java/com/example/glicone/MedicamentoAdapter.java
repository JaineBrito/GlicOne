package com.example.glicone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> medicamentos;
    private Context context;
    private OnMedicamentoActionListener listener;

    public interface OnMedicamentoActionListener {
        void onEditar(Medicamento medicamento);
        void onExcluir(Medicamento medicamento);
    }

    public MedicamentoAdapter(List<Medicamento> medicamentos, Context context, OnMedicamentoActionListener listener) {
        this.medicamentos = medicamentos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = medicamentos.get(position);

        holder.tvNome.setText("Medicamento: " + medicamento.getNome());
        holder.tvDose.setText("Dosagem: " + medicamento.getDose());
        holder.tvDataInicio.setText("Início: " + medicamento.getDataInicio());
        holder.tvDataFim.setText("Fim: " + medicamento.getDataFim());
        holder.tvHora.setText("Hora: " + medicamento.getHora());
        holder.tvFrequencia.setText("Frequência: " + medicamento.getFrequencia());

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(medicamento);
                medicamentos.set(position, medicamento);
                notifyItemChanged(position);
            }
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExcluir(medicamento);
                medicamentos.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicamentos.size();
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNome, tvDose, tvDataInicio, tvDataFim, tvHora, tvFrequencia;
        Button btnEditar, btnExcluir;

        public MedicamentoViewHolder(View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_nome);
            tvDose = itemView.findViewById(R.id.tv_dose);
            tvDataInicio = itemView.findViewById(R.id.tv_dataInicio);
            tvDataFim = itemView.findViewById(R.id.tv_dataFim);
            tvHora = itemView.findViewById(R.id.tv_hora);
            tvFrequencia = itemView.findViewById(R.id.tv_frequencia);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnExcluir = itemView.findViewById(R.id.btn_excluir);
        }
    }
}
