package org.apache;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

public class CustomPropertiesConfiguration extends PropertiesConfiguration {
    public CustomPropertiesConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    public String getString(String key) {
        final String value = System.getProperty(key);
        if (!StringUtils.isEmpty(value))
            return value;
        return super.getString(key);
    }

    public String[] getStringArray(String key) {
        final String values = System.getProperty(key);
        if (!StringUtils.isEmpty(values))
            return values.split(",");
        return super.getStringArray(key);
    }
}
