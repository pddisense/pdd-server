load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "accessors_smart",
    exports = [
        "//3rdparty/jvm/org/ow2/asm:asm",
        "//external:jar/net/minidev/accessors_smart"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "json_smart",
    exports = [
        "//external:jar/net/minidev/json_smart",
        ":accessors_smart"
    ],
    visibility = [
        "//visibility:public"
    ]
)


