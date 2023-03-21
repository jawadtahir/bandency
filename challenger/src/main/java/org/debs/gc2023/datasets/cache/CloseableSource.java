package org.debs.gc2023.datasets.cache;

import java.io.Closeable;
import java.util.Enumeration;

public interface CloseableSource<T> extends Enumeration<T>, Closeable {
}
