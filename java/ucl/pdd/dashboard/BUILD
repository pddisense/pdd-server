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

scala_binary(
  name = "dashboard",
  srcs = glob(["*.scala"]),
  deps = [
    "//java/ucl/pdd/api",
    "//java/ucl/pdd/jackson",
    "//java/ucl/pdd/slf4j",
    "//3rdparty/jvm/com/twitter:finatra_http",
    "//3rdparty/jvm/com/twitter:finatra_httpclient",
    "//3rdparty/jvm/ch/qos/logback:logback_core",
    "//3rdparty/jvm/ch/qos/logback:logback_classic",
    "//3rdparty/jvm/io/sentry:sentry_logback",
  ],
  resources = [
    "//node/ucl/pdd/dashboard:index.bundle.js",
    "//node/ucl/pdd/dashboard:index.html",
    "//node/ucl/pdd/dashboard:robots.txt",
  ],
  resource_strip_prefix = "node/ucl/pdd/dashboard",
  main_class = "ucl.pdd.dashboard.PddDashboardMain",
)
