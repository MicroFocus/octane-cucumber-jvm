package com.hpe.alm.octane.infra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

class FileStreamFactory {
  public static OutputStream createOutputStream(URL url) {
    File file = new File(url.getFile());
    ensureParentDirExists(file);
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(file);
    } catch (IOException e) {
      ErrorHandler.error("Could not create FileOutputStream", e);
    }
    return outputStream;
  }

  private static void ensureParentDirExists(File file) {
    if (file.getParentFile() != null && !file.getParentFile().isDirectory()) {
      boolean success = file.getParentFile().mkdirs() || file.getParentFile().isDirectory();
      if (!success) {
        ErrorHandler.error("Failed to create directory " + file.getParentFile().getAbsolutePath());
      }
    }
  }

}
