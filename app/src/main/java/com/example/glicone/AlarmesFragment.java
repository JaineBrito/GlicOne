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

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarmes, container, false);

        recyclerViewAlarms = rootView.findViewById(R.id.recyclerViewAlarms);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(getActivity()));

        listaMedicamentos = new ArrayList<>();
        medicamentoAdapter = new MedicamentoAdapter(listaMedicamentos, getActivity());
        recyclerViewAlarms.setAdapter(medicamentoAdapter);

        db = FirebaseFirestore.getInstance();

        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("UsuarioId", "ID do usuário: " + usuarioId);
        db.collection("medicamentos")
                .whereEqualTo("userId", usuarioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listaMedicamentos.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Medicamento medicamento = document.toObject(Medicamento.class);
                            listaMedicamentos.add(medicamento);
                            agendarAlarme(medicamento);
                        }
                        medicamentoAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("AlarmesFragment", "Nenhum medicamento encontrado.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AlarmesFragment", "Erro ao recuperar dados: " + e.getMessage());
                });

        return rootView;
    }

    private void agendarAlarme(Medicamento medicamento) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!requireContext().getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date data = sdf.parse(hora);

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
            }
        } catch (ParseException e) {
            Log.e("AlarmesFragment", "Erro ao converter hora para data: " + hora);
            e.printStackTrace();
        }
    }

}

