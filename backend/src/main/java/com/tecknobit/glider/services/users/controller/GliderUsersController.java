package com.tecknobit.glider.services.users.controller;

import com.tecknobit.apimanager.annotations.RequestPath;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxbackend.environment.services.users.controller.EquinoxUsersController;
import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.equinoxcore.annotations.CustomParametersOrder;
import com.tecknobit.equinoxcore.annotations.Validator;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import com.tecknobit.glider.services.users.services.GliderUsersService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.tecknobit.apimanager.apis.APIRequest.RequestMethod.DELETE;
import static com.tecknobit.apimanager.apis.APIRequest.RequestMethod.GET;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.DYNAMIC_ACCOUNT_DATA_ENDPOINT;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;
import static com.tecknobit.glidercore.ConstantsKt.*;

/**
 * The {@code GliderUsersController} class is useful to manage all the user operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see EquinoxUsersController
 */
@RestController
public class GliderUsersController extends EquinoxUsersController<GliderUser, GliderUsersRepository, GliderUsersService> {

    /**
     * {@code WRONG_DEVICE_DATA_MESSAGE} message to use when the device data are wrong
     */
    public static final String WRONG_DEVICE_DATA_MESSAGE = "wrong_device_data";

    /**
     * Constructor to init the controller
     *
     * @param usersService The helper to manage the users database operations
     */
    @Autowired
    public GliderUsersController(GliderUsersService usersService) {
        super(usersService);
    }

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
        return new Object[]{jsonHelper.getJSONObject(DEVICE_KEY)};
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
        return new Object[]{jsonHelper.getJSONObject(DEVICE_KEY)};
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

    /**
     * Method used to validate the data of the device where the user trying to authenticate
     *
     * @param custom The custom parameters from retrieve the device data
     * @return whether the device data are valid as {@link String}, null if they are, error message otherwise
     */
    @Validator
    @CustomParametersOrder(order = DEVICE_KEY)
    private String validateDeviceData(Object... custom) {
        JSONObject deviceData = (JSONObject) custom[0];
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
    public String getDynamicAccountData(String id, String token) {
        return null;
    }

    /**
     * Method used to get the dynamic data of the user to correctly update in all the devices where the user is connected
     *
     * @param id       The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device
     * @return the result of the request as {@link String}
     */
    @GetMapping(
            path = USERS_KEY + "/{" + IDENTIFIER_KEY + "}" + DYNAMIC_ACCOUNT_DATA_ENDPOINT,
            headers = {
                    TOKEN_KEY,
                    DEVICE_IDENTIFIER_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/dynamicAccountData", method = GET)
    public String getDynamicAccountData(
            @PathVariable(IDENTIFIER_KEY) String id,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId
    ) {
        if (!isMe(id, token) || !me.deviceBelongsToMe(deviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return successResponse(usersService.getDynamicAccountData(id, deviceId));
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

    /**
     * Endpoint used to get the connected device of the user
     *
     * @param userId   The identifier of the user
     * @param token    The token of the user
     * @param deviceId The identifier of the device of the user
     * @param page     The page requested
     * @param pageSize The size of the items to insert in the page
     * @return the result of the request as {@link T}
     */
    @GetMapping(
            path = USERS_KEY + "/{" + IDENTIFIER_KEY + "}" + "/" + DEVICES_KEY,
            headers = {
                    TOKEN_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/devices", method = GET)
    public <T> T getDevices(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        if (!isMe(userId, token) || !me.deviceBelongsToMe(deviceId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(usersService.getPagedDevices(page, pageSize, userId));
    }

    /**
     * Endpoint used to disconnect a device from the current session
     *
     * @param userId The identifier of the user
     * @param token The token of the user
     * @param deviceId The identifier of the device of the user
     *
     * @return the result of the request as {@link String}
     */
    @DeleteMapping(
            path = USERS_KEY + "/{" + IDENTIFIER_KEY + "}" + "/" + DEVICES_KEY,
            headers = {
                    TOKEN_KEY
            }
    )
    @RequestPath(path = "/api/v1/users/{id}/devices", method = DELETE)
    public String disconnectDevice(
            @PathVariable(IDENTIFIER_KEY) String userId,
            @RequestHeader(TOKEN_KEY) String token,
            @RequestHeader(DEVICE_IDENTIFIER_KEY) String deviceId,
            @RequestBody Map<String, String> payload
    ) {
        loadJsonHelper(payload);
        String disconnectingDeviceId = jsonHelper.getString(DEVICE_IDENTIFIER_KEY);
        if (!isMe(userId, token) || !me.deviceBelongsToMe(deviceId) || !me.deviceBelongsToMe(disconnectingDeviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        usersService.disconnectDevice(userId, disconnectingDeviceId);
        return successResponse();
    }

}
