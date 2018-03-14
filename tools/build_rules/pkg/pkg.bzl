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

def _pkg_zip_impl(ctx):
  ctx.actions.run_shell(
    command = "zip -r " + ctx.outputs.out.path + " " + " ".join([dep.path for dep in ctx.files.srcs]),
    inputs = ctx.files.srcs,
    outputs = [ctx.outputs.out],
    progress_message = "Creating .zip archive",
    mnemonic = "Zip",
  )

pkg_zip = rule(
  implementation = _pkg_zip_impl,
  attrs = {
    "srcs": attr.label_list(allow_files=True),
  },
  outputs = {
    "out": "%{name}.zip",
  },
)
