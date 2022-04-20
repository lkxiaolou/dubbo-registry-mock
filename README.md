# dubbo-registry-mock

### 介绍

一款 dubbo 注册中心扩展，解决本地开发时对注册中心依赖的问题，不依赖任何第三方注册中心服务（如zookeeper、nacos、etcd等）即可完成本地的 dubbo 服务测试联调。

### 使用场景

- provider: 不依赖第三方注册中心服务即可将 provider 跑起来
- consumer：
  - 本机既起 consumer，又起 provider，mock registry可以像其他注册中心一样工作
  - 本机只起 consumer，需要调用远程provider，可手动配置 provider 地址

### 原理简介

使用本地文件进行通信，`provider`注册时将URL写入文件，`consumer`订阅时读取该文件，文件变更时通知provider。

### 使用方式

- 编译打包
```
mvn clean install
```

- 引入依赖
```xml
<dependency>
    <groupId>org.newboo</groupId>
    <artifactId>dubbo-registry-mock</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

- 配置
最简单的配置如下，ip、port在此只做填充，无实际用处
```
dubbo.registry.address=mock://127.0.0.1:2181
```

默认服务发现文件位于 `/tmp/mock-registry` 目录，也可以通过 registry URL配置的 `discovery_file` 参数来控制：

```
dubbo.registry.address=mock://127.0.0.1:2181?discovery_file=/home/roshi/mock-registry
```

- 指定provider
  - 在文件中新增一行写入 URL 即可被 provider 发现，支持 `#` 开头的注释
  - 在文件中新增一行简写的地址，可被自动转成 URL 被 provider 发现，格式参考如下
  
```
# 以下为自动注册URL
dubbo://172.23.160.129:20880/com.newboo.sample.api.DemoService?anyhost=true&application=boot-samples-dubbo&bean.name=ServiceBean:com.newboo.sample.api.DemoService:1.0.0:read&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&group=read&interface=com.newboo.sample.api.DemoService&methods=sayHello&pid=15712&register=true&release=2.7.3&revision=1.0.0&side=provider&timestamp=1650028374008&version=1.0.0

# 以下为手动新增URL
dubbo://172.23.160.129:20881/com.newboo.sample.api.DemoService?anyhost=true&application=boot-samples-dubbo&bean.name=ServiceBean:com.newboo.sample.api.DemoService:1.0.0:read&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&group=read&interface=com.newboo.sample.api.DemoService&methods=sayHello&pid=15712&register=true&release=2.7.3&revision=1.0.0&side=provider&timestamp=1650028374008&version=1.0.0

# 以下为手动新增简略provider
127.0.0.1:20882:com.newboo.sample.api.DemoService

# 以下为手动新增简略provider，只带version
127.0.0.1:20882:com.newboo.sample.api.DemoService:1.0.0

# 以下为手动新增简略provider，带version、group
127.0.0.1:20882:com.newboo.sample.api.DemoService:1.0.0:read

# 以下为手动新增简略provider，只带group
127.0.0.1:20882:com.newboo.sample.api.DemoService::read
```