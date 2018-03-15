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

import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import ucl.pdd.config.AccessToken

/**
 * Filter handling authentication via a Bearer Token. It looks for a token both in the standard
 * `Authorization` header and in a `access_token` query parameter.
 *
 * @param accessToken Expected access token securing the access to the API.
 */
@Singleton
final class AuthFilter @Inject()(@AccessToken accessToken: String)
  extends SimpleFilter[Request, Response] {

  private[this] val unauthorizedResponse = {
    val resp = Response(Status.Unauthorized)
    resp.wwwAuthenticate = "Bearer"
    resp.contentString = """{"reason":"Unauthorized"}"""
    resp
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    getToken(request) match {
      case Some(token) if token == accessToken => service(request)
      case _ => Future.value(unauthorizedResponse)
    }
  }

  private def getToken(request: Request): Option[String] = {
    request
      .authorization
      .filter(_.startsWith("Bearer "))
      .map(_.drop(7))
      .orElse(request.params.get("access_token"))
  }
}
