package com.itl.comm.omega.OM_USB_TC;

import com.sun.jna.ptr.ByReference;

/**
 * StringByReference: pretty much what it says on the tin.
 * <p>This is a learning tool and should not be included in the final code!
 * <p>Taken from http://jnaexamples.blogspot.com/2012/03/java-native-access-is-easy-way-to.html
 * @author kgraba
 *
 */
public class StringByReference extends ByReference {
    public StringByReference() {
        this(0);
    }

    public StringByReference(int size) {
        super(size < 4 ? 4 : size);
        getPointer().clear(size < 4 ? 4 : size);
    }

    public StringByReference(String str) {
        super(str.length() < 4 ? 4 : str.length() + 1);
        setValue(str);
    }

    private void setValue(String str) {
        getPointer().setString(0, str);
    }

    public String getValue() {
        return getPointer().getString(0);
    }
}