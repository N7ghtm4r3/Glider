package com.tecknobit.glider.services.passwords.repositories;

import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glidercore.enums.PasswordType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.glidercore.ConstantsKt.*;

/**
 * The {@code PasswordsRepository} interface is useful to manage the queries for the passwords operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 * @see Password
 */
@Repository
public interface PasswordsRepository extends JpaRepository<Password, String> {

    /**
     * Query used to edit a {@link PasswordType#GENERATED} password
     *
     * @param tail       The tail of the password
     * @param scopes     The scopes of the password
     * @param passwordId The identifier of the password
     */
    @Modifying
    @Transactional
    @Query(
            value = "UPDATE " + PASSWORDS_KEY + " SET " +
                    TAIL_KEY + "=:" + TAIL_KEY + "," +
                    SCOPES_KEY + "=:" + SCOPES_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editGeneratedPassword(
            @Param(TAIL_KEY) String tail,
            @Param(SCOPES_KEY) String scopes,
            @Param(IDENTIFIER_KEY) String passwordId
    );

    /**
     * Query used to edit a {@link PasswordType#INSERTED} password
     *
     * @param tail The tail of the password
     * @param scopes The scopes of the password
     * @param password The password value
     * @param passwordId The identifier of the password
     */
    @Modifying
    @Transactional
    @Query(
            value = "UPDATE " + PASSWORDS_KEY + " SET " +
                    TAIL_KEY + "=:" + TAIL_KEY + "," +
                    SCOPES_KEY + "=:" + SCOPES_KEY + "," +
                    PASSWORD_KEY + "=:" + PASSWORD_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editInsertedPassword(
            @Param(TAIL_KEY) String tail,
            @Param(SCOPES_KEY) String scopes,
            @Param(PASSWORD_KEY) String password,
            @Param(IDENTIFIER_KEY) String passwordId
    );

    /**
     * Query used to retrieve the passwords of the user
     *
     * @param userId The identifier of the user
     * @param types The types of the passwords to include in the count
     *
     * @return the passwords of the user as {@link List} of {@link Password}
     */
    @Query(
            value = "SELECT * FROM " + PASSWORDS_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY +
                    " AND " + TYPE_KEY + " IN (:" + TYPE_KEY + ")" +
                    " ORDER BY " + CREATION_DATE_KEY + " DESC",
            nativeQuery = true
    )
    List<Password> getPasswords(
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(TYPE_KEY) Set<String> types
    );

    /**
     * Query used to refresh a {@link PasswordType#GENERATED} password
     *
     * @param password The value of the refreshed password
     * @param passwordId The identifier of the password
     */
    @Modifying
    @Transactional
    @Query(
            value = "UPDATE " + PASSWORDS_KEY + " SET " +
                    PASSWORD_KEY + "=:" + PASSWORD_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void refreshPassword(
            @Param(PASSWORD_KEY) String password,
            @Param(IDENTIFIER_KEY) String passwordId
    );

    /**
     * Query used to delete a password
     *
     * @param passwordId The identifier of the password
     */
    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM " + PASSWORDS_KEY + _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void deletePassword(
            @Param(IDENTIFIER_KEY) String passwordId
    );

}
