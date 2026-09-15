package com.mylab.ailearn.core.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service 层公共小工具：参数校验、SPI 端口解析与空列表兜底，避免在各 Service 中重复。
 */
final class ServiceSupport {

    private ServiceSupport() {}

    /** 空列表兜底。 */
    static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    /** 校验 ownerUserId 有效，否则抛 400。 */
    static void requireOwner(Long ownerUserId) {
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerUserId 无效");
        }
    }

    /** 解析 SPI 端口实现；未实现时抛出明确异常。 */
    static <T> T required(ObjectProvider<T> provider, String name) {
        T bean = provider.getIfAvailable();
        if (bean == null) {
            throw new IllegalStateException(name + " 尚未由 Repository 负责人实现");
        }
        return bean;
    }
}
