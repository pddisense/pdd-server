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

package(default_visibility = ["//visibility:public"])

scala_library(
  name = "cron",
  srcs = glob(["*.scala"]),
  deps = [
    "//pdd/java/ucl/pdd/api",
    "//pdd/java/ucl/pdd/storage",
    "//pdd/java/ucl/pdd/strategy",
    "//pdd/java/ucl/pdd/config:annotations",
    "//pdd/java/ucl/pdd/util",
    "//3rdparty/jvm/com/twitter:util_core",
    "//3rdparty/jvm/com/twitter:inject_core",
    "//3rdparty/jvm/com/twitter:inject_slf4j",
    "//3rdparty/jvm/org/quartz_scheduler:quartz",
    "//3rdparty/jvm/com/github/nscala_time",
  ],
  exports = [
    "//3rdparty/jvm/org/quartz_scheduler:quartz",
  ],
)
