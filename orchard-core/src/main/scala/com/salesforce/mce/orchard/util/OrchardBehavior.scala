/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.orchard.util

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

import com.salesforce.mce.orchard.OrchardSettings

class OrchardBehavior[T] (behavior: Behavior[T], orchardSettings: OrchardSettings) {

  def supervise(): Behavior[T] = {
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


object OrchardBehavior {

  def apply[T](behavior: Behavior[T]): OrchardBehavior[T] = {
    new OrchardBehavior[T](behavior, OrchardSettings())
  }

}