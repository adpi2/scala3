package sbt

import sbt.Keys.*
import sbt.nio.Keys.*
import sbt.nio.FileStamper
import com.gradle.develocity.agent.sbt.DevelocityPlugin
import com.gradle.develocity.agent.sbt.DevelocityPlugin.autoImport.*
import com.gradle.develocity.agent.sbt.api.experimental.buildcache
import java.nio.file.Path

object CustomDevelocityPlugin extends AutoPlugin {
  override def requires: Plugins = DevelocityPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val extraDevelocityCacheInputFiles = taskKey[Seq[Path]]("Extra input files for caching")
  }
  import autoImport.*
  
  override def buildSettings: Seq[Def.Setting[?]] = Seq(
    develocityConfiguration := {
      val isInsideCI = insideCI.value
      val config = develocityConfiguration.value
      val buildScan = config.buildScan
      val buildCache = config.buildCache
      // disable test retry on compilation test classes
      val noRetryTestClasses = Set(
        "dotty.tools.dotc.BestEffortOptionsTests",
        "dotty.tools.dotc.CompilationTests",
        "dotty.tools.dotc.FromTastyTests",
        "dotty.tools.dotc.IdempotencyTests",
        "dotty.tools.dotc.ScalaJSCompilationTests",
        "dotty.tools.dotc.TastyBootstrapTests",
        "dotty.tools.dotc.coverage.CoverageTests",
        "dotty.tools.dotc.transform.PatmatExhaustivityTest",
        "dotty.tools.repl.ScriptedTests"
      )
      config
        .withProjectId(ProjectId("scala3"))
        .withServer(config.server.withUrl(Some(url("https://develocity.scala-lang.org"))))
        .withBuildScan(
          buildScan
            .withPublishing(Publishing.onlyIf(_.authenticated))
            .withBackgroundUpload(!isInsideCI)
            .withTag(if (isInsideCI) "CI" else "Local")
            .withLinks(buildScan.links ++ GithubEnv.develocityLinks)
            .withValues(buildScan.values ++ GithubEnv.develocityValues)
            .withObfuscation(buildScan.obfuscation.withIpAddresses(_.map(_ => "0.0.0.0")))
        )
        .withBuildCache(
          buildCache
            .withLocal(buildCache.local.withEnabled(true).withStoreEnabled(true))
            .withRemote(buildCache.remote.withEnabled(true).withStoreEnabled(isInsideCI))
            .withRequireClean(!isInsideCI)
        )
        .withTestRetry(
          config.testRetry
            .withFlakyTestPolicy(FlakyTestPolicy.Fail)
            .withMaxRetries(if (isInsideCI) 1 else 0)
            .withMaxFailures(10)
            .withClassesFilter((className, _) => !noRetryTestClasses.contains(className))
        )
    },
    // Deactivate Develocity's test caching because it caches all tests or nothing.
    // Also at the moment, it does not take compilation files as inputs.
    Test / develocityBuildCacheClient := None,
    extraDevelocityCacheInputFiles := Seq.empty,
    extraDevelocityCacheInputFiles / outputFileStamper := FileStamper.Hash,
  )

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    // add extraDevelocityCacheInputFiles in cache key components 
    Compile / compile / buildcache.develocityTaskCacheKeyComponents += (Compile / extraDevelocityCacheInputFiles / outputFileStamps).taskValue,
    Test / test / buildcache.develocityTaskCacheKeyComponents += (Test / extraDevelocityCacheInputFiles / outputFileStamps).taskValue,
    Test / testOnly / buildcache.develocityInputTaskCacheKeyComponents += (Test / extraDevelocityCacheInputFiles / outputFileStamps).taskValue,
    Test / testQuick / buildcache.develocityInputTaskCacheKeyComponents += (Test / extraDevelocityCacheInputFiles / outputFileStamps).taskValue
  )
}
