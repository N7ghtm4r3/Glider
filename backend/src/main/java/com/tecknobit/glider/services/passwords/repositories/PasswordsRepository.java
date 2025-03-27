package com.tecknobit.glider.services.passwords.repositories;

import com.tecknobit.glider.services.passwords.entities.Password;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;

@Repository
public interface PasswordsRepository extends JpaRepository<Password, String> {

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

    @Query(
            value = "SELECT COUNT(*) FROM " + PASSWORDS_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY +
                    " AND " + TYPE_KEY + " IN (:" + TYPE_KEY + ")",
            nativeQuery = true
    )
    long countPasswords(
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(TYPE_KEY) Set<String> types
    );

    @Query(
            value = "SELECT * FROM " + PASSWORDS_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY +
                    " AND " + TYPE_KEY + " IN (:" + TYPE_KEY + ")",
            nativeQuery = true
    )
    List<Password> getPasswords(
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(TYPE_KEY) Set<String> types,
            Pageable pageable
    );

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
