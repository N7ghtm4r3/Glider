package com.tecknobit.glider.services.users.services;

import com.tecknobit.equinoxcore.annotations.CustomParametersOrder;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import com.tecknobit.glider.services.users.dtos.DeviceLastLogin;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;
import com.tecknobit.glider.services.users.repositories.DevicesRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;
import static com.tecknobit.glidercore.ConstantsKt.DEVICE_KEY;

/**
 * The {@code DevicesService} class is useful to manage all the device database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class DevicesService {

    /**
     * {@code devicesRepository} instance used to manage the database operations for the devices
     */
    @Autowired
    private DevicesRepository devicesRepository;

    /**
     * Method used to store a device
     *
     * @param userId    The identifier of the user
     * @param rawDevice The raw data of the device
     */
    @CustomParametersOrder(order = {DEVICE_KEY})
    public void storeDevice(String userId, Object[] rawDevice) {
        JSONObject device = new JSONObject(rawDevice[0].toString());
        ConnectedDevice connectedDevice = new ConnectedDevice(device);
        String deviceId = connectedDevice.getId();
        devicesRepository.save(connectedDevice);
        devicesRepository.attachDeviceToUser(generateIdentifier(), userId, deviceId);
        updateLastLogin(userId, deviceId);
    }

    /**
     * Method used to update the last login by a device in the specified session
     *
     * @param userId   The identifier of the user
     * @param deviceId The identifier of the device
     */
    public void updateLastLogin(String userId, String deviceId) {
        devicesRepository.updateLastLogin(userId, deviceId, System.currentTimeMillis());
    }

    /**
     * Method used to retrieve all the devices owned by the user
     *
     * @param page      The page requested
     * @param pageSize  The size of the items to insert in the page
     * @param userId The identifier of the user
     *
     * @return the devices owned by the user as {@link PaginatedResponse} of {@link ConnectedDevice}
     */
    public PaginatedResponse<ConnectedDevice> getDevices(int page, int pageSize, String userId) {
        long totalDevices = devicesRepository.countDevices(userId);
        List<DeviceLastLogin> rawDevices = devicesRepository.getDevices(userId, PageRequest.of(page, pageSize));
        List<ConnectedDevice> devices = new ArrayList<>();
        for (DeviceLastLogin deviceData : rawDevices) {
            ConnectedDevice device = deviceData.device();
            device.setLastLogin(deviceData.lastLogin());
            devices.add(device);
        }
        return new PaginatedResponse<>(devices, page, pageSize, totalDevices);
    }

    /**
     * Method used to disconnect a device from the session
     *
     * @param userId The identifier of the user
     * @param deviceId The identifier of the device
     */
    public void disconnectDevice(String userId, String deviceId) {
        devicesRepository.disconnectDevice(userId, deviceId);
        deleteDeviceIfNotReferenced(deviceId);
    }

    /**
     * Method used to delete a device if not referenced, so used, in any session registered in the system
     *
     * @param device The device to delete
     */
    @Wrapper
    public void deleteDeviceIfNotReferenced(ConnectedDevice device) {
        deleteDeviceIfNotReferenced(device.getId());
    }

    /**
     * Method used to delete a device if not referenced, so used, in any session registered in the system
     *
     * @param deviceId The identifier of the device
     */
    public void deleteDeviceIfNotReferenced(String deviceId) {
        boolean isNotReferenced = devicesRepository.countDeviceReferences(deviceId) == 0;
        if (isNotReferenced)
            devicesRepository.deleteById(deviceId);
    }

}
