package com.tecknobit.glider.services.passwords.entities;

import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.glidercore.ConstantsKt.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = PASSWORD_CONFIGURATIONS_KEY)
public class PasswordConfiguration extends EquinoxItem {

    @Column
    private final int length;

    @Column(name = INCLUDE_NUMBERS_KEY)
    private final boolean includeNumbers;

    @Column(name = INCLUDE_UPPERCASE_LETTERS_KEY)
    private final boolean includeUppercaseLetters;

    @Column(name = INCLUDE_SPECIAL_CHARACTERS_KEY)
    private final boolean includeSpecialCharacters;

    @OneToOne
    @OnDelete(action = CASCADE)
    private Password password;

    public PasswordConfiguration() {
        this(null, 0, false, false, false);
    }

    public PasswordConfiguration(String id, int length, boolean includeNumbers, boolean includeUppercaseLetters,
                                 boolean includeSpecialCharacters) {
        super(id);
        this.length = length;
        this.includeNumbers = includeNumbers;
        this.includeUppercaseLetters = includeUppercaseLetters;
        this.includeSpecialCharacters = includeSpecialCharacters;
    }

    public int getLength() {
        return length;
    }

    public boolean includeNumbers() {
        return includeNumbers;
    }

    public boolean includeUppercaseLetters() {
        return includeUppercaseLetters;
    }

    public boolean includeSpecialCharacters() {
        return includeSpecialCharacters;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

}
