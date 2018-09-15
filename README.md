# Spring-Cloud | openfeign使用细节

## 写在前面的话

各位，下午好！

我比较喜欢用 fegin 来实现微服务之间的调用，但是feign使用的那些细节，是get到了吗？本节我将使用Spring Boot 2.0.5.RELEASE + Spring Cloud SR1 + openfeign并结合实际的使用，教你使用feign的姿势。

## 项目架构

我们先对测试架构一番，看图

![针对Feign的使用测试架构图](https://upload-images.jianshu.io/upload_images/5805596-f6fc0ab5242fc257.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


简单来说，就是服务模块化分为：model层、API层、service层，其他服务就可以依赖API层。

另外，我们看一下，Spring官网提供的一段关于[Feign Inheritance Support](http://cloud.spring.io/spring-cloud-static/Finchley.SR1/single/spring-cloud.html#spring-cloud-feign-inheritance)代码：

![Feign Inheritance Support](https://upload-images.jianshu.io/upload_images/5805596-5642895f5fc14e3e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下面我们就动手写例子。

## 测试实例

1、先看一下完成后的目录截图

![测试项目目录结构](https://upload-images.jianshu.io/upload_images/5805596-48882849edb25199.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

我们看 `api`、 `model`、 `service`、 `feign-use`之间的依赖关系。
api依赖model
service依赖api，实现api接口
feign-use依赖api，client继承api，并注入spring bean

2、使用公益eureka，这样我们就省略构建服务注册中心了

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka.fengwenyi.com/eureka/
```

3、关于项目多模块化，看这里：https://github.com/fengwenyi/multi-module。

4、model中的代码：

```java
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Wenyi Feng
 * @since 2018-09-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User {

    /** 标识 */
    private String uuid;

    /** 姓名 */
    private String name;

    /** 年龄 */
    private Integer age;
}
```

5、API接口

```java
import com.fengwenyi.data.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Wenyi Feng
 * @since 2018-09-15
 */
public interface API {

    /**
     * 获取users
     * @return
     */
    @GetMapping("/getUsers")
    List<User> getUsers();

    /**
     * 根据用户ID获取user
     * @param uuid 用户ID
     * @return
     */
    @GetMapping("/getUserById/{uuid}")
    User getUserById(@PathVariable("uuid") String uuid);

    /**
     * 添加用户
     * @param user 用户对象
     * @return
     */
    @PostMapping("/addUser")
    boolean addUser(@RequestBody User user);

    /**
     * 根据用户ID修改用户信息
     * @param uuid 用户ID
     * @param user
     * @return
     */
    @PostMapping("/updateUserById/{uuid}")
    boolean updateUserById(@PathVariable("uuid") String uuid, @RequestBody User user);

    /**
     * 根据用户ID修改用户信息
     * @param uuid 用户ID
     * @param age 用户年龄
     * @return
     */
    @PostMapping("/updateById/{uuid}")
    boolean updateUserAgeById(@PathVariable("uuid") String uuid, @RequestBody Integer age);

    /**
     * 根据用户ID删除用户
     * @param uuid 用户ID
     * @return
     */
    @DeleteMapping("/deleteUserById/{uuid}")
    boolean deleteUserById(@PathVariable("uuid") String uuid);
}
```

6、API实现

```java
import com.fengwenyi.data.API;
import com.fengwenyi.data.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
@Slf4j
public class FeignAPIApplication implements API {

    public static void main(String[] args) {
        SpringApplication.run(FeignAPIApplication.class, args);

//        ApplicationContextAware
//        BeanUtils
    }

    Map<String, User> userMap = new HashMap<>();

    @Override
    public List<User> getUsers() {
        if (userMap == null || userMap.isEmpty())
            return null;
        List<User> userList = new ArrayList<>();
        for (String uuid : userMap.keySet()) {
            userList.add(userMap.get(uuid));
        }
        return userList;
    }

    @Override
    public User getUserById(@PathVariable String uuid) {
        if (userMap == null || userMap.isEmpty() || StringUtils.isEmpty(uuid))
            return null;
        return userMap.get(uuid);
    }

    @Override
    public boolean addUser(@RequestBody User user) {
        if (user == null)
            return false;

        String uuid = user.getUuid();

        if (uuid == null)
            return false;

        if (userMap.get(uuid) != null)
            return false;

        User lastUser = userMap.put(uuid, user);
        if (lastUser != null)
            log.warn("uuid对应的user已被替换，uuid={}, lastUser={}, user={}", uuid, lastUser, user);

        return true;
    }

    @Override
    public boolean updateUserById(@PathVariable String uuid, @RequestBody User user) {
        if (user == null || uuid == null)
            return false;

        if (userMap.get(uuid) == null)
            return false;

        User lastUser = userMap.put(uuid, user);
        if (lastUser != null)
            log.warn("uuid对应的user已被替换，uuid={}, lastUser={}, user={}", uuid, lastUser, user);

        return true;
    }

    @Override
    public boolean updateUserAgeById(@PathVariable String uuid, @RequestBody Integer age) {
        if (age == null || uuid == null || age < -1)
            return false;

        User user = userMap.get(uuid);
        if (user == null)
            return false;

        User lastUser = userMap.put(uuid, user.setAge(age));
        if (lastUser != null)
            log.warn("uuid对应的user已被替换，uuid={}, lastUser={}, user={}", uuid, lastUser, user);

        return true;
    }

    @Override
    public boolean deleteUserById(@PathVariable String uuid) {
        if (uuid == null)
            return false;

        if (userMap.get(uuid) == null)
            return false;

        User lastUser = userMap.remove(uuid);

        if (lastUser != null)
            log.warn("uuid对应的user已被删除，uuid={}, lastUser={}", uuid, lastUser);

        return true;
    }
}
```

7、API继承

```java
import com.fengwenyi.data.API;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

/**
 * @author Wenyi Feng
 * @since 2018-09-15
 */
@FeignClient("feignapi")
public interface APIClient extends API {
}
```

8、写调用测试代码

```java
import com.fengwenyi.data.model.User;
import com.fengwenyi.javalib.util.StringUtil;
import com.fengwenyi.javalib.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@RestController
public class FeignUseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignUseApplication.class, args);
    }

    @Autowired
    private APIClient apiClient;


    @GetMapping("/add/{name}/{age}")
    public Object add(@PathVariable("name") String name, @PathVariable("age") Integer age) {
        if (StringUtil.isEmpty(name)
                || age == null
                || age < 0)
            return false;
        return apiClient.addUser(new User()
                .setUuid(Utils.getUUID())
                .setName("张三")
                .setAge(20));
    }

    @GetMapping("/updateUser/{uuid}")
    public Object updateUser(@PathVariable("uuid") String uuid) {
        if (StringUtil.isEmpty(uuid))
            return false;
        User user = apiClient.getUserById(uuid);
        if (user == null)
            return false;
        return apiClient.updateUserById(uuid,
                user.setName("张三 - Zhangsan")
                        .setAge(21));
    }

    @GetMapping("/update/{uuid}")
    public Object update(@PathVariable("uuid") String uuid) {
        if (StringUtil.isEmpty(uuid))
            return false;
        User user = apiClient.getUserById(uuid);
        if (user == null)
            return false;
        return apiClient.updateUserAgeById(uuid, 23);
    }

    @GetMapping("/delete/{uuid}")
    public Object delete(@PathVariable("uuid") String uuid) {
        if (StringUtil.isEmpty(uuid))
            return false;
        User user = apiClient.getUserById(uuid);
        if (user == null)
            return false;
        return apiClient.deleteUserById(uuid);
    }

    @GetMapping("gets")
    public Object gets() {
        return apiClient.getUsers();
    }

    @GetMapping("/get/{uuid}")
    public Object get(@PathVariable("uuid") String uuid) {
        if (StringUtil.isEmpty(uuid))
            return null;
        return apiClient.getUserById(uuid);
    }
}
```

## 关于测试

![使用测试工具测试API](https://upload-images.jianshu.io/upload_images/5805596-d935c3c5d113f730.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1、添加操作

```
http://localhost:8080/add/张三/19
http://localhost:8080/add/李四/18
http://localhost:8080/add/王五/17
```

2、查询

我们通过这个接口，看一下添加的情况：

```
http://localhost:8080/gets
```

响应
不好意思，上面代码有点问题。修改了下。

```
[
    {
        "uuid":"fddde49a35fe4947950571a93ebfaa1d",
        "name":"张三",
        "age":19
    },
    {
        "uuid":"e136860677a7463d8bcc3c88e0801931",
        "name":"王五",
        "age":17
    },
    {
        "uuid":"b440ebdf36964b62aea2025549409d4a",
        "name":"李四",
        "age":18
    }
]
```

单个查询

```
http://localhost:8080/get/e136860677a7463d8bcc3c88e0801931
```

响应

```
{
    "uuid":"e136860677a7463d8bcc3c88e0801931",
    "name":"王五",
    "age":17
}
```

3、修改操作

```
http://localhost:8080/updateUser/e136860677a7463d8bcc3c88e0801931
```

修改之后，数据是这样子的

```
[
    {
        "uuid":"fddde49a35fe4947950571a93ebfaa1d",
        "name":"张三",
        "age":19
    },
    {
        "uuid":"e136860677a7463d8bcc3c88e0801931",
        "name":"张三 - Zhangsan",
        "age":21
    },
    {
        "uuid":"b440ebdf36964b62aea2025549409d4a",
        "name":"李四",
        "age":18
    }
]
```

4、删除

```
http://localhost:8080/delete/b440ebdf36964b62aea2025549409d4a
```

删除之后，数据是这样子的

```
[
    {
        "uuid":"fddde49a35fe4947950571a93ebfaa1d",
        "name":"张三",
        "age":19
    },
    {
        "uuid":"e136860677a7463d8bcc3c88e0801931",
        "name":"张三 - Zhangsan",
        "age":21
    }
]
```

5、看一下控制台

![控制台打印日志（部分截图）](https://upload-images.jianshu.io/upload_images/5805596-0d0b55c67c05d2f6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
2018-09-15 13:54:34.304  WARN 9732 --- [qtp489047267-34] c.fengwenyi.service.FeignAPIApplication  : 
uuid对应的user已被替换，uuid=e136860677a7463d8bcc3c88e0801931, 
lastUser=User(uuid=e136860677a7463d8bcc3c88e0801931, name=王五, age=17), 
user=User(uuid=e136860677a7463d8bcc3c88e0801931, name=张三 - Zhangsan, age=21)

2018-09-15 13:56:18.367  WARN 9732 --- [qtp489047267-35] c.fengwenyi.service.FeignAPIApplication  : 
uuid对应的user已被删除，uuid=b440ebdf36964b62aea2025549409d4a, 
lastUser=User(uuid=b440ebdf36964b62aea2025549409d4a, name=李四, age=18)
```


![警告提醒](https://upload-images.jianshu.io/upload_images/5805596-82f8bb5cf3e230bc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 测试代码

点击[这里](https://github.com/fengwenyi/FeignExample)，查看本节测试代码。

大抵就是这样子，感谢。


