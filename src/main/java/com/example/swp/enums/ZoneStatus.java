package com.example.swp.enums;

public enum ZoneStatus {
    AVAILABLE("available", "Còn trống"),
    OCCUPIED("occupied", "Đã thuê"),
    MAINTENANCE("maintenance", "Bảo trì");

    private final String value;
    private final String displayName;

    ZoneStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ZoneStatus fromValue(String value) {
        for (ZoneStatus status : ZoneStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return AVAILABLE; // Default value
    }

    public static String getDisplayName(String value) {
        return fromValue(value).getDisplayName();
    }
}
