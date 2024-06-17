package com.example.demo.controller;

import com.example.demo.Service.UserService;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Period;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;


@Controller
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/profile")
    public String getProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Formatear la fecha de nacimiento
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = user.getFechaNacimiento().format(formatter);
        model.addAttribute("user", user);
        model.addAttribute("formattedFechaNacimiento", formattedDate);

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User user, Model model) {
        userService.updateUserProfile(user);
        model.addAttribute("successMessage", "Perfil actualizado correctamente");

        // Actualizar el perfil en el modelo con los datos más recientes
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User updatedUser = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Formatear la fecha de nacimiento
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = updatedUser.getFechaNacimiento().format(formatter);
        model.addAttribute("user", updatedUser);
        model.addAttribute("formattedFechaNacimiento", formattedDate);

        return "profile";
    }

    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> payload) {
        Long id = Long.parseLong(payload.get("id"));
        String newPassword = payload.get("newPassword");
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PostMapping("/users/register")
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody User user, BindingResult result) {
        Map<String, String> response = new HashMap<>();
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> response.put(error.getField(), error.getDefaultMessage()));
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            response.put("email", "Ya existe un usuario registrado con este correo electrónico.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.findByDni(user.getDni()).isPresent()) {
            response.put("dni", "Ya existe un usuario registrado con este DNI.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        user.setRol("USER"); // Asignar rol "USER" por defecto
        userService.saveUser(user);
        response.put("message", "Usuario registrado exitosamente. Por favor, inicia sesión.");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/register_by_admin")
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerUserByAdmin(@Valid @RequestBody User user, BindingResult result) {
        Map<String, String> response = new HashMap<>();
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> response.put(error.getField(), error.getDefaultMessage()));
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            response.put("email", "Ya existe un usuario registrado con este correo electrónico.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.findByDni(user.getDni()).isPresent()) {
            response.put("dni", "Ya existe un usuario registrado con este DNI.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        userService.saveUser(user); // Asignar rol desde el frontend
        response.put("message", "Usuario registrado exitosamente por el administrador.");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/gestion_usuarios")
    public String gestionUsuarios() {
        System.out.println("Debug: Accediendo a /gestion_usuarios");
        return "gestion_usuarios";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public Page<User> getUsers(@RequestParam("size") int size,
                               @RequestParam("page") int page,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "dni", required = false) String dni,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "email", required = false) String email) {
        System.out.println("Debug: Obteniendo usuarios con tamaño: " + size + ", página: " + page);
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsersByFilters(status, dni, name, email, pageable);
    }

    @PostMapping("/api/users/{id}/deactivate")
    @ResponseBody
    public void deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
    }

    @PostMapping("/api/users/{id}/activate")
    @ResponseBody
    public void activateUser(@PathVariable Long id) {
        userService.activateUser(id);
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PostMapping("/api/users/{id}/reset-password")
    @ResponseBody
    public ResponseEntity<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newPassword = requestBody.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía");
        }
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok("Contraseña restablecida correctamente");
    }

    @GetMapping("/api/users/export")
    public void exportUsers(HttpServletResponse response,
                            @RequestParam("all") boolean all,
                            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "dni", required = false) String dni,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "email", required = false) String email) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=usuarios.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Usuarios");
        Row headerRow = sheet.createRow(0);

        String[] columns = {"ID", "Activo", "Nombre", "Apellido", "DNI", "Email", "Fecha de Nacimiento", "Rol"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        List<User> users;
        if (all) {
            users = userService.getAllUsers();
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userService.getUsersByFilters(status, dni, name, email, pageable);
            users = userPage.getContent();
        }

        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getActivo() ? "Activo" : "Desactivado");
            row.createCell(2).setCellValue(user.getNombre());
            row.createCell(3).setCellValue(user.getApellido());
            row.createCell(4).setCellValue(user.getDni());
            row.createCell(5).setCellValue(user.getEmail());
            row.createCell(6).setCellValue(user.getFechaNacimiento().toString());
            row.createCell(7).setCellValue(user.getRol());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
