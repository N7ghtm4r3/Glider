package com.tecknobit.glider.services.passwords.service;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glider.services.passwords.entities.PasswordConfiguration;
import com.tecknobit.glider.services.passwords.helpers.PasswordGenerator;
import com.tecknobit.glider.services.passwords.repository.PasswordsRepository;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glidercore.enums.PasswordType;
import kotlin.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.glidercore.ConstantsKt.*;

@Service
public class PasswordsService {

    @Autowired
    private PasswordsRepository passwordsRepository;

    public void generatePassword(GliderUser user, String token, String tail, int length,
                                 JsonHelper hResponse) throws Exception {
        boolean includeNumbers = hResponse.getBoolean(INCLUDE_NUMBERS_KEY, true);
        boolean includeUppercaseLetters = hResponse.getBoolean(INCLUDE_UPPERCASE_LETTERS_KEY, true);
        boolean includeSpecialCharacters = hResponse.getBoolean(INCLUDE_SPECIAL_CHARACTERS_KEY, true);
        PasswordGenerator generator = PasswordGenerator.getInstance();
        String password = generator.generatePassword(length, includeNumbers, includeUppercaseLetters,
                includeSpecialCharacters);
        String scopes = hResponse.getString(SCOPES_KEY);
        Triple<String, String, String> passwordData = cypherPasswordData(token, tail, password, scopes);
        PasswordConfiguration configuration = new PasswordConfiguration(
                generateIdentifier(),
                length,
                includeNumbers,
                includeUppercaseLetters,
                includeSpecialCharacters
        );
        Password generatedPassword = new Password(
                generateIdentifier(),
                System.currentTimeMillis(),
                passwordData.getFirst(),
                passwordData.getSecond(),
                passwordData.getThird(),
                PasswordType.GENERATED,
                configuration,
                user
        );
        configuration.setPassword(generatedPassword);
        passwordsRepository.save(generatedPassword);
    }

    private Triple<String, String, String> cypherPasswordData(String token, String tail, String password,
                                                              String scopes) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        return vault.encryptPasswordData(token, tail, password, scopes);
    }

}
