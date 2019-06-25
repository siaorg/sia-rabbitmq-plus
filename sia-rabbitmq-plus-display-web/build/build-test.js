require('./check-versions')()

process.env.NODE_ENV = 'testing'

var program = require('commander');
var ora = require('ora')
var rm = require('rimraf')
var path = require('path')
var chalk = require('chalk')
var webpack = require('webpack')
var config = require('../config')
var webpackConfig = require('./webpack.test.conf')

//获取命令行参数
program
  .version('0.0.1')
  .option('-s, --start', 'Start Service')
  .option('-p, --port', 'Service Port')
  .option('-pub, --publish', 'Publish Test Codes')
  .parse(process.argv);
//是否需要启动服务
var isNeedStart = program.start
//是否需要发布到测试服务器
var isNeedPublish = program.publish
//服务的端口号
var port = program.port || 8888

var spinner = ora('building for testing...')
spinner.start()

rm(path.join(config.test.assetsRoot, config.test.assetsSubDirectory), err => {
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

    console.log(chalk.cyan('  Build testing complete.\n'))
    console.log(chalk.yellow(
      '  Tip: built files are meant to be served over an HTTP server.\n' +
      '  Opening index.html over file:// won\'t work.\n'
    ))

    //启动测试环境的服务
    if(isNeedStart){
      var express = require('express')
      var app = express()
      app.use(express.static(path.join(__dirname , '../dist/test')))
      app.listen(port)
      console.log(chalk.cyan('启动测试服务，端口号为:'+port))

      var opn = require('opn')
      var uri = 'http://localhost:' + port
      opn(uri)
    }

    //将代码发布到测试服务器
    if(isNeedPublish){
      var SSH2Utils = require('ssh2-utils');
      var ssh = new SSH2Utils();

      var server = {
        host: config.test.remoteHost,
        port: config.test.remotePort,
        username: config.test.remoteUsername,
        password: config.test.remotePassword
      }
      var remotePath = config.test.remotePath
      var localPath = config.test.assetsRoot
      ssh.putDir(server, localPath, remotePath, function(err,server,conn){
        if(err){
          console.error("发布到测试服务器失败",err)
        }else{
          console.log(chalk.cyan("成功发布到测试服务器!"))
          conn.end()

          var opn = require('opn')
          var uri = config.test.testWebUrl;
          opn(uri)
        }
      })
    }
  })
})
