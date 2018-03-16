# Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

scala_test(
  name = "install",
  srcs = glob(["*.scala"]),
  size = "small",
  deps = [
    "//pdd/java/ucl/pdd/storage",
    "//pdd/java/ucl/pdd/storage/install",
    "//pdd/java/ucl/pdd/storage/memory",
    "//pdd/java/ucl/pdd/storage/mysql",
    "//pdd/javatests/ucl/testing",
    "//pdd/javatests/com/twitter/inject",
     "//3rdparty/jvm/com/twitter:util_stats",
     "//3rdparty/jvm/com/twitter:inject_core",
  ]
)
