import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.sia.supervise.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class/* ,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT */)
public class RedisTemplateTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void scanTest() throws Exception {

        Set<String> last = redisTemplate.keys("*");
        for (String item : last) {
            System.err.println(item + "->" + redisTemplate.opsForValue().get(item));

        }
    }

    @Ignore
    @Test
    public void keysTest() throws Exception {
        //{"unConsumeMessageAlarmGrowthTimes":10,"projectDescription":"反欺诈微服务","queueName":"hawkeye_clic_interaction_receiveClicData","deliverIp":"10.251.32.23","type":"deliver","projectName":"hawkeye_clic_interaction","publishRecentTime":"","publishIp":"","emailReceviers":"zhengtengwang@creditease.cn,leidong4@creditease.cn","deliverRecentTime":"2019-01-22 13:10:06","unConsumeMessageAlarmNum":200}
        redisTemplate.opsForValue().set("name", "lpf");
        redisTemplate.opsForValue().set("age", "28");
        String msg="{\"queueName\":\"zhxdxt_asyn_applyDataSaveZHUGAN\",\"emailSubject\":null,\"emailContent\":null,\"emailAttachFileNames\":null,\"unConsumeMessageNum\":0,\"queueConsumers\":1,\"lastActiveTime\":0,\"publishMessageNum\":0,\"allPublishMessageNum\":252,\"deliverMessageNum\":0,\"allDeliverMessageNum\":252,\"unConsumeMessageGrowthTimes\":0,\"unConsumeMessageAlarmNum\":100,\"unConsumeMessageAlarmGrowthTimes\":5,\"publishIps\":{\"10.143.135.178\":\"2019-04-01 11:42:22\"},\"deliverIps\":{\"10.100.61.111\":\"2019-04-17 20:36:12\",\"10.251.1.22\":\"2019-04-16 16:53:21\",\"10.251.1.34\":\"2019-04-15 19:23:06\",\"10.251.1.57\":\"2019-04-16 17:32:01\",\"10.251.1.58\":\"2019-04-15 17:31:53\",\"10.251.100.124\":\"2019-04-15 18:23:36\",\"10.251.100.132\":\"2019-04-17 18:48:20\",\"10.251.100.77\":\"2019-04-16 17:15:06\",\"10.251.100.91\":\"2019-04-16 11:05:28\",\"10.251.2.107\":\"2019-04-18 13:31:21\",\"10.251.2.124\":\"2019-04-17 17:42:40\",\"10.251.2.126\":\"2019-04-16 17:59:49\",\"10.251.2.23\":\"2019-04-15 18:18:13\",\"10.251.2.7\":\"2019-04-16 10:28:27\",\"10.251.2.80\":\"2019-04-16 16:24:57\",\"10.251.48.43\":\"2019-04-17 18:10:58\",\"10.251.48.47\":\"2019-04-17 17:13:40\",\"10.251.48.65\":\"2019-04-15 11:45:24\",\"10.251.62.19\":\"2019-01-22 13:10:18\"},\"projectInfo\":{\"projectName\":\"ICP2.0_ZHUGAN%zhxdxt\",\"projectDescription\":\"sia项目组自测\",\"emailReceviers\":[\"guanglu@creditease.cn\"]}}";
        redisTemplate.opsForValue().set("zhxdxt_asyn_applyDataSaveZHUGAN", msg);
        Set<String> last = redisTemplate.keys("*");
        for (String item : last) {
            System.err.println(item + "->" + redisTemplate.opsForValue().get(item));
            //redisTemplate.delete(item);
            System.err.println(item + "->" + redisTemplate.hasKey(item));
        }
    }

}