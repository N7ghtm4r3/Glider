package com.tecknobit.glider.services.users.services;

import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.equinoxbackend.environment.services.users.service.EquinoxUsersService;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import com.tecknobit.glider.helpers.ServerVault;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glider.services.users.repositories.GliderUsersRepository;
import org.json.JSONObject;
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

    public PaginatedResponse<ConnectedDevice> getPagedDevices(int page, int pageSize, String userId) {
        return devicesService.getDevices(page, pageSize, userId);
    }

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
