/*
 * Zinc - The incremental compiler for Scala.
 * Copyright Lightbend, Inc. and Mark Harrah
 */

package xsbt;

import dotty.tools.dotc.Compiler;
import dotty.tools.dotc.Driver;
import dotty.tools.dotc.config.CompilerCommand;
import dotty.tools.dotc.config.Properties;
import dotty.tools.dotc.core.Contexts;
import dotty.tools.dotc.util.SourceFile;
import scala.collection.mutable.ListBuffer;
import scala.io.Codec;
import xsbti.Problem;
import xsbti.*;
import xsbti.compile.Output;

public class CompilerBridgeDriver extends Driver {
  private final String[] scalacOptions;
  private final String[] args;

  public CompilerBridgeDriver(String[] scalacOptions, Output output) {
    super();
    this.scalacOptions = scalacOptions;

    if (!output.getSingleOutput().isPresent())
      throw new IllegalArgumentException("output should be a SingleOutput, was a " + output.getClass().getName());

    this.args = new String[scalacOptions.length + 2];
    System.arraycopy(scalacOptions, 0, args, 0, scalacOptions.length);
    args[scalacOptions.length] = "-d";
    args[scalacOptions.length + 1] = output.getSingleOutput().get().getAbsolutePath();
  }

  private static final String StopInfoError =
    "Compiler option supplied that disabled Zinc compilation.";

  /**
   * `sourcesRequired` is set to false because the context is set up with no sources
   * The sources are passed programmatically to the compiler in the form of SourceFile instances
   */
  @Override
  public boolean sourcesRequired() {
    return false;
  }

  synchronized public void run(VirtualFile[] sources, AnalysisCallback callback, Logger log, Reporter delegate) {
    DelegatingReporter reporter = new DelegatingReporter(delegate);
    try {
      log.debug(this::infoOnCachedCompiler);

      Contexts.Context initialCtx = initCtx()
        .fresh()
        .setReporter(reporter)
        .setSbtCallback(callback);

      Contexts.Context context = setup(args, initialCtx)._2;

      if(CompilerCommand.shouldStopWithInfo(context)) {
        throw new InterfaceCompileFailed(args, new Problem[0], StopInfoError);
      }

      if (!delegate.hasErrors()) {
        log.debug(this::prettyPrintCompilationArguments);
        Compiler compiler = newCompiler(context);

        ListBuffer<SourceFile> sourcesBuffer = new ListBuffer<>();
        for (VirtualFile file: sources)
          sourcesBuffer.append(new SourceFile(AbstractZincFile.of(file), Codec.UTF8()));
        doCompileSources(compiler, sourcesBuffer.toList(), context);

        for (xsbti.Problem problem: delegate.problems()) {
          callback.problem(problem.category(), problem.position(), problem.message(), problem.severity(),
            true);
        }
      }

      delegate.printSummary();

      if (delegate.hasErrors()) {
        log.debug(() -> "Compilation failed");
        throw new InterfaceCompileFailed(args, delegate.problems(), "Compilation failed");
      }
    } finally {
      reporter.dropDelegate();
    }
  }

  private String infoOnCachedCompiler() {
    String compilerId = Integer.toHexString(hashCode());
    String compilerVersion = Properties.versionString();
    return String.format("[zinc] Running cached compiler %s for Scala Compiler %s", compilerId, compilerVersion);
  }

  private String prettyPrintCompilationArguments() {
    StringBuilder builder = new StringBuilder();
    builder.append("[zinc] The Scala compiler is invoked with:");
    for (String opt: scalacOptions) {
      builder.append("\n\t");
      builder.append(opt);
    }
    return builder.toString();
  }

}
