package com.tecknobit.glider.services.passwords.controller;

import com.tecknobit.apimanager.annotations.RequestPath;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.passwords.entities.Password;
import com.tecknobit.glider.services.passwords.services.PasswordsService;
import com.tecknobit.glider.services.shared.controllers.DefaultGliderController;
import com.tecknobit.glidercore.enums.PasswordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static com.tecknobit.apimanager.apis.APIRequest.RequestMethod.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.helpers.GliderEndpointsSet.KEYCHAIN_ENDPOINT;
import static com.tecknobit.glidercore.helpers.GliderEndpointsSet.REFRESH_ENDPOINT;
import static com.tecknobit.glidercore.helpers.GliderInputsValidator.INSTANCE;

/**
 * The {@code PasswordsController} class is useful to handle the requests to operate with {@link Password} entities
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see DefaultGliderController
 */
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

    /**
     * {@code passwordsService} helper to manage the passwords database operations
     */
    private final PasswordsService passwordsService;

    /**
     * Constructor used to init the controller
     *
     * @param passwordsService The helper to manage the passwords database operations
     */
    @Autowired
    public PasswordsController(PasswordsService passwordsService) {
        this.passwordsService = passwordsService;
    }

    /**
     * Endpoint used to generate a new password for a user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param payload  Payload of the request
     *                 <pre>
     *                                      {@code
     *                                              {
     *                                                  "length" : "the length of the password" -> [Integer],
     *                                                  "include_numbers" : "whether the generated password must include the numbers" -> [Boolean],
     *                                                  "include_uppercase_letters": "whether the generated password must include the uppercase letters" -> [Boolean],
     *                                                  "include_special_characters": "whether the generated password must include the special characters" -> [Boolean],
     *                                                  "tail": "the tail of the password" -> [String],
     *                                                  "scopes": "the scopes of the password" -> [String]
     *                                              }
     *                                      }
     *                                 </pre>
     * @return the result of the request as {@link String}
     */
    @PutMapping(
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords", method = PUT)
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

    /**
     * Endpoint used to insert a password owned by a user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param payload Payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "tail": "the tail of the password" -> [String],
     *                                  "scopes": "the scopes of the password" -> [String],
     *                                  "password": "the value of the password" -> [String]
     *                              }
     *                      }
     *                 </pre>
     * @return the result of the request as {@link String}
     */
    @PostMapping(
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords", method = POST)
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

    /**
     * Endpoint used to edit an existing password of a user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param passwordId The identifier of the password
     * @param payload Payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "tail": "the tail of the password" -> [String],
     *                                  "scopes": "the scopes of the password" -> [String],
     *                                  "password": "the value of the password" -> [String] (if INSERTED password)
     *                              }
     *                      }
     *                 </pre>
     * @return the result of the request as {@link String}
     */
    @PatchMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/{password_id}", method = PATCH)
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

    /**
     * Endpoint used to retrieve a password owned by the user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param passwordId The identifier of the password
     *
     * @return the result of the request as {@link T}
     */
    @GetMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/{password_id}", method = GET)
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

    /**
     * Endpoint used to retrieve the keychain owned by the user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param page      The page requested
     * @param pageSize  The size of the items to insert in the page
     * @param keywords The filter keywords
     * @param types The types of the passwords to retrieve
     *
     * @return the result of the request as {@link T}
     */
    @GetMapping(
            path = "/" + KEYCHAIN_ENDPOINT,
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/keychain", method = GET)
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

    /**
     * Endpoint used to notify the copy of a password
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param passwordId The identifier of the password
     *
     * @return the result of the request as {@link String}
     */
    @PutMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/{password_id}", method = PUT)
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

    /**
     * Endpoint used to refresh a {@link PasswordType#GENERATED} password
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param passwordId The identifier of the password
     *
     * @return the result of the request as {@link String}
     */
    @PatchMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}" + REFRESH_ENDPOINT,
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/{password_id}/refresh", method = PATCH)
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

    /**
     * Endpoint used to delete a password
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param passwordId The identifier of the password
     *
     * @return the result of the request as {@link String}
     */
    @DeleteMapping(
            path = "/{" + PASSWORD_IDENTIFIER_KEY + "}",
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/passwords/{password_id}", method = DELETE)
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

    /**
     * Method used to validate a request related to a {@link Password}'s operation
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device from the user request
     * @param passwordId The identifier of the password
     * @return whether the requester user is authorized to work on the specified password
     */
    @Validator
    private boolean validPasswordRequest(String userId, String token, String deviceId, String passwordId) {
        boolean validRequester = validRequester(userId, token, deviceId);
        if (!validRequester)
            return false;
        return me.passwordBelongsToMe(passwordId);
    }

}
