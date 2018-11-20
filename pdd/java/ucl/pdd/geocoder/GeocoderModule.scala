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

import com.twitter.inject.{Injector, TwitterModule}

/**
 * Guice module configuring the geocoder service.
 */
object GeocoderModule extends TwitterModule {
  private[this] val typeFlag = flag(
    "geocoder",
    "null",
    "Which geocoder to use to map IP addresses to a country code. Valid values are: 'null', 'maxmind'.")

  override def configure(): Unit = {
    typeFlag() match {
      case "maxmind" => bind[Geocoder].to[MaxmindGeocoder]
      case "null" => bind[Geocoder].toInstance(NullGeocoder)
      case invalid => throw new IllegalArgumentException(s"Invalid geocoder type: $invalid")
    }
  }

  override def singletonShutdown(injector: Injector): Unit = {
    injector.instance[Geocoder].close()
  }
}
