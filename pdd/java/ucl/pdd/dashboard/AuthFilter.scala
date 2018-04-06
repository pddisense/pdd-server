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
