def _pkg_zip_impl(ctx):
  print(ctx.files.srcs)
  inputs = depset(
    items = ctx.files.srcs,
    transitive = [dep[DefaultInfo].files for dep in ctx.attr.srcs],
  )
  ctx.actions.run_shell(
    command = "zip -r " + ctx.outputs.out.path + " " + " ".join([dep.path for dep in inputs]),
    inputs = inputs,
    outputs = [ctx.outputs.out],
    progress_message = "Creating .zip archive",
    mnemonic = "Zip",
  )
  pass

pkg_zip = rule(
  implementation = _pkg_zip_impl,
  attrs = {
    "srcs": attr.label_list(allow_files=True),
  },
  outputs = {
    "out": "%{name}.zip",
  },
)
