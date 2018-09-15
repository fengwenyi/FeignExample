package com.fengwenyi.feignuse;

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
