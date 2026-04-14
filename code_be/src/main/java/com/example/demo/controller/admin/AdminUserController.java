package com.example.demo.controller.admin;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")

public class AdminUserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/user-management";
    }

    @PostMapping("/{userId}/toggle-mod")
    public String toggleModRole(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role modRole = roleRepository.findByName("ROLE_MOD")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_MOD")));

        if (user.getRoles().contains(modRole)) {
            user.getRoles().remove(modRole);
            redirectAttributes.addFlashAttribute("success", "Đã thu hồi quyền MOD của " + user.getUsername());
        } else {
            user.getRoles().add(modRole);
            redirectAttributes.addFlashAttribute("success", "Đã cấp quyền MOD cho " + user.getUsername());
        }
        userRepository.save(user);

        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/toggle-uploader")
    public String toggleUploaderRole(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role uploaderRole = roleRepository.findByName("ROLE_UPLOADER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_UPLOADER")));

        if (user.getRoles().contains(uploaderRole)) {
            user.getRoles().remove(uploaderRole);
            redirectAttributes.addFlashAttribute("success", "Đã thu hồi quyền Uploader của " + user.getUsername());
        } else {
            user.getRoles().add(uploaderRole);
            redirectAttributes.addFlashAttribute("success", "Đã cấp quyền Uploader cho " + user.getUsername());
        }
        userRepository.save(user);

        return "redirect:/admin/users";
    }
}