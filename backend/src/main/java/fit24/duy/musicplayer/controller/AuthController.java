package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.dto.UserDTO;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.repository.UserRepository;
import fit24.duy.musicplayer.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/login")
    public String Login(@RequestBody UserDTO user) {
        return authService.Login(user.getEmail(), user.getPassword());
    }

    @GetMapping("/users")
    public List<User> Users(){
        List<User> u = authService.users();
        System.out.println(u.size());
        return u;
    }

    @PostMapping("/sendcode")
    public String SendOtp(@RequestParam String email) {
        return authService.SendOtp(email);
    }


    @PostMapping("/register")
    public ResponseEntity<?> Register(@RequestBody UserDTO newUserDTO, @RequestParam String otp) {
        // Kiểm tra OTP
        if (!authService.VerifyOtp(newUserDTO.getEmail(), otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP"); // Trả về mã lỗi 400
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(newUserDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists"); // Trả về mã lỗi 409
        }

        // Chuyển UserDTO thành User
        User newUser = new User();
        newUser.setEmail(newUserDTO.getEmail());
        newUser.setPassword(newUserDTO.getPassword()); // Cần mã hóa mật khẩu trước khi lưu vào DB!

        // Lưu người dùng mới vào database
        userRepository.save(newUser);
        authService.otpCache.remove(newUserDTO.getEmail());

        return ResponseEntity.ok("User registered successfully");
    }



    @PostMapping("/forgotPassword")
    public String ForgotPassword(@RequestParam String email, @RequestParam String pass, @RequestParam String otp) {
        if(!userRepository.findByEmail(email).isPresent()) {
            return "Email not found";
        }
        // Kiểm tra OTP người dùng nhập vào
        if (!authService.VerifyOtp(email, otp)) {
            return "Invalid OTP";  // Trả về thông báo nếu OTP không chính xác
        }
        User u = userRepository.findUserByEmail(email);
        u.setPassword(pass);
        userRepository.save(u);
        authService.otpCache.remove(email);
        return "User forgot password successfully";
    }
}
