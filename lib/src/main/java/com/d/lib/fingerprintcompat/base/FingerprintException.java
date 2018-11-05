package com.d.lib.fingerprintcompat.base;

/**
 * FingerprintException
 * Created by D on 2018/11/5.
 **/
public class FingerprintException extends Exception {
    public int code;
    public CharSequence desc;

    public FingerprintException(int code, CharSequence desc) {
        this.code = code;
        this.desc = desc;
    }
}
