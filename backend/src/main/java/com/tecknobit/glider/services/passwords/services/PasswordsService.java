package com.tecknobit.glider.services.passwords.services;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.equinoxcore.annotations.Returner;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glider.services.passwords.entities.PasswordConfiguration;
import com.tecknobit.glider.services.passwords.helpers.PasswordGenerator;
import com.tecknobit.glider.services.passwords.repositories.PasswordsRepository;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glidercore.enums.PasswordType;
import kotlin.Pair;
import kotlin.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.enums.PasswordType.GENERATED;
import static com.tecknobit.glidercore.enums.PasswordType.INSERTED;

@Service
public class PasswordsService {

    @Autowired
    private PasswordsRepository passwordsRepository;

    @Autowired
    private PasswordEventsService eventsService;

    public void generatePassword(GliderUser user, String token, String tail, String scopes, int length,
                                 JsonHelper hResponse) throws Exception {
        boolean includeNumbers = hResponse.getBoolean(INCLUDE_NUMBERS_KEY, true);
        boolean includeUppercaseLetters = hResponse.getBoolean(INCLUDE_UPPERCASE_LETTERS_KEY, true);
        boolean includeSpecialCharacters = hResponse.getBoolean(INCLUDE_SPECIAL_CHARACTERS_KEY, true);
        PasswordGenerator generator = PasswordGenerator.getInstance();
        String password = generator.generatePassword(length, includeNumbers, includeUppercaseLetters,
                includeSpecialCharacters);
        PasswordConfiguration configuration = new PasswordConfiguration(
                generateIdentifier(),
                length,
                includeNumbers,
                includeUppercaseLetters,
                includeSpecialCharacters
        );
        long generationDate = System.currentTimeMillis();
        Password generatedPassword = loadPasswordEntity(token, tail, scopes, password, generationDate, GENERATED,
                configuration, user);
        passwordsRepository.save(generatedPassword);
        eventsService.registerGeneratedPasswordEvent(generatedPassword, generationDate);
    }

    public void insertPassword(GliderUser user, String token, String tail, String scopes, String password) throws Exception {
        long insertionDate = System.currentTimeMillis();
        Password insertedPassword = loadPasswordEntity(token, tail, scopes, password, insertionDate, INSERTED, null, user);
        passwordsRepository.save(insertedPassword);
        eventsService.registerInsertedPasswordEvent(insertedPassword, insertionDate);
    }

    @Returner
    private Password loadPasswordEntity(String token, String tail, String scopes, String password, long currentDate,
                                        PasswordType type, PasswordConfiguration configuration,
                                        GliderUser user) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Triple<String, String, String> passwordData = vault.encryptPasswordData(token, tail, password, scopes);
        Password passwordEntity = new Password(
                generateIdentifier(),
                currentDate,
                passwordData.getFirst(),
                passwordData.getSecond(),
                passwordData.getThird(),
                type,
                configuration,
                user
        );
        if (configuration != null)
            configuration.setPassword(passwordEntity);
        return passwordEntity;
    }

    @Wrapper
    public void editPassword(String token, String passwordId, String tail, String scopes, String password) throws Exception {
        Password storedPassword = findPasswordById(passwordId);
        if (storedPassword.getType() == GENERATED)
            editGeneratedPassword(token, passwordId, tail, scopes);
        else {
            if (!Companion.isPasswordValid(password))
                throw new IllegalStateException("Wrong password value");
            editInsertedPassword(token, passwordId, tail, scopes, password);
        }
        eventsService.registerEditPasswordEvent(storedPassword);
    }

    private void editGeneratedPassword(String token, String passwordId, String tail, String scopes) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Pair<String, String> encryptedData = vault.encryptPasswordData(token, tail, scopes);
        passwordsRepository.editGeneratedPassword(encryptedData.getFirst(), encryptedData.getSecond(), passwordId);
    }

    private void editInsertedPassword(String token, String passwordId, String tail, String scopes,
                                      String password) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Triple<String, String, String> encryptedData = vault.encryptPasswordData(token, tail, scopes, password);
        passwordsRepository.editInsertedPassword(encryptedData.getFirst(), encryptedData.getSecond(),
                encryptedData.getThird(), passwordId);
    }

    public Password findPasswordById(String passwordId) {
        return passwordsRepository.getReferenceById(passwordId);
    }

}
