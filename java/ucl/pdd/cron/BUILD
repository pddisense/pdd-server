package(default_visibility = ["//visibility:public"])

scala_library(
  name = "cron",
  srcs = glob(["*.scala"]),
  deps = [
    "//java/ucl/pdd/api",
    "//java/ucl/pdd/storage",
    "//java/ucl/pdd/strategy",
    "//java/ucl/pdd/config:annotations",
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
