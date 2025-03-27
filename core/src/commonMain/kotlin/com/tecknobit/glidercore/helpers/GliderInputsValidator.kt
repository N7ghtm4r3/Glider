package com.tecknobit.glidercore.helpers

import com.tecknobit.equinoxcore.annotations.Validator
import com.tecknobit.equinoxcore.helpers.InputsValidator

object GliderInputsValidator : InputsValidator() {

    const val TAIL_MAX_LENGTH = 30

    const val SCOPES_MAX_LENGTH = 50

    @Validator
    fun tailIsValid(
        tail: String?,
    ): Boolean {
        return isInputValid(tail) && tail!!.length <= TAIL_MAX_LENGTH
    }

    @Validator
    fun scopesAreValid(
        scopes: String?,
    ): Boolean {
        return scopes == null || scopes.length <= SCOPES_MAX_LENGTH
    }

    @Validator
    fun passwordLengthValid(
        passwordLength: Int,
    ): Boolean {
        return passwordLength in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
    }

}