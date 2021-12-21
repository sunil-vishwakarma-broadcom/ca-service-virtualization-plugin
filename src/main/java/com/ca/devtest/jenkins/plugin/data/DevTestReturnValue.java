package com.ca.devtest.jenkins.plugin.data;

public class DevTestReturnValue implements iData {

    public DevTestReturnValue(){
        this.success=false;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected String message = "";

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    protected String node = "";

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    protected boolean success;
}