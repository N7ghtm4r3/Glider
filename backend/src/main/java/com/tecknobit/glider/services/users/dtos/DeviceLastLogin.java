package com.tecknobit.glider.services.users.dtos;

import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;

@DTO
public record DeviceLastLogin(ConnectedDevice device, long lastLogin) {
}
