const path = require('path');
const webpack = require('webpack');

const context = path.resolve(process.env.PWD, process.env._INPUT_DIR);
const output = path.resolve(process.env.PWD, process.env._OUTPUT_DIR);
const modules = process.env._PATH.split(',').map(dir => path.resolve(process.env.PWD, dir));

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
  module: {
    loaders: [
      {
        test: /.jsx?$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: ['airbnb', 'flow',],
          plugins: ['transform-decorators-legacy',],
        }
      },
      {
        test: /\.css$/,
        loader: 'style-loader!css-loader',
      },
      {
        test: /\.(woff|woff2|eot|ttf|png|jpg|jpeg|svg)$/,
        loader: 'url-loader',
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
