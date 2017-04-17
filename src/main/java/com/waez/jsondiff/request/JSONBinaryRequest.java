package com.waez.jsondiff.request;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Object used to model the request associated to a JSON with the binary data
 * encoded in base64.
 * 
 * @author Damian
 */
public class JSONBinaryRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte[] binary;

    public byte[] getBinary() {
        return binary;
    }

    public void setBinary(byte[] binary) {
        this.binary = binary;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JSONBinaryRequest [binary=").append(Arrays.toString(binary)).append("]");
        return builder.toString();
    }
}
