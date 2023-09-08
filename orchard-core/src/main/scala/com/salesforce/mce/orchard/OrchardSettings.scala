/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.orchard

import akka.actor.typed.SupervisorStrategy

import scala.concurrent.duration._
import scala.jdk.DurationConverters._
import scala.util.control.Exception.catching

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

class OrchardSettings private (config: Config) {

  def opt[T](f: => T): Option[T] = catching(classOf[ConfigException.Missing]).opt(f)

  def slickDatabaseConf = config.getConfig("jdbc")

  def providerConfig(provider: String): Config = config.getConfig(s"io.$provider")

  val checkProgressDelay = config.getDuration("activity.checkProgressDelay").toScala

  val resourceReattemptDelay = config.getDuration("resource.reAttemptDelay").toScala

  val restartBackoffParams = for {
    min <- opt(config.getInt("restart.min-backoff-seconds"))
    max <- opt(config.getInt("restart.max-backoff-seconds"))
    jitter <- opt(config.getDouble("restart.jitter-probability"))
  } yield {
    OrchardSettings.RestartBackoffParams(min, max, jitter)
  }

}

object OrchardSettings {

  case class RestartBackoffParams(minBackoff: Int, maxBackoff: Int, jitterProbability: Double) {
    val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
      minBackoff.seconds, maxBackoff.seconds, jitterProbability
    )
  }

  val configPath = "com.salesforce.mce.orchard"

  def withRootConfig(rootConfig: Config): OrchardSettings = new OrchardSettings(
    rootConfig.getConfig(configPath)
  )

  def apply(): OrchardSettings = withRootConfig(ConfigFactory.load())

}
