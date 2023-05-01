package org.debs.gc2023.datasets.disc;

import java.io.File;
import java.util.List;

public record FileEntry(File file, List<String> csvFiles) {
}
