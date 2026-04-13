package com.example.demo.controller;

import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "redirect:/login?mode=register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(username, email, password);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login?mode=register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        authService.processForgotPassword(email);
        redirectAttributes.addFlashAttribute("success",
                "Nếu tồn tại tài khoản với email này, một liên kết đặt lại mật khẩu sẽ được gửi đến email của bạn.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(required = false) String token, Model model,
                                        RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Mã đặt lại mật khẩu không hợp lệ.");
            return "redirect:/login";
        }

        try {
            authService.validateResetToken(token);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            authService.resetPassword(token, password, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được đặt lại.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
