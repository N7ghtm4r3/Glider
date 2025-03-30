package com.tecknobit.glider.services.passwords.repositories;

import com.tecknobit.glider.services.passwords.entities.PasswordEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The {@code PasswordEventsRepository} interface is useful to manage the queries for the events operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 * @see PasswordEvent
 */
@Repository
public interface PasswordEventsRepository extends JpaRepository<PasswordEvent, String> {
}
