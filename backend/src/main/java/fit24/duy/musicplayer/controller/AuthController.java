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
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
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
}