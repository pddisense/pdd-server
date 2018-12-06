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

package ucl.pdd.util

import com.twitter.util.Future

/**
 * Trait for services that can be started/stopped asynchronously. This is to be used
 * instead of [[com.google.common.util.concurrent.Service]], as its interface is
 * lighter and integrates better with our usage of Twitter's futures.
 */
trait Service {
  /**
   * Operation to perform when starting the service. By contract, no other method
   * will be called on this object until the returned future completes.
   */
  def startUp(): Future[Unit]

  /**
   * Operation to perform when stopping the service. By contract, no other method
   * will be called on this object once the shutdown has been initiated.
   */
  def shutDown(): Future[Unit]
}
