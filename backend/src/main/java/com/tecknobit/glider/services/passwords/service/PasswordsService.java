package com.tecknobit.glider.services.passwords.service;

import com.tecknobit.glider.services.passwords.helpers.PasswordGenerator;
import com.tecknobit.glider.services.passwords.repository.PasswordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordsService {

    @Autowired
    private PasswordsRepository passwordsRepository;

    public void generatePassword(String userId, String token, int length, boolean includeNumbers,
                                 boolean includeUppercaseLetters, boolean includeSpecialCharacters) {
        PasswordGenerator generator = PasswordGenerator.getInstance();
        String password = generator.generatePassword(length, includeNumbers, includeUppercaseLetters,
                includeSpecialCharacters);
        System.out.println(password);
    }

}
