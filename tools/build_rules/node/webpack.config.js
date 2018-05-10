const path = require('path');
const webpack = require('webpack');

const context = path.resolve(process.env.PWD, process.env.PACKAGE);
const output = path.resolve(process.env.PWD, process.env.OUTDIR);
const modules = process.env.NODE_PATH.split(',').map(dir => path.resolve(process.env.PWD, dir));

module.exports = {
  context: context,
  entry: {
    options: './app/options.js',
    background: './app/background.js',
  },
  output: {
    path: output,
    filename: '[name].js',
  },
  module: {
    loaders: [
      {
        test: /.jsx?$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: [
            'airbnb',
            'flow',
          ],
          plugins: [
            'transform-decorators-legacy',
          ],
        }
      },
      {
        test: /\.css$/,
        loader: 'style-loader!css-loader',
      },
      {
        test: /\.(woff|woff2|eot|ttf|png|jpg|jpeg|svg)$/,
        loader: 'url-loader',
        options: {
          limit: 8000,
        }
      },
    ]
  },
  plugins: [
    new webpack.EnvironmentPlugin(['API_URL', 'SENTRY_DSN', 'NODE_ENV']),
  ],
  resolveLoader: {
    modules: modules,
  },
  resolve: {
    modules: modules,
  },
};
