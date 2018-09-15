package com.fengwenyi.data;

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
