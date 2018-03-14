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

import com.twitter.finatra.httpclient.modules.HttpClientModule

object ApiHttpClientModule extends HttpClientModule {
  private[this] val destFlag = flag("api.server", "localhost:8000", "Address to the API server")
  private[this] val tokenFlag = flag[String]("api.token", "Token to communicate with the API")

  override def dest = destFlag()

  override def defaultHeaders = Map("Authorization" -> s"Bearer ${tokenFlag()}")
}
