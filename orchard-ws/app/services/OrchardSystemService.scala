/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package services

import javax.inject._

import akka.actor.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.adapter._
import play.api.Configuration

import com.salesforce.mce.orchard.OrchardSettings
import com.salesforce.mce.orchard.util.OrchardBehavior
import com.salesforce.mce.orchard.system.OrchardSystem

@Singleton
class OrchardSystemService @Inject() (
  actorSystem: ActorSystem,
  databaseService: DatabaseService,
  conf: Configuration
) {

  private val orchardSettings = OrchardSettings.withRootConfig(conf.underlying)

  private val supervisedOrchardSystem: Behavior[OrchardSystem.Msg] = {
    OrchardBehavior.supervise[OrchardSystem.Msg](
      OrchardSystem.apply(databaseService.orchardDB, orchardSettings),
      orchardSettings
    )
  }

  val orchard: ActorRef[OrchardSystem.Msg] =
    actorSystem.spawn(supervisedOrchardSystem, "orchard-system")
}
