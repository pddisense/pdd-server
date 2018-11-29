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

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule

/**
 * Guice module providing authentication services.
 */
object AuthModule extends TwitterModule {
  private val masterPasswordFlag = flag[String]("master_password", "Master password securing the access to the app")

  @Provides
  @Singleton
  def providesAuthenticator(): Authenticator = {
    // The cryptographic key pair is generated on-the-fly and not persisted. It means
    // that JWT will not be valid across server restarts. This will oblige users to
    // enter again their passwords, but this may also be used as an emergency measure
    // to force logout all users.
    Authenticator.newAuthenticator(masterPasswordFlag.get)
  }
}
