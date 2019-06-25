require('./check-versions')()

process.env.NODE_ENV = 'production'

var program = require('commander');
var ora = require('ora')
var rm = require('rimraf')
var path = require('path')
var chalk = require('chalk')
var webpack = require('webpack')
var config = require('../config')
var webpackConfig = require('./webpack.prod.conf')

//获取命令行参数
program
  .version('0.0.1')
  .option('-s, --start', 'Start Service')
  .option('-p, --port', 'Service Port')
  .parse(process.argv);
//是否需要启动服务
var isNeedStart = program.start
//服务的端口号
var port = program.port || 9999;

var spinner = ora('building for production...')
spinner.start()

rm(path.join(config.prod.assetsRoot, config.prod.assetsSubDirectory), err => {
  if (err) throw err
  webpack(webpackConfig, function (err, stats) {
    spinner.stop()
    if (err) throw err
    process.stdout.write(stats.toString({
      colors: true,
      modules: false,
      children: false,
      chunks: false,
      chunkModules: false
    }) + '\n\n')

    if (stats.hasErrors()) {
      console.log(chalk.red('  Build failed with errors.\n'))
      process.exit(1)
    }

    console.log(chalk.cyan('  Build complete.\n'))
    console.log(chalk.yellow(
      '  Tip: built files are meant to be served over an HTTP server.\n' +
      '  Opening index.html over file:// won\'t work.\n'
    ))

    //启动测试环境的服务
    if(isNeedStart){
      var express = require('express')
      var app = express()
      app.use(express.static(path.resolve(__dirname , '../dist/prod')));
      app.listen(port)
      console.log(chalk.cyan('启动生产服务，端口号为:'+port))

      var opn = require('opn')
      var uri = 'http://localhost:' + port
      opn(uri)
    }
  })
})
