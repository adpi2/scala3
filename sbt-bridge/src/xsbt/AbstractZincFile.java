/*
 * Zinc - The incremental compiler for Scala.
 * Copyright Lightbend, Inc. and Mark Harrah
 */

package xsbt;

import dotty.tools.io.AbstractFile;
import xsbti.PathBasedFile;
import xsbti.VirtualFile;

import java.io.IOException;

interface AbstractZincFile {
  String id();

  static AbstractFile of(VirtualFile virtualFile) {
    if (virtualFile instanceof PathBasedFile)
      return new ZincPlainFile((PathBasedFile) virtualFile);

    try {
      return new ZincVirtualFile(virtualFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("invalid file " + virtualFile.name(), e);
    }
  }
}
