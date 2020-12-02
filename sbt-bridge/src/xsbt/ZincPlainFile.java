/*
 * Zinc - The incremental compiler for Scala.
 * Copyright Lightbend, Inc. and Mark Harrah
 */

package xsbt;

import xsbti.PathBasedFile;

public class ZincPlainFile extends dotty.tools.io.PlainFile implements AbstractZincFile {
  private final PathBasedFile underlying;

  public ZincPlainFile(PathBasedFile underlying) {
    super(new dotty.tools.io.Path(underlying.toPath()));
    this.underlying = underlying;
  }

  @Override
  public String id() {
    return underlying.id();
  }
}