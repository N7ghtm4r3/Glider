package com.tecknobit.glider.services.passwords.services;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glider.services.passwords.entities.PasswordConfiguration;
import com.tecknobit.glider.services.passwords.helpers.PasswordGenerator;
import com.tecknobit.glider.services.passwords.repositories.PasswordsRepository;
import com.tecknobit.glider.services.users.entities.GliderUser;
import kotlin.Pair;
import kotlin.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.enums.PasswordType.GENERATED;

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
        Triple<String, String, String> passwordData = cypherPasswordData(token, tail, password, scopes);
        PasswordConfiguration configuration = new PasswordConfiguration(
                generateIdentifier(),
                length,
                includeNumbers,
                includeUppercaseLetters,
                includeSpecialCharacters
        );
        long generationDate = System.currentTimeMillis();
        Password generatedPassword = new Password(
                generateIdentifier(),
                generationDate,
                passwordData.getFirst(),
                passwordData.getSecond(),
                passwordData.getThird(),
                GENERATED,
                configuration,
                user
        );
        configuration.setPassword(generatedPassword);
        passwordsRepository.save(generatedPassword);
        eventsService.registerGeneratedPasswordEvent(generatedPassword, generationDate);
    }

    public void editGeneratedPassword(String passwordId, String token, String tail, String scopes) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Pair<String, String> encryptedData = vault.encryptPasswordData(token, tail, scopes);
        passwordsRepository.editGeneratedPassword(encryptedData.getFirst(), encryptedData.getSecond(), passwordId);
        Password password = findPasswordById(passwordId);
        eventsService.registerEditGeneratedPasswordEvent(password);
    }

    private Triple<String, String, String> cypherPasswordData(String token, String tail, String password,
                                                              String scopes) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        return vault.encryptPasswordData(token, tail, password, scopes);
    }

    public Password findPasswordById(String passwordId) {
        return passwordsRepository.getReferenceById(passwordId);
    }

}
