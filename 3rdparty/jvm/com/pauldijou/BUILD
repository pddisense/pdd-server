load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "jwt_core",
    exports = [
        "//3rdparty/jvm/org/bouncycastle:bcpkix_jdk15on",
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":jwt_core_impl_2_11"
    ],
    jars = [
        "//external:jar/com/pauldijou/jwt_core_2_11"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "jwt_core_impl_2_11",
    exports = [
        "//external:jar/com/pauldijou/jwt_core_impl_2_11"
    ],
    visibility = [
        "//visibility:public"
    ]
)


