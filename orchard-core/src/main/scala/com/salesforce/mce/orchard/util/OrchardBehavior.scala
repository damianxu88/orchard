package com.salesforce.mce.orchard.util

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

import com.salesforce.mce.orchard.OrchardSettings

class OrchardBehavior[T] (behavior: Behavior[T]) {

  def supervise(orchardSettings: OrchardSettings = OrchardSettings()): Behavior[T] = {
    orchardSettings.restartBackoffParams match {
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

}
