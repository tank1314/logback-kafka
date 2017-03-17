package com.dafy.klog.offset;

/**
 * Created by Caedmon on 2017/3/2.
 */
public interface OffsetRecorder {
    void setOffset(long offset);

    long getOffset();
}
