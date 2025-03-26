package com.tecknobit.glider.services.users.services;

import com.tecknobit.equinoxbackend.environment.services.users.service.EquinoxUsersService;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public class GliderUsersService extends EquinoxUsersService<GliderUser, GliderUsersRepository> {

    @Autowired
    private DevicesService devicesService;

    /**
     * Method to sign up a new user in the system
     *
     * @param id       The identifier of the user
     * @param token    The token of the user
     * @param name     The name of the user
     * @param surname  The surname of the user
     * @param email    The email of the user
     * @param password The password of the user
     * @param language The language of the user
     * @param custom   The custom parameters to add in the default query
     * @apiNote the order of the custom parameters must be the same of that specified in the {@link #getSignUpKeys()}
     */
    @Override
    public void signUpUser(String id, String token, String name, String surname, String email, String password,
                           String language, Object... custom) throws NoSuchAlgorithmException {
        super.signUpUser(id, token, name, surname, email, password, language);
        devicesService.storeDevice(id, custom);
    }

}
