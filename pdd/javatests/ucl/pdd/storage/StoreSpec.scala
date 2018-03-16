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

package ucl.pdd.storage

import com.twitter.util.Await
import org.joda.time.{DateTime, Instant}
import org.scalatest.BeforeAndAfterEach
import ucl.testing.UnitSpec

private[storage] abstract class StoreSpec extends UnitSpec with BeforeAndAfterEach {
  protected var storage: Storage = _

  protected def createStorage: Storage

  override def beforeEach(): Unit = {
    storage = createStorage
    Await.ready(storage.startUp())

    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Await.ready(storage.shutDown())
    storage = null

    super.afterEach()
  }

  protected final def now(): Instant = DateTime.now().withMillisOfSecond(0).toInstant
}
