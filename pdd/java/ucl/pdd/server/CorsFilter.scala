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

import com.google.inject.Singleton
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

@Singleton
private[server] final class CorsFilter extends SimpleFilter[Request, Response] {
  private[this] val cors = {
    val allowsOrigin = { origin: String => Some(origin) }
    val allowsMethods = { method: String => Some(Seq("GET", "POST", "PUT", "PATCH", "DELETE")) }
    val allowsHeaders = { headers: Seq[String] => Some(headers) }
    val policy = Cors.Policy(allowsOrigin, allowsMethods, allowsHeaders)
    new Cors.HttpFilter(policy)
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    cors.apply(request, service)
  }
}
