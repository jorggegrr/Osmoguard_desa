package com.example.demo.Repository;

import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByDni(String dni);

    @Query("SELECT u FROM User u WHERE " +
            "(:status IS NULL OR u.activo = :status) AND " +
            "(:dni IS NULL OR u.dni LIKE %:dni%) AND " +
            "(:name IS NULL OR u.nombre LIKE %:name%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%)")
    Page<User> findByFilters(@Param("status") Boolean status,
                             @Param("dni") String dni,
                             @Param("name") String name,
                             @Param("email") String email,
                             Pageable pageable);
}
