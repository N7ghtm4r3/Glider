package com.tecknobit.glidercore.helpers

import com.tecknobit.equinoxcore.annotations.Validator
import com.tecknobit.equinoxcore.helpers.InputsValidator

/**
 * The `GliderInputsValidator` class is useful to validate the inputs
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see InputsValidator
 */
object GliderInputsValidator : InputsValidator() {

    /**
     * `TAIL_MAX_LENGTH` max allowed length for the tail of the passwords
     */
    const val TAIL_MAX_LENGTH = 30

    /**
     * `SCOPES_MAX_LENGTH` max allowed length for the scopes of the passwords
     */
    const val SCOPES_MAX_LENGTH = 50

    /**
     * Method to validate a tail
     *
     * @param tail The tail value to check the validity
     *
     * @return whether the tail is valid or not as `boolean`
     */
    @Validator
    fun tailIsValid(
        tail: String?,
    ): Boolean {
        return isInputValid(tail) && tail!!.length <= TAIL_MAX_LENGTH
    }

    /**
     * Method to validate a scopes
     *
     * @param scopes The scopes value to check the validity
     *
     * @return whether the scopes are valid or not as `boolean`
     */
    @Validator
    fun scopesAreValid(
        scopes: String?,
    ): Boolean {
        return scopes == null || scopes.length <= SCOPES_MAX_LENGTH
    }

    /**
     * Method to validate the length of the password
     *
     * @param passwordLength The password length value to check the validity
     *
     * @return whether the password length is valid or not as `boolean`
     */
    @Validator
    fun passwordLengthValid(
        passwordLength: Int,
    ): Boolean {
        return passwordLength in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
    }

}