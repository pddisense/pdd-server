package(default_visibility = ["//visibility:public"])

scala_binary(
  name = "server",
  srcs = glob(["*.scala"]),
  deps = [
    "//java/ucl/pdd/api",
    "//java/ucl/pdd/cron",
    "//java/ucl/pdd/config",
    "//java/ucl/pdd/config:annotations",
    "//java/ucl/pdd/jackson",
    "//java/ucl/pdd/slf4j",
    "//java/ucl/pdd/storage",
    "//java/ucl/pdd/storage/install",
    "//3rdparty/jvm/com/twitter:finatra_http",
    "//3rdparty/jvm/ch/qos/logback:logback_core",
    "//3rdparty/jvm/ch/qos/logback:logback_classic",
    "//3rdparty/jvm/io/sentry:sentry_logback",
  ],
  main_class = "ucl.pdd.server.PddServerMain",
)
