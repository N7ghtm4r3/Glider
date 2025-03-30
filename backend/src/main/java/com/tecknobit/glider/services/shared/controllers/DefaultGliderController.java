package com.tecknobit.glider.services.shared.controllers;

import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import com.tecknobit.glider.services.users.services.GliderUsersService;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code DefaultGliderController} class is useful to give the base behavior of the <b>Glider's controllers</b>
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Structure
@RestController
public abstract class DefaultGliderController extends EquinoxController<GliderUser, GliderUsersRepository,
        GliderUsersService> {

    /**
     * Method used to validate the requester's identifier, token and the device id from requested an operation
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device from the user request
     * @return whether the requester is authorized as {@code boolean}
     */
    @Validator
    protected boolean validRequester(String userId, String token, String deviceId) {
        return isMe(userId, token) && me.deviceBelongsToMe(deviceId);
    }

}
