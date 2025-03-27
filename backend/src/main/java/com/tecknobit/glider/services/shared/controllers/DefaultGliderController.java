package com.tecknobit.glider.services.shared.controllers;

import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import com.tecknobit.glider.services.users.services.GliderUsersService;
import org.springframework.web.bind.annotation.RestController;

@Structure
@RestController
public abstract class DefaultGliderController extends EquinoxController<GliderUser, GliderUsersRepository,
        GliderUsersService> {

    protected boolean authenticateRequester(String userId, String token, String deviceId) {
        return isMe(userId, token) && me.deviceBelongsToMe(deviceId);
    }

}
