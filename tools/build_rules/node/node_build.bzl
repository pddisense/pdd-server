# Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

def _get_package_dir(ctx):
  return ctx.label.package

def _get_output_dir(ctx):
  # If it's an external label, output to workspace_root.
  if ctx.label.workspace_root:
    return ctx.configuration.bin_dir.path + '/' + ctx.label.workspace_root + '/' + _get_package_dir(ctx)
  return ctx.configuration.bin_dir.path + '/' + _get_package_dir(ctx)

def _get_input_dir(ctx):
    # If it's an external label, input is in workspace_root.
    if ctx.label.workspace_root:
        return ctx.label.workspace_root + '/' + _get_package_dir(ctx)
    return _get_package_dir(ctx)

def _node_build_impl(ctx):
  inputs = depset(
    items = ctx.files.srcs + [ctx.file._webpack_config, ctx.executable._webpack],
    transitive = [ctx.attr._node_modules[DefaultInfo].files] + [dep[DefaultInfo].files for dep in ctx.attr.deps],
  )

  # Dependencies listed under `deps` are treated differently than those listed under `srcs`,
  # because they are considered as modules of their own. The common node_modules are always
  # registered.
  node_path = ["3rdparty/node_modules"]
  for dep in ctx.attr.deps:
    if dep.label.package not in node_path:
      node_path.append(dep.label.package)

  # '_' prefixed environment variables are private, i.e., not exported to the application.
  env = dict(
    ctx.attr.env,
    _INPUT_DIR = _get_input_dir(ctx),
    _OUTPUT_DIR = _get_output_dir(ctx),
    _ENTRY = ",".join([file.path for file in ctx.files.entry]),
    _PATH = ",".join(node_path),
  )

  outputs = [getattr(ctx.outputs, _get_output_key(entry.label)) for entry in ctx.attr.entry]
  ctx.actions.run_shell(
    command = ctx.executable._webpack.path + " --config " + ctx.file._webpack_config.path,
    inputs = inputs,
    outputs = outputs,
    progress_message = "Building with webpack",
    mnemonic = "WebpackBuild",
    env = env,
  )

def _get_output_key(label):
  basename = label.name.split('/')[-1]
  return basename[:-3]

def _get_outputs(entry):
  return {_get_output_key(label): label.name[:-3] + ".bundle.js" for label in entry}

node_build = rule(
  implementation = _node_build_impl,
  attrs = {
    "srcs": attr.label_list(allow_files=[".js", ".jsx", ".css"]),
    "deps": attr.label_list(providers=[DefaultInfo]),
    "entry": attr.label_list(allow_files=[".js"], allow_empty=False),
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
  outputs = _get_outputs,
)
