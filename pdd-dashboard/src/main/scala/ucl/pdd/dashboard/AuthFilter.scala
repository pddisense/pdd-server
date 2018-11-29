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

import java.security.KeyPair

import com.google.inject.{Inject, Singleton}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.util.{Failure, Success}

@Singleton
final class AuthFilter @Inject()(keyPair: KeyPair, @MasterPassword masterPassword: Option[String])
  extends SimpleFilter[Request, Response] {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    if (masterPassword.isEmpty || authenticate(request)) {
      service(request)
    } else {
      Future.value(Response(Status.Unauthorized))
    }
  }

  private def authenticate(request: Request): Boolean = {
    request.authorization match {
      case Some(header) if header.startsWith("Bearer ") =>
        Jwt.decode(header.drop(7), keyPair.getPublic, Seq(JwtAlgorithm.ES512)) match {
          case Success(_) => true
          case Failure(e) =>
            e.printStackTrace()
            false
        }
      case None =>
        println("no token")
        false
    }
  }
}
