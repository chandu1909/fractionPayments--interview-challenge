package com.fraction.payments.appConfig;

public enum ErrorsAndWarnings {
    DATABASE_UPDATE_ERROR("FRACTION101", "Updating database field {} failed. Reason : {}. ");


    //constructor
    private final String code;
    private final String description;

    ErrorsAndWarnings(String code, String description) {
        this.code = code;
        this.description = description;
    }
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}



