package(default_visibility = ["//visibility:public"])

scala_library(
  name = "mysql",
  srcs = glob(["*.scala"]),
  deps = [
    "//java/ucl/pdd/api",
    "//java/ucl/pdd/storage",
    "//3rdparty/jvm/com/twitter:util_core",
    "//3rdparty/jvm/com/twitter:inject_slf4j",
    "//3rdparty/jvm/com/twitter:finagle_mysql",
    "//3rdparty/jvm/com/github/nscala_time",
  ],
  exports = [
    "//3rdparty/jvm/com/twitter:finagle_mysql",
  ]
)
