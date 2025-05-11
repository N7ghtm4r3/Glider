package com.tecknobit.glider.services.passwords.entities;

import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.glidercore.ConstantsKt.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * The {@code PasswordConfiguration} class is used to represent the configuration used to generate a {@link Password}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = PASSWORD_CONFIGURATIONS_KEY)
public class PasswordConfiguration extends EquinoxItem {

    /**
     * {@code length} The length of the password
     */
    @Column
    private final int length;

    /**
     * {@code includeNumbers} whether the generated password must include the numbers
     */
    @Column(name = INCLUDE_NUMBERS_KEY)
    private final boolean includeNumbers;

    /**
     * {@code includeUppercaseLetters} whether the generated password must include the uppercase letters
     */
    @Column(name = INCLUDE_UPPERCASE_LETTERS_KEY)
    private final boolean includeUppercaseLetters;

    /**
     * {@code includeSpecialCharacters} whether the generated password must include the special characters
     */
    @Column(name = INCLUDE_SPECIAL_CHARACTERS_KEY)
    private final boolean includeSpecialCharacters;

    /**
     * {@code password} the password owner of the configuration
     */
    @OneToOne
    @OnDelete(action = CASCADE)
    private Password password;

    /**
     * Constructor to init the {@link PasswordConfiguration} class
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public PasswordConfiguration() {
        this(null, 0, false, false, false);
    }

    /**
     * Constructor to init the {@link PasswordConfiguration} class
     *
     * @param id                       The identifier of the password
     * @param length                   The length of the generated password
     * @param includeNumbers           Whether the generated password must include the numbers
     * @param includeUppercaseLetters  Whether the generated password must include the uppercase letters
     * @param includeSpecialCharacters Whether the generated password must include the special characters
     */
    public PasswordConfiguration(String id, int length, boolean includeNumbers, boolean includeUppercaseLetters,
                                 boolean includeSpecialCharacters) {
        super(id);
        this.length = length;
        this.includeNumbers = includeNumbers;
        this.includeUppercaseLetters = includeUppercaseLetters;
        this.includeSpecialCharacters = includeSpecialCharacters;
    }

    /**
     * Method used to get the {@link #length} instance
     *
     * @return the {@link #length} instance as {@code int}
     */
    public int getLength() {
        return length;
    }

    /**
     * Method used to get the {@link #includeNumbers} instance
     *
     * @return the {@link #includeNumbers} instance as {@code boolean}
     */
    public boolean includeNumbers() {
        return includeNumbers;
    }

    /**
     * Method used to get the {@link #includeUppercaseLetters} instance
     *
     * @return the {@link #includeUppercaseLetters} instance as {@code boolean}
     */
    public boolean includeUppercaseLetters() {
        return includeUppercaseLetters;
    }

    /**
     * Method used to get the {@link #includeSpecialCharacters} instance
     *
     * @return the {@link #includeSpecialCharacters} instance as {@code boolean}
     */
    public boolean includeSpecialCharacters() {
        return includeSpecialCharacters;
    }

    /**
     * Method used to get the {@link #password} instance
     *
     * @return the {@link #password} instance as {@link  Password}
     */
    public Password getPassword() {
        return password;
    }

    /**
     * Method used to set the {@link #password} instance
     *
     * @param password The password owner of the configuration
     */
    public void setPassword(Password password) {
        this.password = password;
    }

}
