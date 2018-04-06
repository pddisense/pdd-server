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
