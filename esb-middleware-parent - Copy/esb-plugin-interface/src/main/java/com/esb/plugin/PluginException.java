package com.esb.plugin;

/**
 * Exception class for plugin-related errors
 */
public class PluginException extends Exception {

    private String errorCode;
    private String pluginId;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PluginException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public PluginException(String pluginId, String errorCode, String message) {
        super(message);
        this.pluginId = pluginId;
        this.errorCode = errorCode;
    }

    public PluginException(String pluginId, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PluginException{");
        if (pluginId != null) {
            sb.append("pluginId='").append(pluginId).append("', ");
        }
        if (errorCode != null) {
            sb.append("errorCode='").append(errorCode).append("', ");
        }
        sb.append("message='").append(getMessage()).append("'");
        sb.append("}");
        return sb.toString();
    }
}