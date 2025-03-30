package com.tecknobit.glidercore.enums

import kotlinx.serialization.Serializable

/**
 * The `ConnectedDeviceType` are the available types of the device which can connect to a session
 */
@Serializable
enum class ConnectedDeviceType {

    /**
     * `MOBILE` mobile device
     */
    MOBILE,

    /**
     * `DESKTOP` desktop device
     */
    DESKTOP,

    /**
     * `WEB` device connected from a browser
     */
    WEB

}