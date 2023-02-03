package com.example.mydiary.constants;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public final class Constants {
    public static final String isRequiredErrorMessage = " field is required.";
    public static final String isInvalidErrorMessage = " field is invalid.";
    public static final String passwordErrorMessage = "Please provide password with min 6 characters";
    public static final String passwordsNotMatchErrorMessage = "Passwords do not match.";
    public static final String registerErrorMessage = "Failed to register!";
    public static final String registerSuccessfulMessage = "Successfully registered!\nPlease check your email to verify account! (check spam partition)";
    public static final String noteSavedMessage = "Note successfully saved!";
    public static final String somethingWentWrong = "Something went wrong.";
    public static final String BASE_URL = "https://mydiary-108f5-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String currUserStorageKey = "currUser";
    public static final String sharedPreferencesStorageKey = "sharedPreferencesStorageKey";
    public static final String lastActivityStorageKey = "lastActivityStorageKey";
    public static final String userEmailStorageKey = "userEmailStorageKey";
    public static final String userPasswordStorageKey = "userPasswordStorageKey";
    public static final Integer IMAGE_PICK_CODE = 1000;
    public static final Integer PERMISSION_CODE = 1001;
    public static final Integer MICROPHONE_PERMISSION_CODE = 200;
    public static final int[] ChartMaterials = {
            rgb("#fc0303"), rgb("#ff4f03"), rgb("#ff7903"), rgb("#ffb803"),
            rgb("#ffea03"), rgb("#eaff03"), rgb("#bcff03"), rgb("#14ff03"),
            rgb("#03ff3e"), rgb("#03ff74")
    };
}