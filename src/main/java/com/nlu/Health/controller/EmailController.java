package com.nlu.Health.controller;

import com.nlu.Health.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendMail")
    public String sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        try {
            emailService.sendEmail(to, subject, body);
            return "Email đã gửi thành công!";
        } catch (MessagingException e) {
            return "Lỗi khi gửi email: " + e.getMessage();
        }
    }
}
