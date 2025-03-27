package com.tecknobit.glider.services.passwords.controller;

import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.passwords.services.PasswordsService;
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

    /**
     * {@code WRONG_SCOPES_MESSAGE} message to use when the scopes of a password are wrong
     */
    public static final String WRONG_SCOPES_MESSAGE = "wrong_scopes";

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
        String scopes = jsonHelper.getString(SCOPES_KEY);
        if (!INSTANCE.scopesAreValid(scopes))
            return failedResponse(WRONG_SCOPES_MESSAGE);
        int length = jsonHelper.getInt(PASSWORD_LENGTH);
        if (!INSTANCE.passwordLengthValid(length))
            return failedResponse(WRONG_PASSWORD_MESSAGE);
        try {
            passwordsService.generatePassword(me, token, tail, scopes, length, jsonHelper);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @PatchMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String editGeneratedPassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @PathVariable(PASSWORD_IDENTIFIER_KEY) String passwordId,
            @RequestBody Map<String, Object> payload
    ) {
        if (!validPasswordRequest(userId, token, deviceId, passwordId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        loadJsonHelper(payload);
        String tail = jsonHelper.getString(TAIL_KEY);
        if (!INSTANCE.tailIsValid(tail))
            return failedResponse(WRONG_TAIL_MESSAGE);
        String scopes = jsonHelper.getString(SCOPES_KEY);
        if (!INSTANCE.scopesAreValid(scopes))
            return failedResponse(WRONG_SCOPES_MESSAGE);
        try {
            passwordsService.editGeneratedPassword(passwordId, token, tail, scopes);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @Validator
    private boolean validPasswordRequest(String userId, String token, String deviceId, String passwordId) {
        boolean validRequester = validRequester(userId, token, deviceId);
        if (!validRequester)
            return false;
        return me.passwordBelongsToMe(passwordId);
    }

}
