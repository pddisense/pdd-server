/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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

object PddServerMain extends PddServer

class PddServer extends HttpServer with LoggingConfigurator {
  override def modules = Seq(ConfigModule, StorageModule, CronModule, StatsReceiverModule)

  override def defaultFinatraHttpPort: String = ":8000"

  override def jacksonModule = PddJacksonModule

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .filter[CorsFilter]
      .add[CampaignsController]
      .add[ClientsController]
      .add[SketchController]
  }
}
