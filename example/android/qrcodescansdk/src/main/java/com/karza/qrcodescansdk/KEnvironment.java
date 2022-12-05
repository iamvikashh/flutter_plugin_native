package com.karza.qrcodescansdk;

/**
 * Created by Krishn Agarwal on 19/08/22
 */
public enum KEnvironment {

    /* This should be on only for test */
    DEV("dev"),
    ALPHA("alpha"),
    BETA("beta"),

    /* This should be enabled for prod */
    TEST("test"),
    PRODUCTION("prod");


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
