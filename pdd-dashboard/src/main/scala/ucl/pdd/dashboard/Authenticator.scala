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

import java.security.spec.ECGenParameterSpec
import java.security.{KeyPair, KeyPairGenerator, SecureRandom, Security}

import com.twitter.finagle.http.Request
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.util.{Failure, Success}

/**
 * Authentication service, handling the JWT-based authentication.
 *
 * @param keyPair             Cryptographic key pair used to create the JWT tokens.
 * @param maybeMasterPassword Master password that should be sent as a header. If left empty,
 *                            no authentication will be performed and calls will be allowed.
 */
final class Authenticator(keyPair: KeyPair, maybeMasterPassword: Option[String]) {

  import Authenticator._

  /**
   * Check whether the JWT provided by a request as a bearer auth header is valid.
   *
   * @param request Request to check.
   */
  def authenticate(request: Request): Response = request.authorization match {
    case Some(header) if header.startsWith("Bearer ") =>
      val accessToken = header.drop(7)
      Jwt.decode(accessToken, keyPair.getPublic, Seq(JwtAlgorithm.ES512)) match {
        case Success(_) => Response(authenticated = true, Some(accessToken))
        case Failure(_) => Unauthorized
      }
    case None => Unauthorized
  }

  /**
   * Check whether the password is valid against the master password, and generate
   * a JWT if required.
   *
   * @param password Password to check.
   * @return
   */
  def authenticate(password: String): Response = {
    val isPasswordValid = maybeMasterPassword.forall(_ == password)
    if (isPasswordValid) {
      val accessToken = Jwt.encode("""{}""", keyPair.getPrivate, JwtAlgorithm.ES512)
      Response(authenticated = true, Some(accessToken))
    } else {
      Unauthorized
    }
  }
}

object Authenticator {

  /**
   * Authentication response.
   *
   * @param authenticated Whether the authentication was successful.
   * @param accessToken   Access token to use in further requests. It will be defined if the
   *                      authentication was successful.
   */
  case class Response(authenticated: Boolean, accessToken: Option[String])

  /**
   * Unauthorized authentication response.
   */
  private val Unauthorized = Response(authenticated = false, None)

  /**
   * Return a new authenticator with a freshly generated cryptographic key pair.
   *
   * @param masterPassword Master password that should be sent as a header. If left empty,
   *                       no authentication will be performed and calls will be allowed.
   */
  def newAuthenticator(masterPassword: Option[String]): Authenticator = {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider)
    }
    val generator = KeyPairGenerator.getInstance("ECDSA", "BC")
    val params = new ECGenParameterSpec("P-521")
    generator.initialize(params, new SecureRandom)
    val keyPair = generator.generateKeyPair()

    new Authenticator(keyPair, masterPassword)
  }
}