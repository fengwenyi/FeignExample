package com.fengwenyi.data.model;

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
