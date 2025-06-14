package com.example.glicone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmesFragment extends Fragment {

    private RecyclerView recyclerViewAlarms;
    private MedicamentoAdapter medicamentoAdapter;
    private List<Medicamento> listaMedicamentos;
    private FirebaseFirestore db;
    private String usuarioId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarmes, container, false);

        recyclerViewAlarms = rootView.findViewById(R.id.recyclerViewAlarms);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(getActivity()));

        listaMedicamentos = new ArrayList<>();
        medicamentoAdapter = new MedicamentoAdapter(listaMedicamentos, getActivity(), new MedicamentoAdapter.OnMedicamentoActionListener() {

            @Override
            public void onEditar(Medicamento medicamento) {
                Log.d("AlarmesFragment", "Editar: " + medicamento.getNome());

                EditarMedicamentoDialogFragment dialog = EditarMedicamentoDialogFragment.newInstance(medicamento);
                dialog.seteditarMedicamentoListener(new EditarMedicamentoDialogFragment.editarMedicamentoListener() {
                    @Override
                    public void editarMedicamento(Medicamento medicamentoEditado) {
                        atualizarMedicamentoNoFirebase(medicamentoEditado);

                        for (int i = 0; i < listaMedicamentos.size(); i++) {
                            Medicamento m = listaMedicamentos.get(i);
                            if (m.getNome().equals(medicamentoEditado.getNome())) {
                                listaMedicamentos.set(i, medicamentoEditado);
                                medicamentoAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                });

                dialog.show(requireActivity().getSupportFragmentManager(), "editarMedicamentoDialog");
            }

            @Override
            public void onExcluir(Medicamento medicamento) {
                db.collection("medicamentos")
                        .whereEqualTo("userId", usuarioId)
                        .whereEqualTo("nome", medicamento.getNome())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                doc.getReference().delete();
                            }
                            listaMedicamentos.remove(medicamento);
                            medicamentoAdapter.notifyDataSetChanged();
                            Log.d("AlarmesFragment", "Medicamento excluído: " + medicamento.getNome());
                        })
                        .addOnFailureListener(e -> Log.e("AlarmesFragment", "Erro ao excluir: " + e.getMessage()));
            }
        });
        recyclerViewAlarms.setAdapter(medicamentoAdapter);

        db = FirebaseFirestore.getInstance();
        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        carregarMedicamentos();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarMedicamentos();
    }

    private void carregarMedicamentos() {
        db.collection("medicamentos")
                .whereEqualTo("userId", usuarioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaMedicamentos.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Medicamento medicamento = document.toObject(Medicamento.class);
                            listaMedicamentos.add(medicamento);
                            agendarAlarme(medicamento);
                        }
                    }
                    medicamentoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("AlarmesFragment", "Erro ao recuperar dados: " + e.getMessage());
                });
    }

    private void agendarAlarme(Medicamento medicamento) {
        SharedPreferences prefs = requireContext().getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE);
        String alarmKey = medicamento.getNome();

        if (prefs.getBoolean(alarmKey, false)) {
            Log.d("AlarmesFragment", "Alarme já agendado para: " + medicamento.getNome());
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = requireContext().getSystemService(AlarmManager.class);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        String hora = medicamento.getHora();
        if (hora == null || hora.isEmpty()) {
            Log.e("AlarmesFragment", "Hora nula ou vazia para medicamento: " + medicamento.getNome());
            return;
        }

        try {
            String dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date data = sdf.parse(dataAtual + " " + hora);

            if (data != null) {
                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(requireContext(), AlarmeReceiver.class);
                intent.putExtra("nome", medicamento.getNome());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        requireContext().getApplicationContext(),
                        medicamento.getNome().hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, data.getTime(), pendingIntent);
                prefs.edit().putBoolean(alarmKey, true).apply();

                Log.d("AlarmesFragment", "Alarme agendado para: " + data.toString());
            }
        } catch (ParseException e) {
            Log.e("AlarmesFragment", "Erro ao converter hora para data: " + hora);
            e.printStackTrace();
        }
    }

    private void atualizarMedicamentoNoFirebase(Medicamento medicamento) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("medicamentos")
                .whereEqualTo("userId", usuarioId)
                .whereEqualTo("nome", medicamento.getNome())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        doc.getReference().update(
                                "nome", medicamento.getNome(),
                                "dose", medicamento.getDose(),
                                "hora", medicamento.getHora(),
                                "dataInicio", medicamento.getDataInicio(),
                                "dataFim", medicamento.getDataFim(),
                                "frequencia", medicamento.getFrequencia()
                        ).addOnSuccessListener(aVoid -> {
                            Log.d("AlarmesFragment", "Medicamento atualizado com sucesso!");
                        }).addOnFailureListener(e -> {
                            Log.e("AlarmesFragment", "Erro ao atualizar medicamento: " + e.getMessage());
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("AlarmesFragment", "Erro ao recuperar medicamento: " + e.getMessage()));
    }
}
