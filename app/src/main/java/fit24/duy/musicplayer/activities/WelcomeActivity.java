package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
//        sessionManager = new SessionManager(this);
//        if (sessionManager.isLoggedIn()) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//            return;
//        }
        
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