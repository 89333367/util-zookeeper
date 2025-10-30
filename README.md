# util-zookeeper

Zookeeper操作工具类

# pom.xml依赖

```xml

<dependency>
    <groupId>sunyu.util</groupId>
    <artifactId>util-zookeeper</artifactId>
    <!-- {zookeeper.version}_{util.version}_{jdk.version}_{architecture.version} -->
    <version>3.9.4_1.0_jdk8_x64</version>
    <classifier>shaded</classifier>
</dependency>
```


# 使用示例
```java

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.junit.jupiter.api.Test;
import sunyu.util.ZookeeperUtil;

import java.util.List;

public class ZookeeperUtilTests {
    Log log = LogFactory.get();

    /**
     * [zk: localhost:2181(CONNECTED) 25] ls /consumers
     * [spark_fence_group_farm_aike_20220606, spark_kafka_hbase_farm_aike, spark_kafka_hbase_farm_aike-1, spark_kafka_hbase_farm_aike-1234, spark_kafka_hbase_farm_aike-2, spark_kafka_hbase_farm_aike-3, spark_kafka_hdfs_farm_aike, stream-alarm-realtime-extract-aike-20220606, stream-command-aike, stream-command-aike1, stream-command-aike2, stream-command-test-202200606, stream-command-test-202200906, stream-dataforward, stream-dataforward-aike, stream-dataforward-aike0917, stream-dataforward-aike0918, stream-dataforward-aike0919, stream-dataforward-aike0920, stream-dataforward-aike0921, stream-dataforward-aike1, stream-dataforward-aike2, stream-dataforward1, stream_calc_area_aike, stream_direct_kafka_redis_farm_aike_20220606, uml-stream-redis-fl-aike]
     * [zk: localhost:2181(CONNECTED) 26] ls /consumers/uml-stream-redis-fl-aike
     * [offsets]
     * [zk: localhost:2181(CONNECTED) 27] ls /consumers/uml-stream-redis-fl-aike/offsets
     * [US_GENERAL_NJ]
     * [zk: localhost:2181(CONNECTED) 28] ls /consumers/uml-stream-redis-fl-aike/offsets/US_GENERAL_NJ
     * [0, 1, 10, 11, 2, 3, 4, 5, 6, 7, 8, 9]
     * [zk: localhost:2181(CONNECTED) 29] ls /consumers/uml-stream-redis-fl-aike/offsets/US_GENERAL_NJ/0
     * []
     * [zk: localhost:2181(CONNECTED) 30] get /consumers/uml-stream-redis-fl-aike/offsets/US_GENERAL_NJ/0
     * 47704639
     */

    @Test
    void t001() {
        ZookeeperUtil zookeeperUtil = ZookeeperUtil.builder()
                .setConnectString("cdh1:2181")
                .setSessionTimeout(3000)
                .build();

        String groupId = "stream_kafka_hbse_farm_1";
        List<String> topics = zookeeperUtil.listDirectory("/consumers/" + groupId + "/offsets");
        log.info("{}", topics);
        for (String topic : topics) {
            List<String> partitions = zookeeperUtil.listDirectory("/consumers/" + groupId + "/offsets/" + topic);
            log.info("{}", partitions);
            for (String partition : partitions) {
                String nodeData = zookeeperUtil.getNodeData("/consumers/" + groupId + "/offsets/" + topic + "/" + partition);
                log.info("partition:{} offsets:{}", partition, nodeData);
            }
        }

        zookeeperUtil.close();
    }

    @Test
    void t002() {
        ZookeeperUtil zookeeperUtil = ZookeeperUtil.builder()
                .setConnectString("cdh1:2181")
                .setSessionTimeout(3000)
                .build();

        List<String> groups = zookeeperUtil.listDirectory("/consumers");
        log.info("{}", groups);

        zookeeperUtil.close();
    }

    @Test
    void t003() {
        ZookeeperUtil zookeeperUtil = ZookeeperUtil.builder()
                .setConnectString("cdh1:2181")
                .setSessionTimeout(3000)
                .build();

        List<String> groups = zookeeperUtil.listDirectory("/consumers");
        for (String groupId : groups) {
            log.info("groupId:{}", groupId);
            List<String> topics = zookeeperUtil.listDirectory("/consumers/" + groupId + "/offsets");
            //log.info("{}", topics);
            for (String topic : topics) {
                List<String> partitions = zookeeperUtil.listDirectory("/consumers/" + groupId + "/offsets/" + topic);
                //log.info("{}", partitions);
                for (String partition : partitions) {
                    String nodeData = zookeeperUtil.getNodeData("/consumers/" + groupId + "/offsets/" + topic + "/" + partition);
                    log.info("tpic:{} partition:{} offsets:{}", topic, partition, nodeData);
                }
            }
            log.info("#############################");
        }

        zookeeperUtil.close();
    }
}

```