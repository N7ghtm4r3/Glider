package com.tecknobit.glider.services.passwords.services;

import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glider.services.passwords.entities.PasswordEvent;
import com.tecknobit.glider.services.passwords.repositories.PasswordEventsRepository;
import com.tecknobit.glidercore.enums.PasswordEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.glidercore.enums.PasswordEventType.*;

/**
 * The {@code PasswordEventsService} class is useful to manage all the password events database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class PasswordEventsService {

    /**
     * {@code eventsRepository} instance used to manage the database operations for the events
     */
    @Autowired
    private PasswordEventsRepository eventsRepository;

    /**
     * Method used to register the {@link PasswordEventType#GENERATED} event
     *
     * @param password The password owner of the event
     */
    @Wrapper
    public void registerGeneratedPasswordEvent(Password password, long generationDate) {
        registerPasswordEvent(password, generationDate, GENERATED);
    }

    /**
     * Method used to register the {@link PasswordEventType#INSERTED} event
     *
     * @param password The password owner of the event
     */
    @Wrapper
    public void registerInsertedPasswordEvent(Password password, long insertionDate) {
        registerPasswordEvent(password, insertionDate, INSERTED);
    }

    /**
     * Method used to register the {@link PasswordEventType#EDITED} event
     *
     * @param password The password owner of the event
     */
    @Wrapper
    public void registerEditPasswordEvent(Password password) {
        registerPasswordEvent(password, EDITED);
    }

    /**
     * Method used to register the {@link PasswordEventType#COPIED} event
     *
     * @param password The password owner of the event
     */
    @Wrapper
    public void registerCopiedPasswordEvent(Password password) {
        registerPasswordEvent(password, COPIED);
    }

    /**
     * Method used to register the {@link PasswordEventType#REFRESHED} event
     *
     * @param password The password owner of the event
     */
    @Wrapper
    public void registerRefreshedPasswordEvent(Password password) {
        registerPasswordEvent(password, REFRESHED);
    }

    /**
     * Method used to register an event of a password
     *
     * @param password The password owner of the event
     * @param type     The type of the event to register
     */
    @Wrapper
    private void registerPasswordEvent(Password password, PasswordEventType type) {
        registerPasswordEvent(password, System.currentTimeMillis(), type);
    }

    /**
     * Method used to register an event of a password
     *
     * @param password The password owner of the event
     * @param eventDate The date of the event
     * @param type The type of the event to register
     */
    private void registerPasswordEvent(Password password, long eventDate, PasswordEventType type) {
        eventsRepository.save(new PasswordEvent(generateIdentifier(), eventDate, type, password));
    }

}
