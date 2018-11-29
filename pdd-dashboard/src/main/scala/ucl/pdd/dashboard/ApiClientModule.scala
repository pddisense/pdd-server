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

import com.twitter.finatra.httpclient.modules.HttpClientModule

object ApiClientModule extends HttpClientModule {
  private[this] val serverFlag = flag("api.server", "localhost:8000", "Address to the API server")
  private[this] val sslHostnameFlag = flag[String]("api.ssl_hostname", "Hostname for SSL")
  private[this] val tokenFlag = flag[String]("api.access_token", "Token to communicate with the API")

  override def dest = serverFlag()

  override def sslHostname: Option[String] = sslHostnameFlag.get

  override def defaultHeaders = Map("Authorization" -> s"Bearer ${tokenFlag()}")
}
