package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import fit24.duy.musicplayer.R;

public class LoginOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_options);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Email login
        findViewById(R.id.btnLoginEmail).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Phone login
        findViewById(R.id.btnLoginPhone).setOnClickListener(v -> {
            // TODO: Implement phone login
        });

        // Register
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
} 