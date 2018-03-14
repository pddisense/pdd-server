package(default_visibility = ["//visibility:public"])

scala_library(
  name = "api",
  srcs = glob(["*.scala"]),
  deps = [
    "//3rdparty/jvm/com/github/nscala_time",
    "//3rdparty/jvm/com/twitter:finatra_jackson",
  ],
  exports = [
    "//3rdparty/jvm/com/github/nscala_time",
    "//3rdparty/jvm/com/twitter:finatra_jackson",
  ]
)
