package com.example.glicone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glicone.MainActivity;
import com.example.glicone.RedefinirSenhaActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private TextView txtUsuarioInvalido;
    private FirebaseAuth mAuth;
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.login);
        editSenha = findViewById(R.id.senha);
        txtUsuarioInvalido = findViewById(R.id.usuarioinvalido);
        checkBox = findViewById(R.id.checkBox);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        TextView criarConta = findViewById(R.id.criarConta);
        criarConta.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, com.example.glicone.CadastroActivity.class);
            startActivity(intent);
        });
        TextView esqueceuSenha = findViewById(R.id.esqueceuSenha);
        esqueceuSenha.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RedefinirSenhaActivity.class);
            startActivity(intent);
        });

        if(sharedPreferences.contains("login") && sharedPreferences.contains("senha")){
            editEmail.setText(sharedPreferences.getString("login", ""));
            editSenha.setText(sharedPreferences.getString("senha", ""));
            checkBox.setChecked(true);
        }
    }

    public void fazLogin(View view) {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            txtUsuarioInvalido.setVisibility(View.INVISIBLE);
                            Toast.makeText(this, "Login bem-sucedido! Bem-vindo(a) " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                            if(checkBox.isChecked()){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("login", email);
                                editor.putString("senha", senha);
                                editor.apply();
                            }

                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        txtUsuarioInvalido.setVisibility(View.VISIBLE);
                    }
                });
    }
}
