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

package ucl.pdd.service

import java.net.InetAddress

import com.twitter.util.{Closable, Future}

/**
 * A geocoder is used to translate IP address into location information.
 */
trait Geocoder extends Closable {
  /**
   * Return the country code associated with a given IP address.
   *
   * @param ipAddress IP address to locate.
   * @return An ISO 3166-1 alpha-2 country code.
   */
  def geocode(ipAddress: InetAddress): Future[Option[String]]
}
