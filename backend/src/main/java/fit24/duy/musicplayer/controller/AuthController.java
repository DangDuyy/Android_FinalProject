package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.dto.UserDTO;
import fit24.duy.musicplayer.dto.UserResponse;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.repository.UserRepository;
import fit24.duy.musicplayer.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api") // Giữ nguyên base path là /api
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        // Tìm user theo email
        User user = userRepository.findUserByEmail(userDTO.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại");
        }

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mật khẩu không đúng");
        }

        // Trả về thông tin user
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getProfileImage()));
    }

    // Lấy danh sách users (nếu cần)
    @GetMapping("/users")
    public List<User> users() {
        return authService.users();
    }

    // Gửi OTP
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        String result = authService.SendOtp(email);
        if (result.equals("Error sending OTP")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // Xác thực OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (!authService.VerifyOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
        return ResponseEntity.ok("OTP verified successfully");
    }

    // Đăng ký user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO newUserDTO) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(newUserDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(newUserDTO.getUsername());
        newUser.setEmail(newUserDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(newUserDTO.getPassword()));

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }

    // Quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email,
                                            @RequestParam String newPassword,
                                            @RequestParam String otp) {
        if (!userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        if (!authService.VerifyOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }

        User user = userRepository.findUserByEmail(email);
        user.setPassword(newPassword); // Nên mã hóa password mới
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

    // API để cập nhật thông tin profile (username và ảnh)
    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) MultipartFile profileImage) {
        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Cập nhật username nếu có
        if (username != null && !username.isEmpty() && !username.equals(user.getUsername())) {
            user.setUsername(username);
        }

        // Xử lý upload ảnh nếu có
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Định nghĩa đường dẫn thư mục lưu ảnh
                String uploadDir = "C:/Uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Tạo tên file duy nhất
                String fileName = "user_" + userId + "_" + System.currentTimeMillis() + "." +
                        profileImage.getOriginalFilename().split("\\.")[1];
                Path filePath = Paths.get(uploadDir + fileName);

                // Lưu ảnh vào thư mục
                Files.write(filePath, profileImage.getBytes());

                // Xóa ảnh cũ nếu tồn tại
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    File oldImage = new File(uploadDir + user.getProfileImage());
                    if (oldImage.exists()) {
                        oldImage.delete();
                    }
                }

                // Chỉ lưu tên file ảnh (không lưu đường dẫn)
                user.setProfileImage(fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload image: " + e.getMessage());
            }
        }

        // Lưu thông tin user đã cập nhật
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getProfileImage()));
    }
}