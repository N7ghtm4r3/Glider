package com.tecknobit.glider.services.passwords.helpers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class PasswordGenerator {

    private static final PasswordGenerator generator = new PasswordGenerator();

    private static final char[] CHARACTERS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z',
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}', '|', ';', ':', '\'',
            '\"', ',', '.', '<', '>', '?', '/', '\\', '`', '~'
    };

    private static final HashSet<Character> DIGITS = new HashSet<>(
            List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    );

    private static final HashSet<Character> UPPERCASE_LETTERS = new HashSet<>(
            List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    );

    private static final HashSet<Character> SPECIAL_CHARACTERS = new HashSet<>(
            List.of('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}',
                    '|', ';', ':', '\'', '\"', ',', '.', '<', '>', '?', '/', '\\', '`', '~')
    );

    private static final int BASE_OFFSET = 26;

    private PasswordGenerator() {
    }

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

    private int getDigitsValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + secureRandom.nextInt(DIGITS.size());
    }

    private int getUppercaseLettersValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + DIGITS.size() + secureRandom.nextInt(UPPERCASE_LETTERS.size());
    }

    private int getSpecialCharactersValidIndex(SecureRandom secureRandom) {
        return BASE_OFFSET + DIGITS.size() + UPPERCASE_LETTERS.size() + secureRandom.nextInt(SPECIAL_CHARACTERS.size());
    }

    private boolean constraintsValid(char character, boolean includeNumbers, boolean includeUppercaseLetters,
                                     boolean includeSpecialCharacters) {
        if (DIGITS.contains(character) && !includeNumbers)
            return false;
        if (UPPERCASE_LETTERS.contains(character) && !includeUppercaseLetters)
            return false;
        return !SPECIAL_CHARACTERS.contains(character) || includeSpecialCharacters;
    }

    private String convertToString(ArrayList<Character> rawPassword) {
        StringBuilder converter = new StringBuilder();
        for (Character character : rawPassword)
            converter.append(character);
        return converter.toString();
    }

    public static PasswordGenerator getInstance() {
        return generator;
    }

}
