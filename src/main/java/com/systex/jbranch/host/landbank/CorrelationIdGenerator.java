package com.systex.jbranch.host.landbank;

/**
 * @author Alex Lin
 * @version 2011/03/28 4:25 PM
 */
public class CorrelationIdGenerator {
    public String generate() {
        return String.valueOf(System.nanoTime());
    }
}
