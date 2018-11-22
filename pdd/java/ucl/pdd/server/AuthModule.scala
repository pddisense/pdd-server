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

import com.twitter.inject.TwitterModule

import scala.util.Random

object AuthModule extends TwitterModule {
  private[this] val tokenFlag = flag[String]("api.access_token", "Token used to secure the access to relevant endpoints")

  override def configure(): Unit = {
    bind[String].annotatedWith[AccessToken].toInstance(tokenFlag.get.getOrElse(randomToken(20)))
  }

  private def randomToken(length: Int): String = {
    val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val token = Seq.fill(length)(characters(Random.nextInt(characters.length))).mkString
    info("---------------------------------------------------------")
    info(s"Randomly generated API access token: $token")
    info("---------------------------------------------------------")
    token
  }
}
