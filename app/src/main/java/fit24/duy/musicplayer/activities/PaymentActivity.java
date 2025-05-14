package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.utils.SessionManager;

public class PaymentActivity extends AppCompatActivity {

    private Button buttonUploadProof, buttonConfirm;
    private TextView textWait;
    private SessionManager sessionManager;  // Đảm bảo bạn có class này để lưu trạng thái người dùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Ánh xạ view
        buttonUploadProof = findViewById(R.id.button_upload_proof);
        buttonConfirm = findViewById(R.id.button_confirm);
        textWait = findViewById(R.id.text_wait);

        sessionManager = new SessionManager(this);

        // Bắt sự kiện upload minh chứng (ở đây giả lập, có thể chọn ảnh nếu muốn)
        buttonUploadProof.setOnClickListener(v -> {
            // Mở trình chọn ảnh từ bộ nhớ
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");  // Chỉ chọn hình ảnh
            startActivityForResult(intent, 1);  // Mã yêu cầu (request code) là 1
        });


        // Khi người dùng nhấn Confirm
        buttonConfirm.setOnClickListener(v -> {
            // Hiển thị "Wait for 5s"
            textWait.setVisibility(View.VISIBLE);
            buttonConfirm.setEnabled(false);
            buttonUploadProof.setEnabled(false);

            new Handler().postDelayed(() -> {
                // Sau 5 giây: nâng cấp tài khoản người dùng
                sessionManager.setUserStatus(1);  // 1 = Premium

                Toast.makeText(this, "Upgrade successful! You are now a Premium user.", Toast.LENGTH_LONG).show();

                // Quay về PlayerActivity hoặc MainActivity
                Intent intent = new Intent(PaymentActivity.this, PlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }, 5000);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Nhận URI của tệp ảnh đã chọn
            if (data != null) {
                // Lấy URI của ảnh
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Giả sử bạn muốn hiển thị thông báo rằng ảnh đã được tải lên
                    Toast.makeText(this, "Proof uploaded successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

