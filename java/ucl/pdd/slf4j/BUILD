package(default_visibility = ["//visibility:public"])

scala_library(
  name = "slf4j",
  srcs = glob(["*.scala"]),
  deps = [
    "//3rdparty/jvm/ch/qos/logback:logback_core",
    "//3rdparty/jvm/ch/qos/logback:logback_classic",
    "//3rdparty/jvm/io/sentry:sentry_logback",
    "//3rdparty/jvm/com/twitter:inject_app",
  ],
  resources = ["logback.xml"],
  resource_strip_prefix = "java",
)
