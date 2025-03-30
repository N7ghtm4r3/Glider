package com.tecknobit.glidercore.enums

import kotlinx.serialization.Serializable

/**
 * The `PasswordEventType` are the available events related to a password lifecycle
 */
@Serializable
enum class PasswordEventType {

    /**
     * `GENERATED` the password has been generated
     */
    GENERATED,

    /**
     * `INSERTED` the password has been inserted
     */
    INSERTED,

    /**
     * `COPIED` the password has been copied
     */
    COPIED,

    /**
     * `EDITED` the password has been edited
     */
    EDITED,

    /**
     * `REFRESHED` the password has been refreshed
     */
    REFRESHED

}