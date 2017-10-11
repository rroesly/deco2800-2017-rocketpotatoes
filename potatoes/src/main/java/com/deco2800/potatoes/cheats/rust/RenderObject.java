package com.deco2800.potatoes.cheats.rust;

import com.sun.jna.Structure;

import java.io.Closeable;
import java.util.List;
import java.util.Arrays;

public class RenderObject extends Structure implements Closeable {
    public static class ByReference extends RenderObject implements Structure.ByReference {
    }

    public static class ByValue extends RenderObject implements Structure.ByValue {
    }

    public String asset;
    public int x;
    public int y;
    public float rotation;
    public float scale;
    public int flipX;
    public int flipY;


    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("asset", "x", "y", "rotation", "scale", "flipX", "flipY");
    }

    @Override
    public void close() {
        // Turn off "auto-synch". If it is on, JNA will automatically read all fields
        // from the struct's memory and update them on the Java object. This synchronization
        // occurs after every native method call. If it occurs after we drop the struct, JNA
        // will try to read from the freed memory and cause a segmentation fault.
        setAutoSynch(false);
    }
}
