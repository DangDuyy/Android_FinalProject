package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fit24.duy.musicplayer.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Sign up button
        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Login button
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginOptionsActivity.class));
        });
    }
} 