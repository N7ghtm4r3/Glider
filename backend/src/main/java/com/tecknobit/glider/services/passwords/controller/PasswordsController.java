package com.tecknobit.glider.services.passwords.controller;

import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.passwords.services.PasswordsService;
import com.tecknobit.glider.services.shared.controllers.DefaultGliderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.helpers.GliderEndpointsSet.KEYCHAIN_ENDPOINT;
import static com.tecknobit.glidercore.helpers.GliderEndpointsSet.REFRESH_ENDPOINT;
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
        int length = jsonHelper.getInt(PASSWORD_LENGTH_KEY);
        if (!INSTANCE.passwordLengthValid(length))
            return failedResponse(WRONG_PASSWORD_MESSAGE);
        try {
            return successResponse(passwordsService.generatePassword(me, token, tail, scopes, length, jsonHelper));
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @PostMapping(
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String insertPassword(
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
        String password = jsonHelper.getString(PASSWORD_KEY);
        if (!Companion.isPasswordValid(password))
            return failedResponse(WRONG_PASSWORD_MESSAGE);
        try {
            passwordsService.insertPassword(me, token, tail, scopes, password);
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
    public String editPassword(
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
        String password = jsonHelper.getString(PASSWORD_KEY);
        try {
            passwordsService.editPassword(token, passwordId, tail, scopes, password);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @GetMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public <T> T getPassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @PathVariable(PASSWORD_IDENTIFIER_KEY) String passwordId
    ) {
        if (!validPasswordRequest(userId, token, deviceId, passwordId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            return (T) successResponse(passwordsService.getPassword(token, passwordId));
        } catch (Exception e) {
            return (T) failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @GetMapping(
            path = "/" + KEYCHAIN_ENDPOINT,
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public <T> T getKeychain(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(name = TYPE_KEY, defaultValue = "GENERATED, INSERTED", required = false) Set<String> types
    ) {
        if (!validRequester(userId, token, deviceId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            return (T) successResponse(passwordsService.getKeychain(userId, token, page, pageSize, keywords, types));
        } catch (Exception e) {
            return (T) failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @PutMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String notifyCopiedPassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @PathVariable(PASSWORD_IDENTIFIER_KEY) String passwordId
    ) {
        if (!validPasswordRequest(userId, token, deviceId, passwordId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        passwordsService.notifyCopiedPassword(passwordId);
        return successResponse();
    }

    @PatchMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}" + REFRESH_ENDPOINT,
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String refreshGeneratedPassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @PathVariable(PASSWORD_IDENTIFIER_KEY) String passwordId
    ) {
        if (!validPasswordRequest(userId, token, deviceId, passwordId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            return successResponse(passwordsService.refreshGeneratedPassword(token, passwordId));
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @DeleteMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    public String deletePassword(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @PathVariable(PASSWORD_IDENTIFIER_KEY) String passwordId
    ) {
        if (!validPasswordRequest(userId, token, deviceId, passwordId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        passwordsService.deletePassword(passwordId);
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
