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

@Service
public class PasswordEventsService {

    @Autowired
    private PasswordEventsRepository eventsRepository;

    @Wrapper
    public void registerGeneratedPasswordEvent(Password password, long generationDate) {
        registerPasswordEvent(password, generationDate, GENERATED);
    }

    @Wrapper
    public void registerInsertedPasswordEvent(Password password, long insertionDate) {
        registerPasswordEvent(password, insertionDate, INSERTED);
    }

    @Wrapper
    public void registerEditPasswordEvent(Password password) {
        registerPasswordEvent(password, EDITED);
    }

    @Wrapper
    public void registerCopiedPasswordEvent(Password password) {
        registerPasswordEvent(password, COPIED);
    }

    @Wrapper
    private void registerPasswordEvent(Password password, PasswordEventType type) {
        registerPasswordEvent(password, System.currentTimeMillis(), type);
    }

    private void registerPasswordEvent(Password password, long eventDate, PasswordEventType type) {
        eventsRepository.save(new PasswordEvent(generateIdentifier(), eventDate, type, password));
    }

}
