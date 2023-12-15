package cn.ray.gateway.etcd.core;

import cn.ray.gateway.etcd.api.*;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;
import io.etcd.jetcd.options.*;
import io.etcd.jetcd.support.CloseableClient;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ray
 * @date 2023/12/12 19:07
 * @description
 */
@Slf4j
public class EtcdClientImpl implements EtcdClient {

    private static final int LEASE_TIME = 5;

    private Client client;

    private KV kvClient;

    private Lease leaseClient;

    private Lock lockClient;

    private Watch watchClient;

    /**
     * 公有租约, 相当于心跳, 随着客户端关闭停止续租
     */
    private long leaseId;

    private HeartBeatLeaseTimeoutListener heartBeatLeaseTimeoutListener;

    private volatile ConcurrentHashMap<String, EtcdWatcher> etcdWatchers = new ConcurrentHashMap<String, EtcdClientImpl.EtcdWatcher>();

    private static final String ETCD_BASE_COUNTDOWN_LATCHER = "__base__v3";

    private AtomicBoolean isCreated = new AtomicBoolean(false);

    public EtcdClientImpl(String address) {
        this(address, false, null, null, null, null);
    }

    public EtcdClientImpl(String address, boolean usedHeartBeatLease) {
        this(address, usedHeartBeatLease, null, null, null, null);
    }

    public EtcdClientImpl(String address, boolean usedHeartBeatLease, String loadBalancerPolicy) {
        this(address, usedHeartBeatLease, loadBalancerPolicy, null, null, null);
    }

    /**
     * 构造方法，用于初始化 Etcd 客户端实例
     * @param address               Etcd 集群的地址，多个地址以逗号分隔
     * @param usedHeartBeatLease    是否使用心跳租约机制
     * @param loadBalancerPolicy    负载均衡策略，可选
     * @param authority             认证信息的主体，可选
     * @param username              认证用户名，可选
     * @param password              认证密码，可选
     */
    public EtcdClientImpl(String address, boolean usedHeartBeatLease, String loadBalancerPolicy, String authority, String username, String password) {
        // 使用 cas 确保只有一个实例被创建
        if(isCreated.compareAndSet(false, true)) {
            // 创建 Etcd 客户端构建器
            ClientBuilder clientBuilder = Client.builder();

            // 设置 Etcd 集群地址
            String[] addressList = address.split(",");
            clientBuilder.endpoints(addressList);

            // 设置负载均衡策略
            if(StringUtils.isNotBlank(loadBalancerPolicy)) {
                clientBuilder.loadBalancerPolicy(loadBalancerPolicy);
            } else {
                clientBuilder.loadBalancerPolicy();
            }

            // 设置认证信息（如果需要用户名、密码和主体）
            if(StringUtils.isNoneBlank(authority) && StringUtils.isNoneBlank(username) && StringUtils.isNoneBlank(password)) {
                clientBuilder.authority(authority);
                clientBuilder.user(ByteSequence.from(username, Charset.defaultCharset()));
                clientBuilder.password(ByteSequence.from(username, Charset.defaultCharset()));
            }

            // 构建 Etcd 客户端
            this.client = clientBuilder.build();
            this.kvClient = client.getKVClient();
            this.leaseClient = client.getLeaseClient();
            this.watchClient = client.getWatchClient();
            this.lockClient = client.getLockClient();

            try {
                // first as countDownLatch init
                // 初始化计数器键，用于标记初始化完成
                KeyValue kv = getKey(ETCD_BASE_COUNTDOWN_LATCHER);
                if(kv == null) {
                    putKey(ETCD_BASE_COUNTDOWN_LATCHER, ETCD_BASE_COUNTDOWN_LATCHER);
                }
                log.info("#EtcdClientImpl#Constructor Init ok!");

                // 如果启用了心跳租约
                if(usedHeartBeatLease) {
                    this.leaseId = this.generatorLeaseId(LEASE_TIME);

                    // 启动一个用于定期续租的线程，并使用 StreamObserver 处理续租响应
                    this.keepAliveSingleLease(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {

                        //	onNext：确定下一次租约续约时间后触发
                        @Override
                        public void onNext(LeaseKeepAliveResponse value) {
                            // 在租约续约成功时触发，可在此处理相应逻辑
//							log.info("#EtcdClientImpl.keepAliveSingleLease# onNext, leaseId: {} ttl: {} !", value.getID(), value.getTTL());
                        }

                        //	onError：续约异常时触发
                        @Override
                        public void onError(Throwable t) {
                            // 在租约续约过程中出现异常时触发，可在此处理异常情况
                            log.error("#EtcdClientImpl.keepAliveSingleLease# onError !", t);
                        }

                        //	onCompleted：租约过期后触发
                        @Override
                        public void onCompleted() {
                            // 在租约过期时触发，通常意味着续租失败，需要处理租约过期的逻辑
                            log.error("#EtcdClientImpl.keepAliveSingleLease# onCompleted !");

                            // 通知监听器，租约已过期
                            heartBeatLeaseTimeoutListener.timeoutNotify();
                        }
                    });

                    log.info("#EtcdClientImpl.HeartBeatLease# heartbeat lease thread is running, leaseId: {} !", leaseId);
                }
            } catch (Exception e) {
                log.error("#EtcdClientImpl#Constructor Init Error, execute close ! ", e);
                this.close();
                log.info("#EtcdClientImpl#Constructor Init Error, execute close ok ! ", e);
            } finally {
                // 注册关闭钩子，确保在应用程序关闭时优雅地关闭 Etcd 客户端
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    this.close();
                    log.info("#EtcdClientImpl# EtcdClient Shutdown Hook ok!");
                }));
            }
        }
    }

    /**
     * 获取当前心跳租约的ID
     * @return 心跳租约的ID
     * @throws InterruptedException
     */
    public long getHeartBeatLeaseId() throws InterruptedException {
        return this.leaseId;
    }

    /**
     * 将键值对存储到Etcd中
     * @param key
     * @param value
     * @throws Exception
     */
    public void putKey(String key, String value) throws Exception {
        // 使用异步方式向Etcd存储键值对
        CompletableFuture<PutResponse> completableFuture = kvClient.put(ByteSequence.from(key, Charset.defaultCharset()), ByteSequence.from(value.getBytes(CHARSET)));

        // 等待操作完成，阻塞当前线程直到异步操作完成
        completableFuture.get();
    }

    /**
     * 异步方式将键值对存储到Etcd中，并返回一个CompletableFuture对象用于处理操作结果
     * @param key
     * @param value
     * @return 表示异步操作结果的CompletableFuture对象
     * @throws Exception
     */
    public CompletableFuture<PutResponse> putKeyCallFuture(String key, String value) throws Exception {
        return kvClient.put(ByteSequence.from(key, Charset.defaultCharset()), ByteSequence.from(value.getBytes(CHARSET)));
    }

    /**
     * 从Etcd中获取指定键的键值对
     * @param key 要获取的键
     * @return 表示键值对的KeyValue对象，如果键不存在则返回null
     * @throws Exception
     */
    public KeyValue getKey(final String key) throws Exception {
        // 使用同步方式从Etcd获取指定键的键值对
        GetResponse getResponse = kvClient.get(ByteSequence.from(key, Charset.defaultCharset())).get();

        // 检查获取的响应是否为null
        if(getResponse == null) {
            return null;
        }

        // 获取响应中的键值对列表
        List<KeyValue> kvs = getResponse.getKvs();
        if (kvs.size() > 0) {
            return kvs.get(0);
        } else {
            return null;
        }
    }

    /**
     * 从Etcd中删除指定键的键值对
     * @param key
     */
    public void deleteKey(String key) {
        // 使用同步方式从Etcd删除指定键的键值对
        kvClient.delete(ByteSequence.from(key, Charset.defaultCharset()));
    }

    /**
     * 从Etcd中获取具有指定前缀的键值对列表
     * @param prefix 前缀，用于筛选键
     * @return 包含具有指定前缀的键值对的列表
     */
    public List<KeyValue> getKeyWithPrefix(String prefix) {
        List<KeyValue> keyValues = new ArrayList<>();

        // 创建GetOption对象，设置前缀过滤条件
        GetOption getOption = GetOption.newBuilder().withPrefix(ByteSequence.from(prefix, Charset.defaultCharset())).build();
        try {
            // 使用同步方式从Etcd获取具有指定前缀的键值对列表
            keyValues = kvClient.get(ByteSequence.from(prefix, Charset.defaultCharset()), getOption).get().getKvs();
        } catch (Exception e) {
            throw new RuntimeException("#EtcdClientImpl.getKeyWithPrefix# Error: " + e.getMessage(), e);
        }
        return keyValues;
    }

    /**
     * 从Etcd中删除具有指定前缀的所有键值对
     * @param prefix 前缀，用于筛选要删除的键
     */
    public void deleteKeyWithPrefix(String prefix) {
        // 创建DeleteOption对象，设置前缀过滤条件
        DeleteOption deleteOption = DeleteOption.newBuilder().withPrefix(ByteSequence.from(prefix, Charset.defaultCharset())).build();
        kvClient.delete(ByteSequence.from(prefix, Charset.defaultCharset()), deleteOption);
    }

    /**
     * 使用私有租约将键值对存储到Etcd中，并返回对应的租约ID
     * @param key
     * @param value
     * @param expireTime 私有租约：适用于需要为每个键值对设置独立过期时间的情况，具有更灵活的管理和控制
     * @return
     */
    private long putKeyWithPrivateLease(String key, String value, long expireTime) {
        // 异步方式请求新的私有租约
        CompletableFuture<LeaseGrantResponse> leaseGrantResponse = leaseClient.grant(expireTime);

        PutOption putOption;
        try {
            // 创建PutOption对象，设置租约ID
            putOption = PutOption.newBuilder().withLeaseId(leaseGrantResponse.get().getID()).build();
            // 使用同步方式将键值对存储到Etcd中，使用指定的租约ID
            kvClient.put(ByteSequence.from(key, Charset.defaultCharset()), ByteSequence.from(value, Charset.defaultCharset()), putOption);
            // 返回存储操作使用的租约ID
            return leaseGrantResponse.get().getID();
        } catch (Exception e) {
            throw new RuntimeException("#EtcdClientImpl.putKeyWithPrivateLease# Error: " + e.getMessage(), e);
        }
    }

    /**
     * 使用公共租约将键值对存储到Etcd中，并返回存储操作的修订版本号
     * @param key
     * @param value
     * @param leaseId 公共租约：适用于多个键值对共享同一过期时间的情况，可以减少租约管理的复杂性
     * @return 存储操作的修订版本号
     * @throws Exception
     */
    private long putKeyWithPublicLease(String key, String value, long leaseId) throws Exception {
        // 创建PutOption对象，设置公共租约ID
        PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();

        // 使用同步方式将键值对存储到Etcd中，使用指定的公共租约ID
        CompletableFuture<PutResponse> putResponse = kvClient.put(ByteSequence.from(key, Charset.defaultCharset()), ByteSequence.from(value, Charset.defaultCharset()), putOption);
        try {
            // 获取存储操作的修订版本号，并返回
            return putResponse.get().getHeader().getRevision();
        } catch (Exception e) {
            throw new RuntimeException("#EtcdClientImpl.putKeyWithPublicLease# Error: " + e.getMessage(), e);
        }
    }

    public long putKeyWithExpireTime(String key, String value, long expireTime) {
        return putKeyWithPrivateLease(key, value, expireTime);
    }

    public long putKeyWithLeaseId(String key, String value, long leaseId) throws Exception{
        return putKeyWithPublicLease(key, value, leaseId);
    }

    /**
     * 生成一个新的租约ID，并设置租约的过期时间
     * @param expireTime 租约的过期时间
     * @return 生成的租约ID
     * @throws Exception
     */
    public long generatorLeaseId(long expireTime) throws Exception {
        // 异步方式请求新的租约
        CompletableFuture<LeaseGrantResponse> leaseGrantResponse = leaseClient.grant(expireTime);
        // 获取生成的租约ID并返回
        return leaseGrantResponse.get().getID();
    }

    /**
     * 启动对指定租约的单次续约操作，并提供用于处理续约响应的观察者。
     * @param leaseId   租约ID，用于标识要进行续约的租约
     * @param observer  用于处理续约响应的观察者，包括 onNext、onError、onCompleted 方法
     * @return CloseableClient，用于关闭续约操作
     */
    public CloseableClient keepAliveSingleLease(long leaseId, StreamObserver<LeaseKeepAliveResponse> observer) {
        return leaseClient.keepAlive(leaseId, observer);
    }

    /**
     * 发送一次对指定租约的续约请求，等待并返回续约响应
     * @param leaseId   租约ID，标识要进行续约的租约
     * @return LeaseKeepAliveResponse 续约响应
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public LeaseKeepAliveResponse keepAliveOnce(long leaseId) throws InterruptedException, ExecutionException {
        // 发送一次对指定租约的续约请求，返回 CompletableFuture 对象
        CompletableFuture<LeaseKeepAliveResponse> completableFuture = leaseClient.keepAliveOnce(leaseId);
        if(completableFuture != null) {
            // 等待并返回续约响应
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 发送一次对指定租约的续约请求，等待并返回续约响应，设置超时时间。
     * @param leaseId 租约ID，标识要进行续约的租约
     * @param timeout 超时时间
     * @return LeaseKeepAliveResponse 续约响应
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public LeaseKeepAliveResponse keepAliveOnce(long leaseId, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        // 发送一次对指定租约的续约请求，返回 CompletableFuture 对象
        CompletableFuture<LeaseKeepAliveResponse> completableFuture = leaseClient.keepAliveOnce(leaseId);
        if(completableFuture != null) {
            // 等待并返回续约响应，设置超时时间
            return completableFuture.get(timeout, TimeUnit.MILLISECONDS);
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 查询指定租约的生存时间和与其关联的键的信息。
     * @param leaseId 租约ID，标识要查询的租约
     * @return LeaseTimeToLiveResponse 包含租约生存时间和关联键信息的响应
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public LeaseTimeToLiveResponse timeToLiveLease(long leaseId) throws InterruptedException, ExecutionException {
        // 查询指定租约的生存时间和与其关联的键的信息，返回 CompletableFuture 对象
        CompletableFuture<LeaseTimeToLiveResponse> completableFuture = leaseClient.timeToLive(leaseId, LeaseOption.newBuilder().withAttachedKeys().build());
        if(completableFuture != null) {
            // 等待并返回查询租约生存时间的响应
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 撤销指定租约
     * @param leaseId   租约ID，标识要撤销的租约
     * @return LeaseRevokeResponse 包含撤销租约的响应
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public LeaseRevokeResponse revokeLease(long leaseId) throws InterruptedException, ExecutionException {
        CompletableFuture<LeaseRevokeResponse> completableFuture = leaseClient.revoke(leaseId);
        if(completableFuture != null) {
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 请求获取指定锁
     * @param lockName  锁的名称，用于标识要获取的锁
     * @return LockResponse 包含获取锁的响应
     * @throws Exception
     */
    public LockResponse lock(String lockName) throws Exception {
        CompletableFuture<LockResponse> completableFuture = lockClient.lock(ByteSequence.from(lockName, Charset.defaultCharset()), 0);
        if(completableFuture != null) {
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 请求获取指定锁，并指定锁的过期时间
     * @param lockName      锁的名称，用于标识要获取的锁
     * @param expireTime    锁的过期时间，指定获取锁后的租约过期时间
     * @return LockResponse 包含获取锁的响应
     * @throws Exception
     */
    public LockResponse lock(String lockName, long expireTime) throws Exception {
        // 请求创建指定过期时间的租约，返回 CompletableFuture 对象
        CompletableFuture<LeaseGrantResponse> leaseGrantResponse = leaseClient.grant(expireTime);

        // 请求获取指定锁，指定使用的租约ID，返回 CompletableFuture 对象
        CompletableFuture<LockResponse> completableFuture = lockClient.lock(ByteSequence.from(lockName, Charset.defaultCharset()), leaseGrantResponse.get().getID());
        if(completableFuture != null) {
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 使用指定的租约ID请求获取锁
     * @param lockName  锁的名称，用于标识要获取的锁
     * @param leaseId   要与锁关联的租约ID
     * @return LockResponse 包含获取锁的响应
     * @throws Exception
     */
    public LockResponse lockByLeaseId(String lockName, long leaseId) throws Exception {
        CompletableFuture<LockResponse> completableFuture = lockClient.lock(ByteSequence.from(lockName, Charset.defaultCharset()), leaseId);
        if(completableFuture != null) {
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 请求释放指定的锁
     * @param lockName  锁的名称，用于标识要释放的锁
     * @return UnlockResponse 包含释放锁的响应
     * @throws Exception
     */
    public UnlockResponse unlock(String lockName) throws Exception {
        // 请求释放指定的锁，返回 CompletableFuture 对象
        CompletableFuture<UnlockResponse> completableFuture = lockClient.unlock(ByteSequence.from(lockName, Charset.defaultCharset()));
        if(completableFuture != null) {
            return completableFuture.get();
        } else {
            throw new EtcdResponseNullPointerException();
        }
    }

    /**
     * 添加Etcd Watcher监听器，用于监视指定的Etcd键。
     * @param watcherKey        要监视的键
     * @param usePrefix         是否使用键前缀进行监视
     * @param watcherListener   Etcd Watcher监听器，处理键变更事件
     * @see cn.ray.gateway.etcd.api.EtcdClient#addWatcherListener(java.lang.String, boolean, cn.ray.gateway.etcd.api.WatcherListener)
     */
    public synchronized void addWatcherListener(final String watcherKey, final boolean usePrefix, WatcherListener watcherListener) {
        // 检查是否已存在对应的Etcd Watcher
        EtcdWatcher etcdWatcher = etcdWatchers.get(watcherKey);

        if(etcdWatcher == null) {
            // 如果Etcd Watcher不存在，创建并启动新的Etcd Watcher
            etcdWatcher = new EtcdWatcher(EtcdClientImpl.this, watcherKey, usePrefix, watcherListener);
            etcdWatcher.start();

            // 将新创建的Etcd Watcher添加到Etcd Watchers集合中
            etcdWatchers.putIfAbsent(watcherKey, etcdWatcher);
            log.info("addWatcherListener watcherKey : {}, usePrefix : {}", watcherKey, usePrefix);
        }
    }

    /**
     * 移除Etcd Watcher监听器，停止并从Etcd Watchers集合中移除指定键的Etcd Watcher
     * @param watcherKey 要移除的Etcd Watcher的键
     */
    public synchronized void removeWatcherListener(final String watcherKey) {
        // 获取指定键的Etcd Watcher
        EtcdWatcher etcdWatcher = etcdWatchers.get(watcherKey);

        if(etcdWatcher != null) {
            // 如果Etcd Watcher存在，停止并从Etcd Watchers集合中移除
            etcdWatcher.stop();
            etcdWatchers.remove(etcdWatcher.getWatcherKey());
            log.info("removeWatcherListener watcherKey : {}", etcdWatcher.getWatcherKey());
        }
    }

    /**
     * 内部类，用于监视Etcd中特定键的更改
     */
    private class EtcdWatcher {

        // Etcd客户端实例
        private EtcdClient etcdClient;

        // 要监视的键
        private final String watcherKey ;

        // Etcd Watcher 实例
        private final Watch.Watcher watcher;

        // 监听器，用于处理监视到的键的更改事件
        private final WatcherListener watcherListener;

        /**
         * 构造函数，用于初始化EtcdWatcher
         * @param etcdClient        Etcd客户端实例
         * @param watcherKey        要监视的键
         * @param usePrefix         是否使用键前缀进行监视
         * @param watcherListener   监听器，用于处理监视到的键的更改事件
         */
        public EtcdWatcher(EtcdClient etcdClient, final String watcherKey, final boolean usePrefix, WatcherListener watcherListener) {
            this.etcdClient = etcdClient;
            this.watcherKey = watcherKey;
            this.watcherListener = watcherListener;

            // 创建 Watch.Listener 实例，用于处理 Etcd Watcher 的响应
            Watch.Listener listener  = Watch.listener(response -> {
                try {
                    List<WatchEvent> watcherList = response.getEvents();
                    for(WatchEvent watchEvent : watcherList) {
                        KeyValue prevKeyValue = watchEvent.getPrevKV();
                        KeyValue curtkeyValue = watchEvent.getKeyValue();
                        switch (watchEvent.getEventType()) {
                            // 处理键的 PUT 事件，通知监听器
                            case PUT:
                                this.watcherListener.watcherKeyChanged(this.etcdClient, new EtcdChangedEvent(
                                        prevKeyValue,
                                        curtkeyValue,
                                        EtcdChangedEvent.Type.PUT));
                                break;
                            // 处理键的 DELETE 事件，通知监听器
                            case DELETE:
                                this.watcherListener.watcherKeyChanged(this.etcdClient, new EtcdChangedEvent(
                                        prevKeyValue,
                                        curtkeyValue,
                                        EtcdChangedEvent.Type.DELETE));
                                break;
                            case UNRECOGNIZED:
                                // ignore
                                // 忽略无法识别的事件类型
                                log.warn("#EtcdClientImpl.EtcdWatcher# watched UNRECOGNIZED Warn, Type: {} ", EtcdChangedEvent.Type.UNRECOGNIZED);
//									watcherListener.watcherKeyChanged(etcdClient, new EtcdChangedEvent(
//											keyValue.getKey().toStringUtf8(),
//											keyValue.getValue().toStringUtf8(),
//											EtcdChangedEvent.Type.UNRECOGNIZED));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("#EtcdClientImpl.EtcdWatcher# watcher running thread is Warn, catch InterruptedException! ", e);
                    // ignore
                } catch (Throwable e) {
                    log.error("#EtcdClientImpl.EtcdWatcher# watcher running thread is Error, catch Throwable! ", e);
                    // ignore
                }
            });

            // 根据是否使用键前缀创建 Etcd Watcher 实例
            if (usePrefix) {
                watcher = watchClient.watch(ByteSequence.from(watcherKey, Charset.defaultCharset()),
                        WatchOption.newBuilder().withPrefix(ByteSequence.from(watcherKey, Charset.defaultCharset())).build(),
                        listener);
            } else {
                watcher = watchClient.watch(ByteSequence.from(watcherKey, Charset.defaultCharset()),
                        listener);
            }
        }

        /**
         * 启动Etcd Watcher, 初始化, 目前不做任何操作
         */
        public void start() {
            //	ignore
        }

        /**
         * 停止Etcd Watcher，关闭Watcher实例
         */
        public void stop() {
            this.watcher.close();
        }

        /**
         * 获取当前Etcd Watcher的键
         * @return
         */
        public String getWatcherKey() {
            return watcherKey;
        }
    }

    /**
     * 关闭Etcd Client及其所有的Watchers
     * 遍历Etcd Watchers集合，停止每个Watcher，并关闭Etcd Client
     */
    public void close() {
        for(Map.Entry<String, EtcdWatcher> me : etcdWatchers.entrySet()){
            EtcdWatcher etcdWatcher = me.getValue();
            etcdWatcher.stop();
        }
        if(client != null) {
            client.close();
        }
    }

    public void addHeartBeatLeaseTimeoutNotifyListener(HeartBeatLeaseTimeoutListener heartBeatLeaseTimeoutListener) {
        this.heartBeatLeaseTimeoutListener = heartBeatLeaseTimeoutListener;
    }

}