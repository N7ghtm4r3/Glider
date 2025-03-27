package com.tecknobit.glider.services.passwords.repositories;

import com.tecknobit.glider.services.passwords.entities.PasswordEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordEventsRepository extends JpaRepository<PasswordEvent, String> {


}
