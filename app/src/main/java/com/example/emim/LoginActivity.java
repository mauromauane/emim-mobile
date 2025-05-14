package com.example.emim;


import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLoginEmail;
    private TextInputEditText etLoginPassword;
    private Button btnLogin;
    private MaterialButton ibApple, ibFacebook, ibGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_card);

        // find views
        etLoginEmail    = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin        = findViewById(R.id.btnLogin);

        // social buttons (inside the inner LinearLayout)
        ibApple    = findViewById(R.id.btnApple);
        ibFacebook = findViewById(R.id.btnFacebook);
        ibGoogle   = findViewById(R.id.btnGoogle);

        // handle Login tap
        btnLogin.setOnClickListener(v -> {
            String email = etLoginEmail.getText()    .toString().trim();
            String pass  = etLoginPassword.getText() .toString();

          /*  if (email.isEmpty()) {
                etLoginEmail.setError("Email required");
                etLoginEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etLoginEmail.setError("Enter a valid email");
                etLoginEmail.requestFocus();
                return;
            }
            if (pass.isEmpty()) {
                etLoginPassword.setError("Password required");
                etLoginPassword.requestFocus();
                return;
            }
            if (pass.length() < 6) {
                etLoginPassword.setError("Min 6 characters");
                etLoginPassword.requestFocus();
                return;
            } */
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // close this activity so the user can't return to it
            // TODO: perform your authentication (e.g. call your API)
          //  Toast.makeText(this, "Logging in…", Toast.LENGTH_SHORT).show();
        });

        // handle Apple login
        ibApple.setOnClickListener(v -> {
            // TODO: trigger Apple sign-in flow
            Toast.makeText(this, "Sign in with Apple", Toast.LENGTH_SHORT).show();
        });

        // handle Facebook login
        ibFacebook.setOnClickListener(v -> {
            // TODO: trigger Facebook login SDK
            Toast.makeText(this, "Sign in with Facebook", Toast.LENGTH_SHORT).show();
        });

        // handle Google login
        ibGoogle.setOnClickListener(v -> {
            // TODO: trigger Google Sign-In API
            Toast.makeText(this, "Sign in with Google", Toast.LENGTH_SHORT).show();
        });
    }
}
