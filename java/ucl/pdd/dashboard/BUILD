package(default_visibility = ["//visibility:public"])

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
