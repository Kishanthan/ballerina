package org.ballerinalang.model.jvm.model;

public abstract class BNativeFunction {

    public Object result;

    public abstract void execute(Object... args);

}
