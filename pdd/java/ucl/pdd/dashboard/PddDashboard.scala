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

package ucl.pdd.dashboard

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import ucl.pdd.jackson.PddJacksonModule
import ucl.pdd.logging.LoggingConfigurator
import ucl.pdd.metrics.MetricsModule

object PddDashboardMain extends PddDashboard

class PddDashboard extends HttpServer with LoggingConfigurator {
  override def modules = Seq(ApiClientModule, AuthModule)

  override def jacksonModule = PddJacksonModule

  override def statsReceiverModule = MetricsModule

  override def defaultFinatraHttpPort: String = ":8001"

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[AuthFilter, ApiController]
      .add[AuthController]
      .add[UiController]
  }
}
