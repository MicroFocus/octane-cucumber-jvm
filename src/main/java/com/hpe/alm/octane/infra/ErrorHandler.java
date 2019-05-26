package com.hpe.alm.octane.infra;

import cucumber.runtime.CucumberException;

public class ErrorHandler {

    public static void error(String msg) {
        error(msg, null);
    }

    public static void error(String msg, Exception e) {
        String errorMsg = String.format("%s%s", Constants.errorPrefix, msg);
        System.out.println(errorMsg);
        throw new CucumberException(errorMsg, e);
    }
}