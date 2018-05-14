# PDD is a platform for privacy-preserving Web searches collection.
# Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
#
# PDD is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# PDD is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with PDD.  If not, see <http://www.gnu.org/licenses/>.

scala_test(
  name = "service",
  srcs = glob(["*.scala"]),
  size = "small",
  deps = [
    "//pdd/java/ucl/pdd/service",
    "//pdd/java/ucl/pdd/domain",
    "//pdd/java/ucl/pdd/strategy",
    "//pdd/java/ucl/pdd/storage",
    "//pdd/java/ucl/pdd/storage/memory",
    "//pdd/javatests/ucl/testing",
  ]
)
