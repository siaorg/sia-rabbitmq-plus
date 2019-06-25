var webpack = require('webpack')
var path = require('path')
var fs = require('fs')
var utils = require('./utils')
var config = require('../config')
var vueLoaderConfig = require('./vue-loader.conf')

function resolve (dir) {
  return path.join(__dirname, '..', dir)
}

var publicPath = process.env.NODE_ENV === 'production' ? config.prod.assetsPublicPath
  : (process.env.NODE_ENV === 'testing' ? config.test.assetsPublicPath : config.dev.assetsPublicPath)

var baseApiUrlJs = process.env.NODE_ENV === 'production' ? config.prod.baseApiUrlJs
  : (process.env.NODE_ENV === 'testing' ? config.test.baseApiUrlJs : config.dev.baseApiUrlJs)

module.exports = {
  entry: {
    app: './src/main.js'
  },
  output: {
    //path: config.build.assetsRoot,
    path: config.prod.assetsRoot,
    filename: '[name].js',
    /*publicPath: process.env.NODE_ENV === 'production'
      ? config.prod.assetsPublicPath
      : config.dev.assetsPublicPath*/
    publicPath: publicPath
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      '@': resolve('src')
    },
    symlinks: false
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: vueLoaderConfig
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        include: [resolve('src'), resolve('test')]
      },
      {
        test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('img/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('media/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(woff2?|eot|ttf|otf|ttc)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('fonts/[name].[hash:7].[ext]')
        }
      }
    ]
  },
  plugins: [
    new webpack.ProvidePlugin({ //自动加载模块
      Vue: ['vue/dist/vue.esm.js', 'default'],
      VueRouter: 'vue-router/dist/vue-router.js',
      $: 'jquery',
      //baseUrl:[resolve('src/js/modules/base_url.js'), 'default']
      baseUrl:[resolve(baseApiUrlJs), 'default']
    })
  ]
}
