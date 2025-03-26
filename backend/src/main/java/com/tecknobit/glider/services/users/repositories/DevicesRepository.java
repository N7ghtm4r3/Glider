package com.tecknobit.glider.services.users.repositories;

import com.tecknobit.glider.services.users.entities.ConnectedDevice;
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
import static com.tecknobit.glidercore.ConstantsKt.*;

@Repository
public interface DevicesRepository extends JpaRepository<ConnectedDevice, String> {

    @Transactional
    @Modifying(
            clearAutomatically = true
    )
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

    @Query(
            value = "SELECT COUNT(*) FROM " + USER_DEVICES_KEY +
                    _WHERE_ + DEVICE_IDENTIFIER_KEY + "=:" + DEVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long countDeviceReferences(
            @Param(DEVICE_IDENTIFIER_KEY) String deviceId
    );

    @Query(
            value = "SELECT COUNT(*) FROM " + USER_DEVICES_KEY +
                    _WHERE_ + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long countDevices(
            @Param(USER_IDENTIFIER_KEY) String userId
    );

    @Query(
            value = "SELECT d.* FROM " + DEVICES_KEY + " AS d" +
                    " INNER JOIN " + USER_DEVICES_KEY + " AS ud" +
                    " ON d." + IDENTIFIER_KEY + "=" + "ud." + DEVICE_IDENTIFIER_KEY +
                    _WHERE_ + "ud." + USER_IDENTIFIER_KEY + "=:" + USER_IDENTIFIER_KEY,
            nativeQuery = true
    )
    List<ConnectedDevice> getDevices(
            @Param(USER_IDENTIFIER_KEY) String userId,
            Pageable pageable
    );

}
