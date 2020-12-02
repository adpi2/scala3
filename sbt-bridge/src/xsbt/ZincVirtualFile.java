/*
 * Zinc - The incremental compiler for Scala.
 * Copyright Lightbend, Inc. and Mark Harrah
 */

package xsbt;

import dotty.tools.io.Streamable;
import xsbti.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZincVirtualFile extends dotty.tools.io.VirtualFile implements AbstractZincFile {
  private final VirtualFile underlying;

  public ZincVirtualFile(VirtualFile underlying) throws IOException {
    super(underlying.name(), underlying.id());
    this.underlying = underlying;

    // fill in the content
    OutputStream output = output();
    try {
      Streamable.Bytes bytes = new Streamable.Bytes() {
        @Override
        public InputStream inputStream() {
          return underlying.input();
        }
      };
      output.write(bytes.toByteArray());
    } finally {
      output.close();
    }
  }

  @Override
  public String id() {
    return underlying.id();
  }
}
