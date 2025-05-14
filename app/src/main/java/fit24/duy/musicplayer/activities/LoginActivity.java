package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.UserLoginRequest;
import fit24.duy.musicplayer.models.UserResponse;
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Retrieve user status from SessionManager (after initialization)
        int userStatus = sessionManager.getUserStatus();  // Moved inside onCreate()

        // Initialize views
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Login button
        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin(userStatus));  // Pass userStatus here

        // Forgot password
        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> {
            // TODO: Implement forgot password
            Toast.makeText(this, "Chức năng quên mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogin(int userStatus) {  // Accept userStatus as parameter
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();

                    // Lưu phiên đăng nhập với SessionManager
                    sessionManager.createLoginSession(
                            String.valueOf(user.getId()),
                            user.getUsername(),
                            user.getEmail(),
                            user.getToken(),
                            userStatus
                    );

                    // Chuyển hướng đến MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    String errorMsg = "Đăng nhập thất bại";
                    if (response.code() == 401) {
                        errorMsg = "Email hoặc mật khẩu không đúng";
                    } else if (response.code() == 400) {
                        errorMsg = "Dữ liệu không hợp lệ";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
