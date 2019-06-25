
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.sia.rabbitmqplus.binding.RateLimiter;
import com.rabbitmq.client.Address;

public class ShuffleTest {

    private static final String RABBIT_HOST = "skytrain-master.jishu.idc,skytrain-arbiter.jishu.idc,skytrain-slaver.jishu.idc";

    void show(List<Address> addrs) {

        System.err.println("------------------");
        for (Address add : addrs) {
            System.err.println(add);
        }
        System.err.println("------------------");
    }

    @Test
    public void test() {

        List<Address> addrs = new LinkedList<Address>();

        String[] hosts = RABBIT_HOST.split(",");
        for (int i = 0; i < hosts.length; i++) {
            Address addr = new Address(hosts[i].trim(), 5672);
            addrs.add(addr);
        }
        show(addrs);
        // FisherYates洗牌算法，打乱地址的次序，用作客户端负载均衡
        Collections.shuffle(addrs);
        show(addrs);
    }

    @Test
    public void testRateLimiter() throws InterruptedException {

        RateLimiter limit = new RateLimiter(TimeUnit.MINUTES);
        for (int i = 0; i < 10; i++) {
            System.err.println(System.currentTimeMillis() + ":" + limit.acquire(2, 30));
            Thread.sleep(1000L);
        }
    }

}
