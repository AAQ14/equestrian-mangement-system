package com.ga.equestrian.model.enums;

/**
 * This enum represents the reason of verification code was issued.
 */
public enum CodePurpose {
    /**
     * The code a new user enters to activate the account.
     */
    EMAIL_VERIFICATION,
    /**
     * The code a user enters to set a new password after forgetting it.
     */
    PASSWORD_RESET
}
