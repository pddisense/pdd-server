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
