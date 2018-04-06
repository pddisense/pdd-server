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
