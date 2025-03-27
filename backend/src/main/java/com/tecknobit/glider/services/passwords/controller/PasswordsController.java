package com.tecknobit.glider.services.passwords.controller;

import com.tecknobit.glider.services.passwords.service.PasswordsService;
import com.tecknobit.glider.services.shared.controllers.DefaultGliderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.helpers.GliderInputsValidator.INSTANCE;

@RestController
@RequestMapping(path = BASE_EQUINOX_ENDPOINT + USERS_KEY + "/{" + IDENTIFIER_KEY + "}/" + PASSWORDS_KEY)
public class PasswordsController extends DefaultGliderController {

    /**
     * {@code WRONG_TAIL_MESSAGE} message to use when the tail of a password is wrong
     */
    public static final String WRONG_TAIL_MESSAGE = "wrong_tail";

    @Autowired
    private PasswordsService passwordsService;

    @PutMapping(
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String generatePassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @RequestBody Map<String, Object> payload
    ) {
        if (!validRequester(userId, token, deviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        loadJsonHelper(payload);
        String tail = jsonHelper.getString(TAIL_KEY);
        if (!INSTANCE.tailIsValid(tail))
            return failedResponse(WRONG_TAIL_MESSAGE);
        int length = jsonHelper.getInt(PASSWORD_LENGTH);
        if (!INSTANCE.passwordLengthValid(length))
            return failedResponse(WRONG_PASSWORD_MESSAGE);
        try {
            passwordsService.generatePassword(me, token, tail, length, jsonHelper);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

}
