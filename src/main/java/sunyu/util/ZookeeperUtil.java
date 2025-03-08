package sunyu.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Zookeeper工具类
 *
 * @author SunYu
 */
public class ZookeeperUtil implements AutoCloseable {
    private final Log log = LogFactory.get();
    private final Config config;

    public static Builder builder() {
        return new Builder();
    }

    private ZookeeperUtil(Config config) {
        log.info("[构建Zookeeper工具类] 开始");
        if (StrUtil.isBlank(config.connectString)) {
            throw new RuntimeException("Zookeeper连接字符串不能为空");
        }
        if (config.sessionTimeout <= 0) {
            throw new RuntimeException("会话超时时间必须大于0");
        }
        try {
            config.zooKeeper = new ZooKeeper(config.connectString, config.sessionTimeout, event -> {
                log.info("[触发Zookeeper事件] {}", event.getState());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.config = config;
        log.info("[构建Zookeeper工具类] 结束");
    }

    private static class Config {
        private String connectString; // Zookeeper连接字符串
        private int sessionTimeout; // 会话超时时间
        private ZooKeeper zooKeeper; // ZooKeeper客户端实例
    }

    public static class Builder {
        private final Config config = new Config();

        public ZookeeperUtil build() {
            return new ZookeeperUtil(config);
        }

        /**
         * 设置连接字符串，以逗号分隔的主机和端口对，每一对对应一个 zk 服务器。例如 “127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002”。如果使用了可选的 chroot 后缀，示例将会是 “127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002/app/a”，此时客户端将位于 “/app/a” 目录下，所有路径都将相对于此根目录 —— 即获取、设置等操作 “/foo/bar” 将会在服务器端执行于 “/app/a/foo/bar” 路径上。
         *
         * @param connectString
         * @return
         */
        public Builder setConnectString(String connectString) {
            config.connectString = connectString;
            return this;
        }

        /**
         * 设置会话超时时间，以毫秒为单位
         *
         * @param sessionTimeout
         * @return
         */
        public Builder setSessionTimeout(int sessionTimeout) {
            config.sessionTimeout = sessionTimeout;
            return this;
        }
    }

    /**
     * 回收资源
     */
    @Override
    public void close() {
        log.info("[Zookeeper工具关闭] 开始");
        if (config.zooKeeper != null) {
            try {
                config.zooKeeper.close();
            } catch (Exception e) {
                log.error("[Zookeeper工具关闭] 失败", ExceptionUtil.stacktraceToString(e));
            }
        }
        log.info("[Zookeeper工具关闭] 结束");
    }


    /**
     * 遍历目录
     *
     * @param path 路径
     * @return 子节点列表
     */
    public List<String> listDirectory(String path) {
        try {
            return config.zooKeeper.getChildren(path, false);
        } catch (KeeperException | InterruptedException e) {
            log.error("遍历目录失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取节点数据
     *
     * @param path 路径
     * @return 节点数据
     */
    public String getNodeData(String path) {
        try {
            byte[] data = config.zooKeeper.getData(path, false, null);
            return new String(data);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点数据失败", e);
            return null;
        }
    }

    /**
     * 创建节点
     *
     * @param path 路径
     * @param data 节点数据
     * @return 创建的节点路径
     */
    public String createNode(String path, String data) {
        try {
            byte[] dataBytes = data.getBytes();
            return config.zooKeeper.create(path, dataBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            log.error("创建节点失败", e);
            return null;
        }
    }

    /**
     * 删除节点
     *
     * @param path 路径
     */
    public void deleteNode(String path) {
        try {
            config.zooKeeper.delete(path, -1);
            log.info("删除节点: {}", path);
        } catch (KeeperException | InterruptedException e) {
            log.error("删除节点失败", e);
        }
    }
}
