package com.tecknobit.glider.services.passwords.controller;

import com.tecknobit.glider.services.passwords.service.PasswordsService;
import com.tecknobit.glider.services.shared.controllers.DefaultGliderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.glidercore.ConstantsKt.DEVICE_IDENTIFIER_KEY;
import static com.tecknobit.glidercore.ConstantsKt.PASSWORDS_KEY;

@RestController
@RequestMapping(path = BASE_EQUINOX_ENDPOINT + USERS_KEY + "/{" + IDENTIFIER_KEY + "}/" + PASSWORDS_KEY)
public class PasswordsController extends DefaultGliderController {

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
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId
    ) {
        if (!validRequester(userId, token, deviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        passwordsService.generatePassword(userId, token, 6, false, false, false);
        return successResponse();
    }

}
