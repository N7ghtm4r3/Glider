package com.tecknobit.glidercore.helpers

import com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet

/**
 * The `GliderEndpointsSet` class is a container with all the Glider's system endpoints
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see EquinoxBaseEndpointsSet
 */
object GliderEndpointsSet : EquinoxBaseEndpointsSet() {

    /**
     * `KEYCHAIN_ENDPOINT` the endpoint used to retrieve the keychain of the user
     */
    const val KEYCHAIN_ENDPOINT = "keychain"

    /**
     * `REFRESH_ENDPOINT` the endpoint used to refresh a [com.tecknobit.glidercore.enums.PasswordType.GENERATED] password
     */
    const val REFRESH_ENDPOINT = "/refresh"

}