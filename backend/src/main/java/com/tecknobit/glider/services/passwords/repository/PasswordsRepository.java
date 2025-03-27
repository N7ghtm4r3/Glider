package com.tecknobit.glider.services.passwords.repository;

import com.tecknobit.glider.services.passwords.entities.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordsRepository extends JpaRepository<Password, String> {
}
