package com.sdk.karzalivness.enums;

import androidx.annotation.Keep;

@Keep
public enum KEnvironment {

    /* This should be on only for test */
    ALPHA("alpha"),
    BETA("beta"),
    DEV("dev"),

    /* This should be enabled for prod */
    PRODUCTION("prod"),
    TEST("test");


    //**************************************************************************//
    //******************** Constructor + Getter Methods ************************//

    public String env;

    KEnvironment(String env) {
        this.env = env;
    }

    public static KEnvironment get(String env) {
        for (KEnvironment value : values()) {
            if (value.env.equals(env)) {
                return value;
            }
        }
        return TEST;
    }

}
