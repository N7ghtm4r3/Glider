package com.tecknobit.glider.services.users.services;

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

@Service
public class DevicesService {

    @Autowired
    private DevicesRepository devicesRepository;

    public void storeDevice(String userId, Object[] rawDevice) {
        JSONObject device = new JSONObject(rawDevice[0].toString());
        ConnectedDevice connectedDevice = new ConnectedDevice(device);
        String deviceId = connectedDevice.getId();
        devicesRepository.save(connectedDevice);
        devicesRepository.attachDeviceToUser(generateIdentifier(), userId, deviceId);
        updateLastLogin(userId, deviceId);
    }

    public void updateLastLogin(String userId, String deviceId) {
        devicesRepository.updateLastLogin(userId, deviceId, System.currentTimeMillis());
    }

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

    public void deleteDeviceIfNotReferenced(ConnectedDevice device) {
        String deviceId = device.getId();
        boolean isNotReferenced = devicesRepository.countDeviceReferences(deviceId) == 0;
        if (isNotReferenced)
            devicesRepository.deleteById(deviceId);
    }

}
