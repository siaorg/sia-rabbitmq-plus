# 开发指南


## skytrain-client消息发送配置
### 1、准备工作

+ 发送端需要配置`siaparameters.properties`文件(**文件名不可更改**)

```properties

RABBITMQ_HOST=10.100.66.81
RABBITMQ_PORT=5672
SKYTRAIN_LOG_ROOT=D:/logs/send
SKYTRAIN_LOG_FILESIZE=10MB
SKYTRAIN_LOG_FILENUMS=5
PROJECT_NAME=skytrain_client_test
PROJECT_DESCRIPTION=skytrain_project_team
EMAIL_RECEVIERS=xinliang@creditease.cn,pengfeili23@creditease.cn

```
+ `RABBITMQ_HOST`，MQ服务器的IP地址（必须）

+ `RABBITMQ_PORT`，MQ服务器对外暴露的服务端口（必须）

+ `SKYTRAIN_LOG_ROOT`，自定义日志输出路径（可选，默认系统当前路径）。发送接收消息产生的日志按照队列名在指定路径生成

+ `SKYTRAIN_LOG_FILESIZE`，自定义日志大小（可选，默认20MB，单位：【KB、MB、GB】）SKYTRAIN_LOG_FILENUMS，自定义日志个数（可选，默认10个）

注意：默认消息日志大小为：20MB*10，循环日志，项目组可根据实际需求设置大小及个数

+ `PROJECT_NAME`，项目组名称（必须），注意接收的队列名必须以此开头。如果是发送者，请让接收者先启动（因为发送者不建立队列）

+ `PROJECT_DESCRIPTION`，项目组描述，对项目组的（中文）描述（可选）

+ `EMAIL_RECEVIERS`，预警邮件接收者，邮箱为公司邮箱，多个按逗号隔开（必须）

### 2、siaparameters.properties 配置文件的读取

1.	从class文件所在的路径查找

2.	若上一步没有找到，则从项目lib包所在的路径查找

3.	自定义目录下，通过代码
PropertyHelper.setProfilePath("文件所在目录");
设置，就能正确加载。配置文件的读取优先级从1.->2.->3.，如果最终没有找到，会有出错信息，按照提示解决即可。一般把该文件与其他配置文件放在一起即可。

4. 启动参数添加
在启动时，添加JVM参数 -DSKYTRAIN_FILE_PATH=文件所在目录  
这种方式与3.二选一即可

## skytrain-client消息接收配置

###	1、接收端需要配置 siaparameters.properties 文件

参见siaparameters.properties 文件的配置

###	2、接收端需要配置 receivequeue.properties 文件

注意：文件名不可更改！

**点对点模式接收配置**：

 ```properties
 skytrain_client_test_send_p2p={"unConsumeMessageAlarmNum":200,"unConsumeMessageAlarmGrowthTimes":10,"className":"skytrainDemo.RecevieP2P","methodName":"execRun","autoAck":"false","threadPoolSize":"4"}
 ```

**发布订阅模式接收配置**：

```properties
skytrain_client_test_send_pubsub@skytrain_client_test_receive_message={"unConsumeMessageAlarmNum":200,"unConsumeMessageAlarmGrowthTimes":10,"className":"skytrainDemo.ReceviePubSub","methodName":"execRun","autoAck":"true","threadPoolSize":"4"}
```

+ skytrain_client_test_send_p2p，MQ服务器的接收队列名，对应发送端的businessCode（点对点模式，必须），项目组自行设置，需要以PROJECT_NAME 开头

+ skytrain_client_test_send_pubsub，MQ服务器接收交换机名，对应发送端的groupCode（发布订阅模式，必须），项目组自行设置。

+ skytrain_client_test_receive_message，MQ服务器的与交换机绑定的队列名（发布订阅模式，必须），项目组自行设置。

+ unConsumeMessageAlarmNum，自定义队列累积条数预警阈值，超过则发预警邮件（可选，默认为100条）

+ unConsumeMessageAlarmGrowthTimes，自定义累积消息持续递增的次数阈值，超过则发预警邮件（可选，默认为5次）

+ className，接收端进行消息处理的（包名+）类名（必须）

+ methodName，接收端进行消息处理的方法名（必须），接收参数只有一个，要么是SIAMessage要么是String，例如：

```java
public void execRun(SIAMessage message) {}
public void execRun(String message) {}
```

+ autoAck，是否开启自动ACK机制（可选），默认手动确认（true，接收方接收到消息后，自动向服务器发回确认，false，消息接收方处理完消息后，再向服务器发送消息确认，服务器再删除该消息的副本）。建议设为false（手动确认），这样能保证未消费的消息不丢失，但线上可能累积消息。若设为true，则将消息全部拉回本地内存（如果宕机，则消息丢失）。本质上消息的消费速度与该设置无关，与消息的处理线程池大小有关。这个设置的意义是选择将消息缓存在本地内存还是服务器。请项目组自己考量。

+ threadPoolSize，接收端进行消息处理使用线程池的大小（可选），默认为1，这个值与并行处理消息数相关，建议设置为处理器的核数X2。

###	3、receivequeue.properties配置文件的读取

略，参见 siaparameters.properties 配置文件的读取

###	4、消费者的启动

文件 receivequeue.properties 正确配置后，只需在程序的启动（初始化）块中加入代码：

```java
Consumer.start();
```

就能按照配置文件的设置启动所有的消费者（receivequeue.properties 里可以配置多个队列消费者）

#### 如果使用Spring的bean相关配置：
例：
```xml
<bean id="helloWorld" class="skytrainDemo.testBean" />
```
上面的对应设置为：  

点对点接收配置：

```properties
skytrain_client_test_send_p2p={"unConsumeMessageAlarmNum":200,"unConsumeMessageAlarmGrowthTimes":10,"beanName":"helloWorld","beanMethodName":"getMessage","autoAck":"false","threadPoolSize":"4"}
```

发布订阅接收配置：

```properties
skytrain_client_test_send_pubsub@skytrain_client_test_receive_message={"unConsumeMessageAlarmNum":200,"unConsumeMessageAlarmGrowthTimes":10,"beanName":"helloWorld","beanMethodName":"getMessage","autoAck":"false","threadPoolSize":"4"}
```

+ beanName，bean的名字，需要在 Spring 的 applicationContext.xml 中配置，如下所示

```xml
<bean id="helloWorld" class="com.sia.testBean" />
```
+ beanMethodName，接收的方法名，bean所在的类中对应的接收方法，与methodName一样，只能包含一个（String或SIAMessage）参数  

#### 注意：


如果使用 Spring 而不是 Web 项目，只需在程序的启动（初始化）块中加入代码：

```java
Consumer.start(ApplicationContext applicationContext)
```

传递一个应用的上下文，就能按照配置文件的设置启动所有的消费者（receivequeue.properties 里可以配置多个队列消费者）

如果是 Web 项目，通过 listener 启动就可以了
## 接收端web.xml配置
使用如下配置:

```xml
<listener>
    <listener-class>com.sia.rabbitmqplus.start.SIAInitialListener</listener-class>
</listener>
```

#### 注意：
配置过程中，最好将 sia 的 SIAInitialListener 配置在 web.xml 的最后，便于sia的正常加载。


### SIA对象说明
<table border="1">
    <tr>
        <td colspan="4">
          <p>类名： SIAMessage</p>
          <p>所在包：com.creditease.sia.pojo</p>   
          <p>实现接口：Serializable</p>
        </td>   
    </tr>
    <tr>
        <th>属性名</th>
        <th>类型</th>
        <th>解释</th>
        <th>是否必须设置</th>
    </tr>
    <tr>
        <td>messageId</td>
        <td>String</td>
        <td>消息id，方便用于追踪</td>
        <td>可选</td>
    </tr>
    <tr>
        <td>messageInfoClob</td>
        <td>String</td>
        <td>消息主体，可以理解为消息对象的字节流</td>
        <td>必须</td>			
    </tr>
    <tr>
        <td>businessCode</td>
        <td>String</td>
        <td>收发双方通信的队列名称，建议命名格式为：
                <b>项目名称_自定义名称</b>，
                中间可以加上有意义的信息，比如：部门名称，创建者等。
                例，我现在做一个helloworld项目，那就是：
                helloWorld_developCenter_lpf_justForTest，
                目的就是保证队列不会出现重名
        <td>除了发布订阅模式不需要设置(endSend 也不需要)，其他必须设置</td>			
    </tr>
    <tr>
        <td>messageType</td>
        <td>String</td>
        <td>消息类型。以 SIAMessage 对象发送数据 type 为 "object"，以 String 字符串发送数据 type 为 "text/plain"。默认类型为 "text/plain"</td>
        <td>必须</td>			
    </tr>
     <tr>
        <td>receiveQueueName</td>
        <td>String</td>
        <td>同步链条模式下，beginSend 获取回调消息时所监听的队列名</td>
        <td>系统自动生成，无需设置</td>			
    </tr>
    <tr>
        <td>groupCode</td>
        <td>String</td>
        <td>交换机名，表示使用发布订阅模式，建议命名格式为：
<b>项目名称_自定义名称</b>。
中间可以加上有意义的信息，比如：部门名称，创建者等</td>
        <td>使用发布订阅模式，sendGroup 必须设置，其他情况一定不要设置</td>			
    </tr>
     <tr>
        <td>timeout</td>
        <td>Integer</td>
        <td>同步链条模式超时时间默认580秒</td>
        <td>可选</td>			
    </tr>
    <tr>
        <td>expires</td>
        <td>Integer</td>
        <td>临时队列存活时间默认30秒</td>
        <td>可选</td>			
    </tr>
</table>
