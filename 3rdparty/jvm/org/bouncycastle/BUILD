load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "bcpkix_jdk15on",
    exports = [
        "//external:jar/org/bouncycastle/bcpkix_jdk15on",
        ":bcprov_jdk15on"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "bcprov_jdk15on",
    exports = [
        "//external:jar/org/bouncycastle/bcprov_jdk15on"
    ],
    visibility = [
        "//visibility:public"
    ]
)


