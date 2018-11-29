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

import com.twitter.finagle.http.Request
import ucl.testing.UnitSpec

/**
 * Unit tests for [[Authenticator]].
 */
class AuthenticatorSpec extends UnitSpec {
  behavior of "Authenticator"

  it should "authenticate by password" in {
    var authenticator = Authenticator.newAuthenticator(Some("foo_bar"))
    assertIsUnauthorized(authenticator.authenticate("wrong_foobar"))
    assertIsAuthorized(authenticator.authenticate("foo_bar"))

    authenticator = Authenticator.newAuthenticator(None)
    assertIsAuthorized(authenticator.authenticate("wrong_foobar"))
  }

  it should "authenticate requests by bearer token" in {
    val authenticator = Authenticator.newAuthenticator(Some("foo_bar"))
    val accessToken = authenticator.authenticate("foo_bar").accessToken.get

    val req = Request()
    assertIsUnauthorized(authenticator.authenticate(req))

    req.authorization = "Bearer wrong_token"
    assertIsUnauthorized(authenticator.authenticate(req))

    req.authorization = s"Bearer $accessToken"
    authenticator.authenticate(req) shouldBe Authenticator.Response(authenticated = true, Some(accessToken))
  }

  private def assertIsAuthorized(resp: Authenticator.Response): Unit = {
    resp.authenticated shouldBe true
    resp.accessToken.isDefined shouldBe true
  }

  private def assertIsUnauthorized(resp: Authenticator.Response): Unit = {
    resp.authenticated shouldBe false
    resp.accessToken.isDefined shouldBe false
  }
}
