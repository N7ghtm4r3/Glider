package com.tecknobit.glidercore.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PasswordEventType {

    GENERATED,

    INSERTED,

    COPIED,

    EDITED,

    REFRESHED

}