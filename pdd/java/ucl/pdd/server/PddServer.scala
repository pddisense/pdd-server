/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.server

import com.google.inject.Module
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import ucl.pdd.geocoder.GeocoderModule
import ucl.pdd.jackson.PddJacksonModule
import ucl.pdd.logging.LoggingConfigurator
import ucl.pdd.metrics.MetricsModule
import ucl.pdd.service.ServiceModule
import ucl.pdd.storage.install.StorageModule
import ucl.pdd.strategy.StrategyModule

object PddServerMain extends PddServer

class PddServer extends HttpServer with LoggingConfigurator {
  override def modules = Seq(AuthModule, StorageModule, GeocoderModule, ServiceModule, StrategyModule)

  override def defaultHttpPort: String = ":8000"

  override def jacksonModule = PddJacksonModule

  override def statsReceiverModule: Module = MetricsModule

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[AuthFilter, PrivateController]
      .add[CorsFilter, PublicController]
  }
}
