package com.ca.devtest.jenkins.plugin.slavecallable;

import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;
import hudson.FilePath;
import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;

public abstract class AbstractDevTestMasterToSlaveCallable extends MasterToSlaveCallable<DevTestReturnValue,RuntimeException> implements DevTestMasterToSlaveCallable
{
    private FilePath workspace;
    private TaskListener listener;

    protected AbstractDevTestMasterToSlaveCallable(FilePath workspace, TaskListener listener) {
        this.workspace = workspace;
        this.listener = listener;
    }

    public FilePath getWorkspace() {
        return workspace;
    }

    public TaskListener getListener() {
        return listener;
    }
}