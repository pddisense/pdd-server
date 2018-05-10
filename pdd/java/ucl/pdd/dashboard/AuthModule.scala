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

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import org.bouncycastle.jce.provider.BouncyCastleProvider

object AuthModule extends TwitterModule {
  private[this] val masterPassword = flag[String]("master_password", "Master password securing the access to the app")

  override def configure(): Unit = {
    bind[Option[String]].annotatedWith[MasterPassword].toInstance(masterPassword.get)
    if (masterPassword.isDefined) {
      logger.info("Authentication is enabled on the API")
    } else {
      logger.warn("No authentication is configured on the API!")
    }
  }

  @Provides
  @Singleton
  def providesKeyPair: KeyPair = {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider)
    }
    val generator = KeyPairGenerator.getInstance("ECDSA", "BC")
    val params = new ECGenParameterSpec("P-521")
    generator.initialize(params, new SecureRandom)
    generator.generateKeyPair()
  }
}
