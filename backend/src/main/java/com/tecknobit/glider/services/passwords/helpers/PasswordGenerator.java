package com.tecknobit.glider.services.passwords.helpers;

import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.glider.services.passwords.entities.PasswordConfiguration;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * The {@code PasswordGenerator} class is used to generate the {@link com.tecknobit.glider.services.passwords.entities.Password}
 * for the user who request the generation
 *
 * @author N7ghtm4r3 - Tecknobit
 */
public class PasswordGenerator {

    /**
     * {@code generator} the singleton instance of the generator
     */
    private static final PasswordGenerator generator = new PasswordGenerator();

    /**
     * {@code CHARACTERS} the available characters to generate a password
     */
    private static final char[] CHARACTERS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z',
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}', '|', ';', ':', '\'',
            '\"', ',', '.', '<', '>', '?', '/', '\\', '`', '~'
    };

    /**
     * {@code DIGITS} the digits characters available to generate a password
     */
    private static final HashSet<Character> DIGITS = new HashSet<>(
            List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    );

    /**
     * {@code UPPERCASE_LETTERS} the uppercase letters characters available to generate a password
     */
    private static final HashSet<Character> UPPERCASE_LETTERS = new HashSet<>(
            List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    );

    /**
     * {@code SPECIAL_CHARACTERS} the special characters available to generate a password
     */
    private static final HashSet<Character> SPECIAL_CHARACTERS = new HashSet<>(
            List.of('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}',
                    '|', ';', ':', '\'', '\"', ',', '.', '<', '>', '?', '/', '\\', '`', '~')
    );

    /**
     * {@code BASE_OFFSET} the base offset from the {@link #CHARACTERS} lowercase letters
     */
    private static final int BASE_OFFSET = 26;

    /**
     * Constructor to instantiate the object
     */
    private PasswordGenerator() {
    }

    /**
     * Method used to generate a password
     *
     * @param configuration The configuration to use to generate the password
     * @return the generated password as {@link String}
     */
    @Wrapper
    public String generatePassword(PasswordConfiguration configuration) {
        return generatePassword(configuration.getLength(), configuration.includeNumbers(),
                configuration.includeUppercaseLetters(), configuration.includeSpecialCharacters());
    }

    /**
     *
     * Method used to generate a password
     *
     * @param length The length of the generated password
     * @param includeNumbers Whether the generated password must include the numbers
     * @param includeUppercaseLetters Whether the generated password must include the uppercase letters
     * @param includeSpecialCharacters Whether the generated password must include the special characters
     *
     * @return the generated password as {@link String}
     */
    public String generatePassword(int length, boolean includeNumbers, boolean includeUppercaseLetters,
                                   boolean includeSpecialCharacters) {
        ArrayList<Character> password = new ArrayList<>();
        SecureRandom secureRandom = new SecureRandom();
        guaranteeConstraintCharacters(password, secureRandom, includeNumbers, includeUppercaseLetters,
                includeSpecialCharacters);
        for (int j = password.size(); j < length; j++) {
            char character;
            do {
                int characterIndex = secureRandom.nextInt(CHARACTERS.length);
                character = CHARACTERS[characterIndex];
            } while (!constraintsValid(character, includeNumbers, includeUppercaseLetters, includeSpecialCharacters));
            password.add(character);
        }
        Collections.shuffle(password);
        return convertToString(password);
    }

    /**
     * Method used to guarantee the password has the requested constraint characters
     * @param password The current password's characters
     * @param secureRandom The secure random instance used to generate the characters made up the password
     * @param includeNumbers Whether the generated password must include the numbers
     * @param includeUppercaseLetters Whether the generated password must include the uppercase letters
     * @param includeSpecialCharacters Whether the generated password must include the special characters
     */
    private void guaranteeConstraintCharacters(ArrayList<Character> password, SecureRandom secureRandom,
                                               boolean includeNumbers, boolean includeUppercaseLetters,
                                               boolean includeSpecialCharacters) {
        if (includeNumbers)
            password.add(CHARACTERS[getDigitsValidIndex(secureRandom)]);
        if (includeUppercaseLetters)
            password.add(CHARACTERS[getUppercaseLettersValidIndex(secureRandom)]);
        if (includeSpecialCharacters)
            password.add(CHARACTERS[getSpecialCharactersValidIndex(secureRandom)]);
    }

    /**
     * Method used to get a valid index from the {@link #DIGITS} set
     *
     * @param secureRandom The secure random instance used to generate the characters made up the password
     * @return a valid index as {@code int}
     */
    private int getDigitsValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + secureRandom.nextInt(DIGITS.size());
    }

    /**
     * Method used to get a valid index from the {@link #UPPERCASE_LETTERS} set
     *
     * @param secureRandom The secure random instance used to generate the characters made up the password
     * @return a valid index as {@code int}
     */
    private int getUppercaseLettersValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + DIGITS.size() + secureRandom.nextInt(UPPERCASE_LETTERS.size());
    }

    /**
     * Method used to get a valid index from the {@link #SPECIAL_CHARACTERS} set
     *
     * @param secureRandom The secure random instance used to generate the characters made up the password
     * @return a valid index as {@code int}
     */
    private int getSpecialCharactersValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + DIGITS.size() + UPPERCASE_LETTERS.size() + secureRandom.nextInt(SPECIAL_CHARACTERS.size());
    }

    /**
     * Method used to check whether the generated character respect the constraints requested by the user
     *
     * @param character The generated character to check
     * @param includeNumbers Whether the generated password must include the numbers
     * @param includeUppercaseLetters Whether the generated password must include the uppercase letters
     * @param includeSpecialCharacters Whether the generated password must include the special characters
     *
     * @return whether the generated character respect the constraints requested by the user as {@code boolean}
     */
    private boolean constraintsValid(char character, boolean includeNumbers, boolean includeUppercaseLetters,
                                     boolean includeSpecialCharacters) {
        if (DIGITS.contains(character) && !includeNumbers)
            return false;
        if (UPPERCASE_LETTERS.contains(character) && !includeUppercaseLetters)
            return false;
        return !SPECIAL_CHARACTERS.contains(character) || includeSpecialCharacters;
    }

    /**
     * Method used to convert the list of the generated password characters as concatenated string
     *
     * @param rawPassword The raw password characters
     *
     * @return the password converted as {@link String}
     */
    private String convertToString(ArrayList<Character> rawPassword) {
        StringBuilder converter = new StringBuilder();
        for (Character character : rawPassword)
            converter.append(character);
        return converter.toString();
    }

    /**
     * Method used to obtain the instance of the generator
     *
     * @return the generator instance as {@link PasswordGenerator}
     */
    public static PasswordGenerator getInstance() {
        return generator;
    }

}
