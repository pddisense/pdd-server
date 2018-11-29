/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

const webpack = require('webpack');
const path = require('path');

const outputPath = path.resolve(__dirname, 'pdd-dashboard/src/resources');
const config = {
  entry: './pdd-dashboard/src/main/node/index.js',
  output: {
    path: outputPath,
    filename: '[name].bundle.js',
  },
  module: {
    rules: [
      {
        test: /.jsx?$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.css$/,
        loader: 'style-loader!css-loader',
      },
      {
        test: /\.(woff|woff2|eot|ttf|png|jpg|jpeg|svg)$/,
        loader: 'file-loader?name=[name].[ext]',
      },
    ],
  },
  plugins: [],
  resolve: {
    extensions: ['.js', '.json', '.jsx'],
  },
};

module.exports = (env, args) => {
  env = env || {};
  if (args.mode === 'development') {
    config.mode = 'development';
  } else {
    env.SENTRY_DSN = 'https://3b359301775f40f992827f8d685cbcb4@sentry.io/302349';
    config.mode = 'production';
  }

  config.plugins.push(new webpack.EnvironmentPlugin(env));

  return config;
}
