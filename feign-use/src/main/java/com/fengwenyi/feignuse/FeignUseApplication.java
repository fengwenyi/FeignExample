package com.fengwenyi.feignuse;

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
                .setName(name)
                .setAge(age));
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
