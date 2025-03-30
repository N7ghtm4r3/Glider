package com.tecknobit.glider.services.users.services;

import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.equinoxbackend.environment.services.users.service.EquinoxUsersService;
import com.tecknobit.equinoxbackend.resourcesutils.ResourcesManager;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PROFILE_PIC_KEY;

/**
 * The {@code GliderUsersService} class is useful to manage all the user database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see ResourcesManager
 * @see EquinoxUsersService
 */
@Service
public class GliderUsersService extends EquinoxUsersService<GliderUser, GliderUsersRepository> {

    /**
     * {@code devicesService} instance used to manage the database operations for the devices
     */
    @Autowired
    private DevicesService devicesService;

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getDynamicAccountData(String userId) {
        JSONObject dynamicData = super.getDynamicAccountData(userId);
        dynamicData.remove(PROFILE_PIC_KEY);
        return dynamicData;
    }

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
                           String language, Object... custom) {
        ServerVault vault = ServerVault.getInstance();
        try {
            vault.createUserPrivateKey(token);
            super.signUpUser(id, token, name, surname, email, password, language);
            devicesService.storeDevice(id, custom);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to sign in an existing user
     *
     * @param email    The email of the user
     * @param password The password of the user
     * @param custom   The custom parameters added in a customization of the {@link EquinoxUser}
     * @return the authenticated user as {@link EquinoxUser} if the credentials inserted were correct
     */
    @Override
    public GliderUser signInUser(String email, String password, Object... custom) throws NoSuchAlgorithmException {
        GliderUser loggedUser = super.signInUser(email, password);
        devicesService.storeDevice(loggedUser.getId(), custom);
        return loggedUser;
    }

    /**
     * Method used to get the dynamic data of the user to correctly update in all the devices where the user is connected
     *
     * @param userId   The identifier of the user
     * @param deviceId The identifier of the device
     * @return the dynamic data as {@link JSONObject}
     */
    public JSONObject getDynamicAccountData(String userId, String deviceId) {
        JSONObject deviceData = getDynamicAccountData(userId);
        devicesService.updateLastLogin(userId, deviceId);
        return deviceData;
    }

    /**
     * Method used to retrieve the devices owned by the user
     *
     * @param page     The page requested
     * @param pageSize The size of the items to insert in the page
     * @param userId   The identifier of the user
     * @return the devices owned by the user as {@link PaginatedResponse} as {@link ConnectedDevice}
     */
    public PaginatedResponse<ConnectedDevice> getPagedDevices(int page, int pageSize, String userId) {
        return devicesService.getDevices(page, pageSize, userId);
    }

    /**
     * Method used to disconnect a device from the session
     *
     * @param userId   The identifier of the user
     * @param deviceId The identifier of the device
     */
    public void disconnectDevice(String userId, String deviceId) {
        devicesService.disconnectDevice(userId, deviceId);
    }

    /**
     * Method to delete a user
     *
     * @param id The identifier of the user to delete
     */
    @Override
    public void deleteUser(String id) {
        GliderUser user = usersRepository.getReferenceById(id);
        super.deleteUser(id);
        ServerVault vault = ServerVault.getInstance();
        vault.deleteLockBox(user.getToken());
        for (ConnectedDevice device : user.getDevices())
            devicesService.deleteDeviceIfNotReferenced(device);
    }

}
