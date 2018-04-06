/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const path = require('path');
const webpack = require('webpack');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');

const context = path.resolve(process.env.PWD, process.env._INPUT_DIR);
const output = path.resolve(process.env.PWD, process.env._OUTPUT_DIR);
const modules = process.env._PATH.split(',').map(dir => path.resolve(process.env.PWD, dir));
const isHeadless = process.env._HEADLESS === 'true';

const entry = {};
process.env._ENTRY.split(',').forEach(file => {
  const basename = path.relative(context, file);
  // Only .js files are allowed as entry points, per Bazel configuration.
  const name = basename.substr(0, basename.length - 3);
  entry[name] = './' + basename;
});

// '_' prefixed environment variables are private, i.e., not exported to the application.
const env = [] = Object.keys(process.env).filter(key => key[0] !== '_');

module.exports = {
  context: context,
  entry: entry,
  output: {
    path: output,
    filename: '[name].bundle.js',
  },
  target: isHeadless ? 'node' : 'web',
  module: {
    loaders: [
      {
        test: /.jsx?$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: ['airbnb'],
          plugins: ['transform-decorators-legacy',],
        }
      },
      {
        test: /\.css$/,
        loader: isHeadless ? 'null-loader' : 'style-loader!css-loader',
      },
      {
        test: /\.(woff|woff2|eot|ttf|png|jpg|jpeg|svg)$/,
        loader: isHeadless ? 'null-loader' : 'url-loader',
      },
    ],
  },
  plugins: [
    new webpack.EnvironmentPlugin(env),
  ],
  resolveLoader: {
    modules: modules,
  },
  resolve: {
    modules: modules,
    extensions: ['.js', '.json', '.jsx'],
  },
};

if (process.env.NODE_ENV === 'production') {
  module.exports.plugins.push(new UglifyJsPlugin());
}
