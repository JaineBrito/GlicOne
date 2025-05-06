package com.example.glicone;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class CadastroMedicamentoFragment extends Fragment {

    private EditText etNome, etDescricao, etDataHora;
    private Button btnSalvar;
    private FirebaseFirestore db;

    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cadastro_medicamento, container, false);

        etNome = rootView.findViewById(R.id.et_titulo);
        etDescricao = rootView.findViewById(R.id.et_descricao);
        etDataHora = rootView.findViewById(R.id.et_data_hora);
        btnSalvar = rootView.findViewById(R.id.btn_salvar);

        db = FirebaseFirestore.getInstance();

        etDataHora.setFocusable(false);
        etDataHora.setClickable(true);
        etDataHora.setOnClickListener(v -> showDateTimePickerDialog());

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString();
            String descricao = etDescricao.getText().toString();
            String dataHora = etDataHora.getText().toString();

            if (nome.isEmpty() || descricao.isEmpty() || dataHora.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user != null ? user.getUid() : null;

                Medicamento medicamento = new Medicamento(nome, descricao, dataHora, userId);

                db.collection("medicamentos")
                        .add(medicamento)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "Medicamento salvo com sucesso", Toast.LENGTH_SHORT).show();
                            etNome.setText("");
                            etDescricao.setText("");
                            etDataHora.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Erro ao salvar medicamento", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        return rootView;
    }

    private void showDateTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                            (timeView, hourOfDay, minute) -> {
                                mHour = hourOfDay;
                                mMinute = minute;
                                etDataHora.setText(String.format("%02d/%02d/%d %02d:%02d", mDay, mMonth + 1, mYear, mHour, mMinute));
                            }, mHour, mMinute, true);
                    timePickerDialog.show();
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }
}
