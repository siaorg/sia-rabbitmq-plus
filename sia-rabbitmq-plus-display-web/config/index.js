// see http://vuejs-templates.github.io/webpack for documentation.
var path = require('path')

module.exports = {
  prod: {
    env: require('./prod.env'),
    index: path.resolve(__dirname, '../dist/prod/index.html'),
    assetsRoot: path.resolve(__dirname, '../dist/prod'),
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    productionSourceMap: false,
    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],
    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report,
    baseApiUrlJs:'src/js/modules/base_url_pro.js'
  },
  test: {
    env: require('./test.env'),
    index: path.resolve(__dirname, '../dist/test/index.html'),
    assetsRoot: path.resolve(__dirname, '../dist/test'),
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    productionSourceMap: true,
    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],
    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report,
    baseApiUrlJs:'src/js/modules/base_url_test.js',

    /*测试服务器相关配置*/
    remoteHost:'10.100.137.35',
    remotePort:'22',
    remotePath:'/app/testtomcat-7.0.57/webapps/ROOT',
    remoteUsername:'newsettle',
    remotePassword:'newsettle@123',

    testWebUrl: 'http://10.100.137.35:8080'
},
  dev: {
    env: require('./dev.env'),
    port: 8080,
    autoOpenBrowser: true,
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    proxyTable: {},
    // CSS Sourcemaps off by default because relative paths are "buggy"
    // with this option, according to the CSS-Loader README
    // (https://github.com/webpack/css-loader#sourcemaps)
    // In our experience, they generally work as expected,
    // just be aware of this issue when enabling this option.
    cssSourceMap: false,
    baseApiUrlJs:'src/js/modules/base_url_test.js'
  }
}
