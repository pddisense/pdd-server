def _node_build_impl(ctx):
  inputs = depset(
    items = ctx.files.srcs + ctx.files.data + [ctx.file.config, ctx.executable._webpack],
    transitive = [ctx.attr._node_modules[DefaultInfo].files] + [dep[DefaultInfo].files for dep in ctx.attr.deps],
  )
  node_path = ["3rdparty/node_modules"] + [dep.label.package for dep in ctx.attr.deps];
  dist_dir = ctx.actions.declare_directory("dist")

  mkdirs = []
  commands = []
  commands.append(ctx.executable._webpack.path + " --config " + ctx.file.config.path + " --json > " + ctx.outputs.stats.path)
  for file in ctx.files.data:
    relative_path = file.path[len(ctx.label.package) + 1:]
    if relative_path.rfind("/") > -1:
      relative_dir = relative_path[:-len(file.basename)]
      if not relative_dir in mkdirs:
        commands.append("mkdir -p " + dist_dir.path + "/" + relative_dir)
        mkdirs.append(relative_dir)
    commands.append("cp " + file.path + " " + dist_dir.path + "/" + relative_path)

  env = dict(
    ctx.attr.env,
    OUTDIR = dist_dir.path,
    PACKAGE = ctx.label.package,
    NODE_PATH = ",".join(node_path),
  )
  ctx.actions.run_shell(
    command = " && ".join(commands),
    inputs = inputs,
    outputs = [dist_dir, ctx.outputs.stats],
    progress_message = "Building with webpack",
    mnemonic = "WebpackBuild",
    env = env,
  )

  # This may not be supported on Windows, as we call directly the tar executable.
  # Ideally we would like to rename the dist/ prefix into [name], but the --transform option
  # is not supported on macOS.
  ctx.actions.run(
    executable = "tar",
    arguments = ["-czhf", ctx.outputs.targz.path, "-C", dist_dir.dirname, dist_dir.basename],
    inputs = [dist_dir],
    outputs = [ctx.outputs.targz],
    progress_message = "Creating deployable .tar.gz archive",
    mnemonic = "TarGz",
  )

  ctx.actions.run_shell(
      command = "cd " + dist_dir.dirname + " && zip -r " + ctx.outputs.zip.basename + " " + dist_dir.basename,
      inputs = [dist_dir],
      outputs = [ctx.outputs.zip],
      progress_message = "Creating deployable .zip archive",
      mnemonic = "Zip",
    )

  return DefaultInfo(files=depset([ctx.outputs.stats]))

node_build = rule(
  implementation = _node_build_impl,
  attrs = {
    #"entry_points": attr.label_list(allow_files=True, allow_empty=False),
    "srcs": attr.label_list(allow_files=[".js", ".jsx", ".css"]),
    "data": attr.label_list(allow_files=True),
    "deps": attr.label_list(providers=[DefaultInfo]),
    "config": attr.label(allow_single_file=True),
    "env": attr.string_dict(),
    "_node_modules": attr.label(default="//3rdparty:node_modules"),
    "_webpack": attr.label(
      default="//tools/bin:webpack",
      executable=True,
      cfg="host"
    ),
    "_webpack_config": attr.label(
      default="//tools/build_rules/node:webpack.config.js",
      allow_files=True,
      single_file=True,
    ),
  },
  outputs = {
    "stats": "%{name}.json",
    "targz": "%{name}_deploy.tar.gz",
    "zip": "%{name}_deploy.zip",
  }
)
