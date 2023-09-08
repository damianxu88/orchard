package com.salesforce.mce.orchard.util

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

import com.salesforce.mce.orchard.OrchardSettings

object OrchardBehavior {

  def supervise[T](
    behavior: Behavior[T],
    orchardSettings: OrchardSettings = OrchardSettings()
  ): Behavior[T] = orchardSettings.restartBackoffParams match {
    case Some(restartBackoffParams) =>
      Behaviors
        .supervise(behavior)
        .onFailure(restartBackoffParams.supervisorStrategy)
    case _ =>
      Behaviors
        .supervise(behavior)
        .onFailure(SupervisorStrategy.restart)
  }

}
