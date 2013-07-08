package it.webookia.backend.enums;

/**
 * This class provides a list of all the privacy level of a concrete book.
 */
public enum PrivacyLevel {
    PUBLIC, FRIENDS_ONLY, PRIVATE;

    public static PrivacyLevel getDefault() {
        return PUBLIC;
    }
}
