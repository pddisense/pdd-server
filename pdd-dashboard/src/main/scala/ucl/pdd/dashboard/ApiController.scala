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
  get("/api/:*") { req: Request => httpClient.execute(req) }
  post("/api/:*") { req: Request => httpClient.execute(req) }
  put("/api/:*") { req: Request => httpClient.execute(req) }
  patch("/api/:*") { req: Request => httpClient.execute(req) }
  delete("/api/:*") { req: Request => httpClient.execute(req) }
}
