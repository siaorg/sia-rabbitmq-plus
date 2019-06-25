var utils = require('./utils')
var config = require('../config')
//var isProduction = process.env.NODE_ENV === 'production'
var isExtract = process.env.NODE_ENV === 'production' || process.env.NODE_ENV === 'testing'
var sourceMap = process.env.NODE_ENV === 'production'? config.prod.productionSourceMap
                : (process.env.NODE_ENV === 'testing'? config.test.productionSourceMap : config.dev.cssSourceMap)

module.exports = {
  loaders: utils.cssLoaders({
    /*sourceMap: isProduction
      ? config.build.productionSourceMap
      : config.dev.cssSourceMap,*/
    sourceMap: sourceMap,
    //extract: isProduction,
    extract: isExtract,
    scss: 'style!css!sass'
  }),
  transformToRequire: {
    video: 'src',
    source: 'src',
    img: 'src',
    image: 'xlink:href'
  }
}
