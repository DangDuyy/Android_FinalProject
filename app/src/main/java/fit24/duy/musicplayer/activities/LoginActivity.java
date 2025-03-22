package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import fit24.duy.musicplayer.R;

public class LoginActivity extends AppCompatActivity {
    private Button signUpButton;
    private Button googleLoginButton;
    private Button facebookLoginButton;
    private Button appleLoginButton;
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        signUpButton = findViewById(R.id.sign_up_button);
        googleLoginButton = findViewById(R.id.google_login_button);
        facebookLoginButton = findViewById(R.id.facebook_login_button);
        appleLoginButton = findViewById(R.id.apple_login_button);
        loginTextView = findViewById(R.id.login_text);

        // Set up click listeners
        signUpButton.setOnClickListener(v -> handleSignUp());
        googleLoginButton.setOnClickListener(v -> handleGoogleLogin());
        facebookLoginButton.setOnClickListener(v -> handleFacebookLogin());
        appleLoginButton.setOnClickListener(v -> handleAppleLogin());
        loginTextView.setOnClickListener(v -> handleLogin());
    }

    private void handleSignUp() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleGoogleLogin() {
        // Implement Google login
        startMainActivity();
    }

    private void handleFacebookLogin() {
        // Implement Facebook login
        startMainActivity();
    }

    private void handleAppleLogin() {
        // Implement Apple login
        startMainActivity();
    }

    private void handleLogin() {
        // Handle regular login
        startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
} 