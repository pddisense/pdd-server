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

package ucl.pdd.geocoder

import java.net.InetAddress

import com.google.inject.Singleton
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.twitter.util.{Future, FuturePool, Time}

import scala.util.Try

/**
 * Geocoder using MaxMind's GeoLite Country database. The database itself is embedded as a Java
 * resource.
 *
 * @see https://dev.maxmind.com/geoip/geoip2/geolite2/
 */
@Singleton
final class MaxmindGeocoder extends Geocoder {
  private[this] val reader = {
    val is = classOf[MaxmindGeocoder].getResourceAsStream("GeoLite2-Country.mmdb")
    new DatabaseReader.Builder(is)
      .withCache(new CHMCache())
      .build
  }

  override def geocode(ipAddress: InetAddress): Future[Option[String]] = {
    FuturePool.unboundedPool {
      Try(reader.country(ipAddress))
        .toOption
        .flatMap(resp => Option(resp.getCountry))
        .map(_.getIsoCode)
    }
  }

  override def close(deadline: Time): Future[Unit] = {
    reader.close()
    Future.Done
  }
}
