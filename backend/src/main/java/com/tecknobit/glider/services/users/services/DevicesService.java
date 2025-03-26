package com.tecknobit.glider.services.users.services;

import com.tecknobit.glider.services.users.entities.ConnectedDevice;
import com.tecknobit.glider.services.users.repositories.DevicesRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevicesService {

    @Autowired
    private DevicesRepository devicesRepository;

    public void storeDevice(String userId, Object[] rawDevice) {
        try {
            JSONObject device = new JSONObject(rawDevice[0]);
            ConnectedDevice connectedDevice = new ConnectedDevice(device);
            System.out.println(connectedDevice.getId());
            devicesRepository.save(connectedDevice);
            devicesRepository.attachDeviceToUser(userId, connectedDevice.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
