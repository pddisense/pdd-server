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

package ucl.pdd.dashboard

import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.httpclient.HttpClient

/**
 * The API controller exposes endpoints locally to the application. It mainly forwards calls to the
 * API server.
 *
 * @param httpClient HTTP client to the API server.
 */
@Singleton
final class ApiController @Inject()(httpClient: HttpClient) extends Controller {
  get("/api/campaigns") { req: Request => httpClient.execute(req) }
  post("/api/campaigns") { req: Request => httpClient.execute(req) }
  get("/api/campaigns/:name") { req: Request => httpClient.execute(req) }
  put("/api/campaigns/:name") { req: Request => httpClient.execute(req) }
  delete("/api/campaigns/:name") { req: Request => httpClient.execute(req) }

  get("/api/aggregations") { req: Request => httpClient.execute(req) }
  get("/api/aggregations/:name") { req: Request => httpClient.execute(req) }

  get("/api/clients") { req: Request => httpClient.execute(req) }
  get("/api/clients/:name") { req: Request => httpClient.execute(req) }
}
