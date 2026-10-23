package com.arturjarosz.task.systemparameter.application;

/**
 * Lets other modules react to changed system parameters. All beans implementing it are called after every update.
 */
public interface SystemParameterChangeListener {

    /**
     * Called after the system parameter with given name changed from previousValue to newValue.
     */
    void onSystemParameterChanged(String name, String previousValue, String newValue);
}
