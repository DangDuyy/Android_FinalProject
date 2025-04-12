package fit24.duy.musicplayer.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.repository.UserRepository;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    private JavaMailSender mailSender;

    public Map<String, String> otpCache = new HashMap<>();

    public void SaveOtp(String email, String otp) {
        otpCache.put(email, otp);
    }

    public boolean VerifyOtp(String email, String otp) {
        String storedOtp = otpCache.get(email);
        return storedOtp != null && storedOtp.equals(otp);
    }

    public String Login(String username, String password){
        if(userRepository.findByEmailAndPassword(username, password).isPresent()){
            return username;
        }
        return "Invalid username or password";
    }

    public List<User> users (){
        return userRepository.findAll();
    }

    private String GenerateOtp(){
        int otp =100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    // Gửi OTP qua email
    public String SendOtp(String email) {
        // Kiểm tra định dạng và miền email hợp lệ
        if (!isValidEmailFormat(email) || !isEmailDomainValid(email)) {
            return "Invalid or non-existent email";  // Chặn email không hợp lệ hoặc không tồn tại
        }
        String otp = GenerateOtp();

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            messageHelper.setFrom(senderEmail, "07.Nguyễn Duy Đạt");  // Thêm tên người gửi
            messageHelper.setTo(email);
            messageHelper.setSubject("OTP Verification");
            messageHelper.setText("Your OTP code is: " + otp);
            SaveOtp(email,otp);
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
            return "Error sending OTP";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return otp;
    }
    private boolean isValidEmailFormat(String email) {
        String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(EMAIL_REGEX);
    }

    // Helper method to check if email domain exists (basic DNS lookup)
    private boolean isEmailDomainValid(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        try {
            DirContext ctx = new InitialDirContext();
            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (NamingException e) {
            return false;  // Nếu không tìm thấy bản ghi MX, domain không hợp lệ
        }
    }

}
