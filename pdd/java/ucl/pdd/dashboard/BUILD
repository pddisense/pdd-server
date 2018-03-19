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
    "//pdd/java/ucl/pdd/api",
    "//pdd/java/ucl/pdd/jackson",
    "//pdd/java/ucl/pdd/observability/logging",
    "//3rdparty/jvm/com/twitter:finatra_http",
    "//3rdparty/jvm/com/twitter:finatra_httpclient",
  ],
  runtime_deps = [
    "//pdd/java/ucl/pdd/observability/datadog",
  ],
  resources = [
    "//pdd/node/ucl/pdd/dashboard:index.bundle.js",
    "//pdd/node/ucl/pdd/dashboard:index.html",
    "//pdd/node/ucl/pdd/dashboard:robots.txt",
  ],
  resource_strip_prefix = "pdd/node/ucl/pdd/dashboard",
  main_class = "ucl.pdd.dashboard.PddDashboardMain",
)
