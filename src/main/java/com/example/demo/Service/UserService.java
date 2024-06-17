package com.example.demo.Service;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        if (user.getRol() == null) {
            user.setRol("USER");
        }
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setEdad(Period.between(user.getFechaNacimiento(), LocalDate.now()).getYears());
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("El correo electrónico o DNI ya están registrados.");
        }
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByDni(String dni) {
        return userRepository.findByDni(dni);
    }

    public Page<User> getUsersByFilters(String status, String dni, String name, String email, Pageable pageable) {
        Boolean isActive = null;
        if ("active".equalsIgnoreCase(status)) {
            isActive = true;
        } else if ("inactive".equalsIgnoreCase(status)) {
            isActive = false;
        }

        return userRepository.findByFilters(isActive, dni, name, email, pageable);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActivo(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActivo(true);
        userRepository.save(user);
    }



    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setPwd(passwordEncoder.encode(newPassword)); // Codificar la nueva contraseña
        userRepository.save(user);
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public void deleteUserWithValidation(Long id, String email, String currentPassword) {
        User loggedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario logueado no encontrado"));

        if (!passwordEncoder.matches(currentPassword, loggedUser.getPwd())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        User userToDelete = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if ("ADMIN".equals(userToDelete.getRol())) {
            if (!email.equals(userToDelete.getEmail())) {
                throw new RuntimeException("No se puede eliminar un administrador sin validación");
            }
        }

        userRepository.deleteById(id);
    }

    // Método para obtener todos los usuarios
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserProfile(User user) {
        User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        existingUser.setNombre(user.getNombre());
        existingUser.setApellido(user.getApellido());
        existingUser.setDni(user.getDni());
        // El rol no debe ser actualizado
        userRepository.save(existingUser);
    }



}
