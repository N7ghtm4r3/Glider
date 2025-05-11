package com.tecknobit.glider.services.passwords.services;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.equinoxcore.annotations.Returner;
import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.passwords.dtos.PasswordMask;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.DEFAULT_PAGE;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.DEFAULT_PAGE_SIZE;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.enums.PasswordType.GENERATED;
import static com.tecknobit.glidercore.enums.PasswordType.INSERTED;

/**
 * The {@code PasswordsService} class is useful to manage all the password database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class PasswordsService {

    /**
     * {@code passwordsRepository} instance used to manage the database operations for the passwords
     */
    @Autowired
    private PasswordsRepository passwordsRepository;

    /**
     * {@code eventsService} instance used to manage the events related to a password
     */
    @Autowired
    private PasswordEventsService eventsService;

    /**
     * Method used to generate a password
     *
     * @param user      The user owner of the generated password
     * @param token     The token of the user
     * @param tail      The tail of the password
     * @param scopes    The scopes of the password
     * @param length    The length of the password
     * @param hResponse The payload of the request
     * @return the generated password as {@link String}
     * @throws Exception when an error occurred during the password generation
     */
    public String generatePassword(GliderUser user, String token, String tail, String scopes, int length,
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
        return password;
    }

    /**
     * Method used to insert a password
     *
     * @param user The user owner of the inserted password
     * @param token The token of the user
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     * @param password The value of the password
     *
     * @throws Exception when an error occurred during the password insertion
     */
    public void insertPassword(GliderUser user, String token, String tail, String scopes, String password) throws Exception {
        long insertionDate = System.currentTimeMillis();
        Password insertedPassword = loadPasswordEntity(token, tail, scopes, password, insertionDate, INSERTED, null, user);
        passwordsRepository.save(insertedPassword);
        eventsService.registerInsertedPasswordEvent(insertedPassword, insertionDate);
    }

    /**
     * Method to load a {@link Password} entity with the password data
     *
     * @param token The token of the user
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     * @param password The value of the password
     * @param currentDate The date when the password has been generated or inserted
     * @param type The type of the password
     * @param configuration The configuration of the password if is a {@link PasswordType#GENERATED} password
     * @param user The user owner of the password
     *
     * @return the password entity as {@link Password}
     *
     * @throws Exception when an error occurred during the password loading
     */
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

    /**
     * Method used to edit a password
     *
     * @param token The token of the user
     * @param passwordId The identifier of the password
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     * @param password The value of the password
     *
     * @throws Exception when an error occurred during the password editing
     */
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

    /**
     * Method used to edit a {@link PasswordType#GENERATED} password
     *
     * @param token The token of the user
     * @param passwordId The identifier of the password
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     *
     * @throws Exception when an error occurred during the password editing
     */
    private void editGeneratedPassword(String token, String passwordId, String tail, String scopes) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Pair<String, String> encryptedData = vault.encryptPasswordData(token, tail, scopes);
        passwordsRepository.editGeneratedPassword(encryptedData.getFirst(), encryptedData.getSecond(), passwordId);
    }

    /**
     * Method used to edit a {@link PasswordType#INSERTED} password
     *
     * @param token The token of the user
     * @param passwordId The identifier of the password
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     * @param password The value of the password
     *
     * @throws Exception when an error occurred during the password editing
     */
    private void editInsertedPassword(String token, String passwordId, String tail, String scopes,
                                      String password) throws Exception {
        ServerVault vault = ServerVault.getInstance();
        Triple<String, String, String> encryptedData = vault.encryptPasswordData(token, tail, scopes, password);
        passwordsRepository.editInsertedPassword(encryptedData.getFirst(), encryptedData.getSecond(),
                encryptedData.getThird(), passwordId);
    }

    /**
     * Method used to get a password details
     *
     * @param token The token of the user
     * @param passwordId The identifier of the password
     *
     * @return the password details as {@link PasswordMask}
     *
     * @throws Exception when an error occurred during the password retrieving
     */
    public PasswordMask getPassword(String token, String passwordId) throws Exception {
        Password password = findPasswordById(passwordId);
        ServerVault vault = ServerVault.getInstance();
        vault.decryptPassword(token, password);
        return new PasswordMask(password);
    }

    /**
     * Method used to get the keychain of the user
     *
     * @param userId The identifier of the user
     * @param token The token of the user
     * @param page      The page requested
     * @param pageSize  The size of the items to insert in the page
     * @param keywords The filter keywords
     * @param types The types of the passwords to retrieve
     *
     * @return the password owned by the user as {@link PaginatedResponse} of {@link Password}
     */
    public PaginatedResponse<Password> getKeychain(String userId, String token, int page, int pageSize,
                                                   Set<String> keywords, Set<String> types) throws Exception {
        List<Password> passwords = passwordsRepository.getPasswords(userId, types);
        ServerVault vault = ServerVault.getInstance();
        vault.decryptPasswords(token, passwords);
        passwords = filterPasswords(passwords, keywords);
        long totalPasswords = passwords.size();
        if (page < 0)
            page = DEFAULT_PAGE;
        if (pageSize < 0)
            pageSize = DEFAULT_PAGE_SIZE;
        passwords = pagePasswords(passwords, page, pageSize);
        return new PaginatedResponse<>(passwords, page, pageSize, totalPasswords);
    }

    /**
     * Method used to apply the keywords filter to the result list
     *
     * @param passwords The passwords list to filter
     * @param keywords The filter keywords to apply
     * @return the passwords list filtered as {@link List} of {@link Password}
     */
    private List<Password> filterPasswords(List<Password> passwords, Set<String> keywords) {
        List<Password> filteredPasswords = new ArrayList<>();
        for (Password password : passwords) {
            String tail = password.getTail().toLowerCase();
            if (keywords.isEmpty() || (tailMatches(keywords, tail) || password.scopesMatch(keywords)))
                filteredPasswords.add(password);
        }
        return filteredPasswords;
    }

    /**
     * Method used to paginate the passwords list
     *
     * @param page     The page requested
     * @param pageSize The size of the items to insert in the page
     * @return the passwords list paged as {@link List} of {@link Password}
     */
    private List<Password> pagePasswords(List<Password> passwords, int page, int pageSize) {
        int passwordsSize = passwords.size();
        if (passwordsSize == 0)
            return Collections.EMPTY_LIST;
        int from = page * pageSize;
        int to = from + pageSize;
        if (from > passwordsSize)
            from = passwordsSize;
        if (to > passwordsSize)
            to = passwordsSize;
        return passwords.subList(from, to);
    }

    /**
     * Method used to check whether the tail of the password matches with any keywords
     *
     * @param keywords The filter keywords
     * @param tail     The tail of the password
     * @return whether the tail of the password matches with any keywords as {@code boolean}
     */
    @Validator
    private boolean tailMatches(Set<String> keywords, String tail) {
        return !keywords.stream().filter(keyword -> tail.contains(keyword.toLowerCase())).toList().isEmpty();
    }

    /**
     * Method used to notify the {@link com.tecknobit.glidercore.enums.PasswordEventType#COPIED} event
     *
     * @param passwordId The identifier of the copied password
     */
    public void notifyCopiedPassword(String passwordId) {
        Password password = findPasswordById(passwordId);
        eventsService.registerCopiedPasswordEvent(password);
    }

    /**
     * Method used to refresh a {@link PasswordType#GENERATED} password
     * @param token The token of the user
     * @param passwordId The identifier of the password
     * @return the value of the refreshed password as {@link String}
     *
     * @throws Exception when an error occurred during the password refreshing
     */
    public String refreshGeneratedPassword(String token, String passwordId) throws Exception {
        Password password = findPasswordById(passwordId);
        if (password.getType() == INSERTED)
            throw new IllegalStateException("Wrong password type");
        PasswordGenerator generator = PasswordGenerator.getInstance();
        String refreshedPassword = generator.generatePassword(password.getConfiguration());
        ServerVault vault = ServerVault.getInstance();
        String encryptedPassword = vault.encryptPassword(token, refreshedPassword);
        passwordsRepository.refreshPassword(encryptedPassword, passwordId);
        eventsService.registerRefreshedPasswordEvent(password);
        return refreshedPassword;
    }

    /**
     * Method used to find a password by its identifier
     *
     * @param passwordId The identifier of the password
     *
     * @return the password as {@link Password}
     */
    private Password findPasswordById(String passwordId) {
        return passwordsRepository.getReferenceById(passwordId);
    }

    /**
     * Method used to delete a password
     *
     * @param passwordId The identifier of the password
     */
    public void deletePassword(String passwordId) {
        passwordsRepository.deletePassword(passwordId);
    }

}
