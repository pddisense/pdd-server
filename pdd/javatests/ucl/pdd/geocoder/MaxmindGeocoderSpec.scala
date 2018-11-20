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

import com.twitter.util.Await
import ucl.testing.UnitSpec

/**
 * Unit tests for [[MaxmindGeocoder]].
 */
class MaxmindGeocoderSpec extends UnitSpec {
  behavior of "MaxmindGeocoder"

  it should "geocode IP addresses" in {
    val geocoder = new MaxmindGeocoder
    Await.result(geocoder.geocode(InetAddress.getByName("64.253.63.128"))) shouldBe Some("GB")
    Await.result(geocoder.geocode(InetAddress.getByName("92.222.0.128"))) shouldBe Some("FR")
    Await.result(geocoder.geocode(InetAddress.getByName("192.168.0.1"))) shouldBe None
    Await.result(geocoder.close())
  }
}
