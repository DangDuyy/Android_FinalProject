package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.UserRegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {
    private TextInputEditText edtOtp;
    private TextView tvEmail;
    private String email;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // Get registration data from intent
        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        // Initialize views
        edtOtp = findViewById(R.id.edtOtp);
        tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(email);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Verify button
        findViewById(R.id.btnVerify).setOnClickListener(v -> verifyOtp());

        // Resend OTP
        findViewById(R.id.tvResendOtp).setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String otp = edtOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            edtOtp.setError("Vui lòng nhập mã OTP");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.verifyOtp(email, otp);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    completeRegistration();
                } else {
                    Toast.makeText(OTPActivity.this,
                            "Mã OTP không chính xác",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPActivity.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeRegistration() {
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
                    Toast.makeText(OTPActivity.this,
                            "Đăng ký thành công",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OTPActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(OTPActivity.this,
                            "Xác thực thành công nhưng đăng ký thất bại",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPActivity.this,
                        "Lỗi kết nối khi hoàn tất đăng ký",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.sendOtp(email);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OTPActivity.this,
                            "Đã gửi lại mã OTP",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OTPActivity.this,
                            "Không thể gửi lại mã OTP",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPActivity.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
} 