package com.example.glicone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CadastroActivity
        extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextSenha;
    private Button buttonCadastrar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);

        mAuth = FirebaseAuth.getInstance();

        buttonCadastrar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if(verifyTask.isSuccessful()){
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
                                                    Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
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
}
