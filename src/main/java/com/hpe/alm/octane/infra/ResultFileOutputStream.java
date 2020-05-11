package com.hpe.alm.octane.infra;


import java.io.*;
import java.net.URL;

class ResultFileOutputStream extends OutputStream {

  private FileOutputStream fileOutputStream;

  public ResultFileOutputStream(URL url) throws FileNotFoundException {
    File file = new File(url.getFile());
    ensureParentDirExists(file);
    fileOutputStream = new FileOutputStream(file);
  }

  @Override
  public void write(int b) throws IOException {
    fileOutputStream.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    fileOutputStream.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    fileOutputStream.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    fileOutputStream.flush();
  }

  @Override
  public void close() throws IOException {
    fileOutputStream.close();
  }

  private static void ensureParentDirExists(File file) {
    if (file.getParentFile() != null && !file.getParentFile().isDirectory()) {
      boolean success = file.getParentFile().mkdirs() || file.getParentFile().isDirectory();
      if (!success) {
        ErrorHandler.error("Failed to create directory " + file.getParentFile().getPath());
      }
    }
  }
}
