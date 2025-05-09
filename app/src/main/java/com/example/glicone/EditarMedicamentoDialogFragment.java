package com.example.glicone;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class EditarMedicamentoDialogFragment extends DialogFragment {
    private Medicamento medicamento;
    private editarMedicamentoListener listener;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface editarMedicamentoListener {
        void editarMedicamento(Medicamento medicamento);
    }

    public static EditarMedicamentoDialogFragment newInstance(Medicamento medicamento) {
        EditarMedicamentoDialogFragment fragment = new EditarMedicamentoDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("medicamento", medicamento);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_medicamento_dialog, container, false);

        if (getArguments() != null) {
            medicamento = (Medicamento) getArguments().getSerializable("medicamento");
        }

        EditText etNome = view.findViewById(R.id.et_nome);
        EditText etDose = view.findViewById(R.id.et_dose);
        EditText etHora = view.findViewById(R.id.et_hora);
        EditText etDataInicio = view.findViewById(R.id.et_dataInicio);
        EditText etDataFim = view.findViewById(R.id.et_dataFim);
        Spinner spinnerFrequencia = view.findViewById(R.id.spinner_frequencia);
        Spinner spinnerUnidadeDose = view.findViewById(R.id.spinner_unidade);

        ArrayAdapter<CharSequence> frequenciaAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.frequencia_opcoes,
                android.R.layout.simple_spinner_item
        );
        frequenciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencia.setAdapter(frequenciaAdapter);

        ArrayAdapter<CharSequence> unidadeDoseAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.unidade_dose,
                android.R.layout.simple_spinner_item
        );
        unidadeDoseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidadeDose.setAdapter(unidadeDoseAdapter);

        etNome.setText(medicamento.getNome());
        etHora.setText(medicamento.getHora());
        etDataInicio.setText(medicamento.getDataInicio());
        etDataFim.setText(medicamento.getDataFim());

        String[] doseParts = medicamento.getDose().split(" ");
        String doseValor = doseParts[0];
        String unidade = doseParts.length > 1 ? doseParts[1] : "";

        etDose.setText(doseValor);
        int indexUnidadeDose = unidadeDoseAdapter.getPosition(unidade);
        if (indexUnidadeDose >= 0) {
            spinnerUnidadeDose.setSelection(indexUnidadeDose);
        }

        int indexFrequencia = frequenciaAdapter.getPosition(medicamento.getFrequencia());
        if (indexFrequencia >= 0) {
            spinnerFrequencia.setSelection(indexFrequencia);
        }

        etDataInicio.setFocusable(false);
        etDataInicio.setClickable(true);
        etDataFim.setFocusable(false);
        etDataFim.setClickable(true);
        etHora.setFocusable(false);
        etHora.setClickable(true);

        etDataInicio.setOnClickListener(v -> showDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> showDatePicker(etDataFim));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        Button btnSalvar = view.findViewById(R.id.btn_salvar);
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);

        btnSalvar.setOnClickListener(v -> {
            String dose = etDose.getText().toString();
            String unidadeDose = spinnerUnidadeDose.getSelectedItem().toString();
            String doseComUnidade = dose + " " + unidadeDose;

            medicamento.setNome(etNome.getText().toString());
            medicamento.setDose(doseComUnidade);
            medicamento.setHora(etHora.getText().toString());
            medicamento.setDataInicio(etDataInicio.getText().toString());
            medicamento.setDataFim(etDataFim.getText().toString());
            medicamento.setFrequencia(spinnerFrequencia.getSelectedItem().toString());

            if (listener != null) {
                listener.editarMedicamento(medicamento);
            }

            String medicamentoId = medicamento.getId();

            if (medicamentoId != null) {
                db.collection("medicamentos")
                        .document(medicamentoId)
                        .update(
                                "nome", medicamento.getNome(),
                                "dose", medicamento.getDose(),
                                "hora", medicamento.getHora(),
                                "dataInicio", medicamento.getDataInicio(),
                                "dataFim", medicamento.getDataFim(),
                                "frequencia", medicamento.getFrequencia()
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Medicamento atualizado com sucesso", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Erro ao atualizar medicamento", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getActivity(), "ID do medicamento não encontrado", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dismiss());

        return view;
    }

    public void seteditarMedicamentoListener(editarMedicamentoListener listener) {
        this.listener = listener;
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            targetEditText.setText(selectedDate);
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void showTimePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            targetEditText.setText(selectedTime);
        }, mHour, mMinute, true);

        timePickerDialog.show();
    }
}
