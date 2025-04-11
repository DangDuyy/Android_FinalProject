package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import fit24.duy.musicplayer.activities.MainActivity;
import fit24.duy.musicplayer.R;
import com.google.android.material.textfield.TextInputEditText;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.UserRegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText edtUsername;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;
    private Button signUpButton;
    private TextView signInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        signUpButton = findViewById(R.id.sign_up_button);
        signInText = findViewById(R.id.sign_in_text);

        // Set up click listeners
        signUpButton.setOnClickListener(v -> handleSignUp());
        signInText.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void handleSignUp() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Vui lòng nhập tên người dùng");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        if (!((CheckBox) findViewById(R.id.cbTerms)).isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản và điều kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRegisterRequest registerRequest = new UserRegisterRequest(
                username,
                email,
                password
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.register(registerRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Gửi OTP sau khi đăng ký thành công
                    sendOtpAfterRegister(email, username, password);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Đăng ký thất bại: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtpAfterRegister(String email, String username, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.sendOtp(email);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(RegisterActivity.this, OTPActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Gửi OTP thất bại",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối khi gửi OTP",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
} 