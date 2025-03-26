package com.tecknobit.glider.services.users.controller;

import com.tecknobit.equinoxbackend.environment.services.users.controller.EquinoxUsersController;
import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.equinoxcore.annotations.CustomParametersOrder;
import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import com.tecknobit.glider.services.users.services.GliderUsersService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.tecknobit.glidercore.ConstantsKt.DEVICE_KEY;

@RestController
public class GliderUsersController extends EquinoxUsersController<GliderUser, GliderUsersRepository, GliderUsersService> {

    /**
     * {@code WRONG_DEVICE_DATA_MESSAGE} message to use when the device data are wrong
     */
    public static final String WRONG_DEVICE_DATA_MESSAGE = "wrong_device_data";

    /**
     * Method used to get the list of the custom parameters of a custom {@link EquinoxUser} from the payload of the {@link #signUp(Map)}
     * method
     *
     * @return the custom parameters as array of {@link Object}
     * @implNote as default will be returned an empty array, so that means no customized user has being used
     */
    @Override
    @CustomParametersOrder(order = DEVICE_KEY)
    protected Object[] getSignUpCustomParams() {
        return new Object[]{jsonHelper.getString(DEVICE_KEY)};
    }

    /**
     * Method to validate the inputs of the {@link #signUp(Map)} method to correctly execute a sign-up operation
     *
     * @param name     The name of the user
     * @param surname  The surname of the user
     * @param email    The email of the user
     * @param password The password of the user
     * @param language The language of the user
     * @param custom   The custom parameters added in a customization of the {@link EquinoxUser} to execute a customized
     *                 sign up validation
     * @return the key of the error if any inputs is wrong, null otherwise as {@link String}
     */
    @Override
    @CustomParametersOrder(order = DEVICE_KEY)
    protected String validateSignUp(String name, String surname, String email, String password, String language,
                                    Object... custom) {
        String invalidSignUp = super.validateSignUp(name, surname, email, password, language, custom);
        if (invalidSignUp != null)
            return invalidSignUp;
        return validateDeviceData(custom);
    }

    /**
     * Method used to get the list of the custom parameters of a custom {@link EquinoxUser} from the payload of the {@link #signIn(Map)}
     * method
     *
     * @return the custom parameters as array of {@link Object}
     * @implNote as default will be returned an empty array, so that means no customized user has being used
     */
    @Override
    @CustomParametersOrder(order = DEVICE_KEY)
    protected Object[] getSignInCustomParams() {
        return new Object[]{jsonHelper.getString(DEVICE_KEY)};
    }

    /**
     * Method to validate the inputs of the {@link #signIn(Map)} method to correctly execute a sign-in operation
     *
     * @param email    The email of the user
     * @param password The password of the user
     * @param language The language of the user
     * @param custom   The custom parameters added in a customization of the {@link EquinoxUser} to execute a customized
     *                 sign-in validation
     * @return the key of the error if any inputs is wrong, null otherwise as {@link String}
     */
    @Override
    @CustomParametersOrder(order = DEVICE_KEY)
    protected String validateSignIn(String email, String password, String language,
                                    Object... custom) {
        String invalidSignIn = super.validateSignIn(email, password, language, custom);
        if (invalidSignIn != null)
            return invalidSignIn;
        return validateDeviceData(custom);
    }

    @Validator
    private String validateDeviceData(Object... custom) {
        String deviceData = (String) custom[0];
        if (deviceData == null)
            return WRONG_DEVICE_DATA_MESSAGE;
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote Endpoint not used
     */
    @Override
    public String changeProfilePic(String id, String token, MultipartFile profilePic) {
        return null;
    }

}
