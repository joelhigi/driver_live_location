package com.specialops.loctrackerjava.models;

public enum Role {
    UNKNOWN("Unkown"),
    STUDENT("Student"),
    DRIVER("Driver"),
    ADMIN("ADMIN");

    private String _text;

    Role (String role)
    {
        _text = role;
    }

    public String toString()
    {
        return _text;
    }

    public static Role fromString(String stringValue)
    {
        for (Role option : Role.values())
        {
            if (stringValue.equalsIgnoreCase(option._text))
                return option;
        }
        if (stringValue.equalsIgnoreCase("student"))
            return STUDENT;
        if (stringValue.equalsIgnoreCase("admin"))
            return ADMIN;
        if (stringValue.equalsIgnoreCase("driver"))
            return DRIVER;
        return UNKNOWN;
    }

    public static boolean isValid(String strValue)
    {
        if (fromString(strValue) != UNKNOWN)
            return true;
        return false;
    }
}
