package com.fengwenyi.service;

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
