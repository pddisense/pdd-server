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

import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

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
