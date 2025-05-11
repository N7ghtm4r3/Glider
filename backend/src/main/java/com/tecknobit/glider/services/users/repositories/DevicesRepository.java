package com.tecknobit.glider.services.users.repositories;

import com.tecknobit.glider.services.users.dtos.DeviceLastLogin;
import com.tecknobit.glider.services.users.entities.ConnectedDevice;
import com.tecknobit.glidercore.ConstantsKt;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.USER_IDENTIFIER_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;

/**
 * The {@code DevicesRepository} interface is useful to manage the queries for the devices operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 * @see ConnectedDevice
 */
@Repository
public interface DevicesRepository extends JpaRepository<ConnectedDevice, String> {

    /**
     * Query used to attach a device to a user
     *
     * @param sessionId The identifier of the session
     * @param userId    The identifier of the user
     * @param deviceId  The identifier of the device
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + USER_DEVICES_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    USER_IDENTIFIER_KEY + "," +
                    DEVICE_IDENTIFIER_KEY
                    + ") VALUES (" +
                    ":" + SESSION_IDENTIFIER_KEY + "," +
                    ":" + USER_IDENTIFIER_KEY + "," +
                    ":" + DEVICE_IDENTIFIER_KEY
                    + ")",
            nativeQuery = true
    )
    void attachDeviceToUser(
            @Param(SESSION_IDENTIFIER_KEY) String sessionId,
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(DEVICE_IDENTIFIER_KEY) String deviceId
    );

    /**
     * Query used to count the total references of the device between sessions
     *
     * @param deviceId The identifier of the device
     * @return the total references as {@code long}
     */
    @Query(
            value = "SELECT COUNT(*) FROM " + USER_DEVICES_KEY +
                    _WHERE_ + DEVICE_IDENTIFIER_KEY + "=:" + DEVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long countDeviceReferences(
            @Param(DEVICE_IDENTIFIER_KEY) String deviceId
    );

    /**
     * Query used to update the last login by a device in the specified session
     *
     * @param userId The identifier of the user
     * @param deviceId The identifier of the device
     * @param lastLogin The last login timestamp
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + USER_DEVICES_KEY + " SET " +
                    LAST_LOGIN_KEY + "=:" + LAST_LOGIN_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY
                    + " AND " + DEVICE_IDENTIFIER_KEY + "=:" + DEVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    void updateLastLogin(
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(DEVICE_IDENTIFIER_KEY) String deviceId,
            @Param(LAST_LOGIN_KEY) long lastLogin
    );

    /**
     * Query used to count the total devices owned by a user
     *
     * @param userId The identifier of the user
     * @return the total devices as {@code long}
     */
    @Query(
            value = "SELECT DISTINCT COUNT(*) FROM " + USER_DEVICES_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long countDevices(
            @Param(USER_IDENTIFIER_KEY) String userId
    );

    /**
     * Query used to retrieve all the devices owned by the user
     *
     * @param userId The identifier of the user
     * @param pageable The parameters to paginate the query
     *
     * @return the devices owned by the user as {@link List} of {@link DeviceLastLogin}
     */
    @Query(
            value = "SELECT DISTINCT new com.tecknobit.glider.services.users.dtos.DeviceLastLogin(" +
                    "d," +
                    "ud." + "lastLogin" +
                    ") FROM com.tecknobit.glider.services.users.entities.ConnectedDevice d" +
                    " INNER JOIN com.tecknobit.glider.services.users.entities.DeviceUserSession ud" +
                    " ON d." + IDENTIFIER_KEY + "=" + "ud.device." + IDENTIFIER_KEY +
                    " WHERE ud.user." + IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY
    )
    List<DeviceLastLogin> getDevices(
            @Param(USER_IDENTIFIER_KEY) String userId,
            Pageable pageable
    );

    /**
     * Query used to disconnect a device from the session
     *
     * @param userId The identifier of the user
     * @param deviceId The identifier of the device
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "DELETE FROM " + USER_DEVICES_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY
                    + " AND " + DEVICE_IDENTIFIER_KEY + "=:" + DEVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    void disconnectDevice(
            @Param(USER_IDENTIFIER_KEY) String userId,
            @Param(ConstantsKt.DEVICE_IDENTIFIER_KEY) String deviceId
    );

}
