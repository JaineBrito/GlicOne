package com.example.glicone;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CadastroMedicamentoFragment extends Fragment {

    private EditText etNome, etDose, etDataInicio, etDataFim, etHora;
    private Spinner spinnerUnidade;
    private Spinner spinnerFrequencia;
    private Button btnSalvar;
    private FirebaseFirestore db;

    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cadastro_medicamento, container, false);
        db = FirebaseFirestore.getInstance();
        etNome = rootView.findViewById(R.id.et_titulo);
        etDose = rootView.findViewById(R.id.et_dose);
        etDataInicio = rootView.findViewById(R.id.et_dataInicio);
        etDataFim = rootView.findViewById(R.id.et_dataFim);
        etHora = rootView.findViewById(R.id.et_hora);
        btnSalvar = rootView.findViewById(R.id.btn_salvar);
        spinnerUnidade = rootView.findViewById(R.id.spinner_unidade);
        spinnerFrequencia = rootView.findViewById(R.id.spinner_frequencia);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.unidade_dose, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidade.setAdapter(adapter);

        ArrayAdapter<CharSequence> frequenciaAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.frequencia_opcoes, android.R.layout.simple_spinner_item);
        frequenciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencia.setAdapter(frequenciaAdapter);

        etDataInicio.setFocusable(false);
        etDataInicio.setClickable(true);
        etDataFim.setFocusable(false);
        etDataFim.setClickable(true);
        etHora.setFocusable(false);
        etHora.setClickable(true);

        etDataInicio.setOnClickListener(v -> showDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> showDatePicker(etDataFim));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString();
            String dose = etDose.getText().toString();
            String unidade = spinnerUnidade.getSelectedItem().toString();
            String doseCompleta = dose + " " + unidade;
            String dataInicio = etDataInicio.getText().toString();
            String dataFim = etDataFim.getText().toString();
            String hora = etHora.getText().toString();
            String frequencia = spinnerFrequencia.getSelectedItem().toString();

            if (nome.isEmpty() || dose.isEmpty() || dataInicio.isEmpty() || hora.isEmpty() || frequencia.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor, preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dataFim.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date inicio = sdf.parse(dataInicio);
                    Date fim = sdf.parse(dataFim);

                    if (fim.before(inicio)) {
                        Toast.makeText(getActivity(), "A data de fim não pode ser antes da data de início", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), "Erro ao interpretar as datas", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String dataFimFinal = dataFim.isEmpty() ? "Indefinido" : dataFim;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userId = user != null ? user.getUid() : null;

            Medicamento medicamento = new Medicamento(nome, doseCompleta, hora, dataInicio, dataFimFinal, frequencia, userId);

            db.collection("medicamentos")
                    .add(medicamento)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getActivity(), "Medicamento salvo com sucesso", Toast.LENGTH_SHORT).show();
                        etNome.setText("");
                        etDose.setText("");
                        etDataInicio.setText("");
                        etDataFim.setText("");
                        etHora.setText("");
                        setAlarm(frequencia, nome, dataInicio, hora);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Erro ao salvar medicamento", Toast.LENGTH_SHORT).show();
                    });
        });

        return rootView;
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            targetEditText.setText(selectedDate);
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void showTimePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            targetEditText.setText(selectedTime);
        }, mHour, mMinute, true);

        timePickerDialog.show();
    }

    private void setAlarm(String frequencia, String nome, String dataInicio, String hora) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dataHora = dataInicio + " " + hora;
        Date date;
        try {
            date = sdf.parse(dataHora);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        long triggerTime = date.getTime();

        long intervaloMillis;
        switch (frequencia) {
            case "Diariamente":
                intervaloMillis = AlarmManager.INTERVAL_DAY;
                break;
            case "Semanalmente":
                intervaloMillis = AlarmManager.INTERVAL_DAY * 7;
                break;
            case "Mensalmente":
                intervaloMillis = AlarmManager.INTERVAL_DAY * 30;
                break;
            default:
                intervaloMillis = AlarmManager.INTERVAL_DAY;
        }

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(getActivity(), AlarmeReceiver.class);
        intent.putExtra("nome", nome);
        intent.putExtra("intervalo", intervaloMillis);
        intent.putExtra("frequencia", frequencia);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity(),
                nome.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } catch (SecurityException e) {
                Toast.makeText(getActivity(), "Permissão necessária.", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(getActivity(), "Alarme agendado para " + hora + " (" + frequencia + ")", Toast.LENGTH_SHORT).show();
    }
}