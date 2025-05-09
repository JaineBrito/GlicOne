package com.example.glicone;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.Calendar;

public class CadastroActivity extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextSenha, editTextDataNascimento;
    private Spinner spinnerDiabetes;
    private Button buttonCadastrar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String dataNascimento;
    private String tipoDiabetes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        spinnerDiabetes = findViewById(R.id.spinnerDiabetes);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tipos_diabetes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiabetes.setAdapter(adapter);

        editTextDataNascimento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int ano = calendar.get(Calendar.YEAR);
            int mes = calendar.get(Calendar.MONTH);
            int dia = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CadastroActivity.this,
                    (View, year, monthOfYear, dayOfMonth) -> {
                        dataNascimento = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        editTextDataNascimento.setText(dataNascimento);
                    },
                    ano, mes, dia);
            datePickerDialog.show();
        });

        buttonCadastrar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();
            tipoDiabetes = spinnerDiabetes.getSelectedItem().toString();
            if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    Toast.makeText(this, "Cadastro realizado! Verifique seu e-mail.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(this, "Falha ao enviar e-mail de verificação.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(nome)
                                                    .build())
                                            .addOnCompleteListener(updateTask -> {
                                                if (updateTask.isSuccessful()) {
                                                    saveUserData(user.getUid(), nome, email, dataNascimento, tipoDiabetes);
                                                    finish();
                                                }
                                            });
                                }
                            } else {
                                task.getException().printStackTrace();
                                Toast.makeText(this, "Erro ao cadastrar usuário: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveUserData (String userId, String nome, String email, String dataNascimento, String tipoDiabetes){
        UserData user = new UserData(nome, email, dataNascimento, tipoDiabetes);
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(aVoid ->
                {
                    Toast.makeText(CadastroActivity.this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(CadastroActivity.this, "Falha ao salvar dados.!", Toast.LENGTH_SHORT).show();
                });

    }
    public static class UserData{
        private String nome;
        private String email;
        private String dataNascimento;
        private String tipoDiabetes;

        public UserData(String nome, String email, String dataNascimento, String tipoDiabetes){
            this.nome = nome;
            this.email = email;
            this.dataNascimento = dataNascimento;
            this.tipoDiabetes = tipoDiabetes;
        }
        public String getNome(){
            return nome;
        }
        public String getEmail(){
            return email;
        }
        public String getDataNascimento(){
            return dataNascimento;
        }
        public String getTipoDiabetes(){
            return tipoDiabetes;
        }
    }
    }
