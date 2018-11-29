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
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.util.{Failure, Success}

@Singleton
final class AuthController @Inject()(keyPair: KeyPair, @MasterPassword masterPassword: Option[String])
  extends Controller {

  get("/auth") { req: Request =>
    if (masterPassword.isEmpty) {
      AuthResponse(authenticated = true, None)
    } else {
      req.authorization match {
        case Some(header) if header.startsWith("Bearer ") =>
          val accessToken = header.drop(7)
          Jwt.decode(accessToken, keyPair.getPublic, Seq(JwtAlgorithm.ES512)) match {
            case Success(_) => AuthResponse(authenticated = true, Some(accessToken))
            case Failure(_) => AuthResponse(authenticated = false, None)
          }
        case None => AuthResponse(authenticated = false, None)
      }
    }
  }

  post("/auth") { req: AuthRequest =>
    masterPassword match {
      case None => AuthResponse(authenticated = true, None)
      case Some(password) =>
        if (req.password == password) {
          val accessToken = Jwt.encode("""{}""", keyPair.getPrivate, JwtAlgorithm.ES512)
          AuthResponse(authenticated = true, Some(accessToken))
        } else {
          AuthResponse(authenticated = false, None)
        }
    }
  }
}

case class AuthRequest(password: String)

case class AuthResponse(authenticated: Boolean, accessToken: Option[String])
