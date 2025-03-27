package com.tecknobit.glider.services.passwords.repositories;

import com.tecknobit.glider.services.passwords.entities.Password;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;

@Repository
public interface PasswordsRepository extends JpaRepository<Password, String> {

    @Modifying(clearAutomatically = true)
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

}
