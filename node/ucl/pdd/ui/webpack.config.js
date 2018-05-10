const path = require('path');
const webpack = require('webpack');

const context = path.resolve(process.env.PWD, process.env.PACKAGE);
const output = path.resolve(process.env.PWD, process.env.OUTDIR);
const modules = [path.resolve(process.env.PWD, 'third_party/node/node_modules')];

module.exports = {
  context: context,
  entry: ['airbnb-browser-shims', './index.js'],
  output: {
    path: output,
    filename: 'bundle.js',
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
    new webpack.EnvironmentPlugin(['DEBUG', 'API_URL', 'SENTRY_DSN', 'NODE_ENV']),
  ],
  resolveLoader: {
    modules: modules,
  },
  resolve: {
    extensions: ['.js', '.jsx'],
    modules: modules,
  },
};
