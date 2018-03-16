/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ucl.pdd.server

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.modules.StatsReceiverModule
import ucl.pdd.config.ConfigModule
import ucl.pdd.cron.CronModule
import ucl.pdd.jackson.PddJacksonModule
import ucl.pdd.slf4j.LoggingConfigurator
import ucl.pdd.storage.install.StorageModule
import ucl.pdd.strategy.StrategyModule

object PddServerMain extends PddServer

class PddServer extends HttpServer with LoggingConfigurator {
  override def modules = Seq(ConfigModule, StorageModule, CronModule, StrategyModule, StatsReceiverModule)

  override def defaultFinatraHttpPort: String = ":8000"

  override def jacksonModule = PddJacksonModule

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[AuthFilter, AdminController]
      .add[CorsFilter, PublicController]
  }
}
