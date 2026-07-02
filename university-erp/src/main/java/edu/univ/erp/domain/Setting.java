package edu.univ.erp.domain;

public class Setting {
    private String key;   // e.g., 'maintenance_on'
    private String value; // e.g., 'true' or 'false'

    // Constructors
    public Setting() {}

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
