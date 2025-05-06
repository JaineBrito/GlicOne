package com.example.glicone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonRedefinir;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonRedefinir = findViewById(R.id.buttonRedefinir);
        mAuth = FirebaseAuth.getInstance();

        buttonRedefinir.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "E-mail de redefinição de senha enviado!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Falha ao enviar e-mail de redefinição: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Por favor, insira um e-mail.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}