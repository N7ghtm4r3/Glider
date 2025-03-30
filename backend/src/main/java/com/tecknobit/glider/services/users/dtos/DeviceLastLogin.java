package com.tecknobit.glider.services.users.dtos;

import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;

/**
 * The {@code DeviceLastLogin} record class is the {@link DTO} used to share the information about a device and its
 * current last login in the current session
 *
 * @param device    The device information
 * @param lastLogin The timestamp of the last login the device done in the current session
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
public record DeviceLastLogin(ConnectedDevice device, long lastLogin) {
}
