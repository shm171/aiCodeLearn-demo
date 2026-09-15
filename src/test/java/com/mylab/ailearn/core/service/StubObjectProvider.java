package com.mylab.ailearn.core.service;

import org.springframework.beans.factory.ObjectProvider;

/**
 * 测试用的 {@link ObjectProvider} 桩：固定返回给定实例（可以为 null）。
 *
 * <p>不依赖 Mockito，便于在受限环境下运行。</p>
 */
final class StubObjectProvider {

    private StubObjectProvider() {
    }

    /** 始终把 {@code instance} 当作可用的 bean 返回；传 null 表示没有该 bean。 */
    static <T> ObjectProvider<T> of(T instance) {
        return new ObjectProvider<>() {
            @Override
            public T getObject() {
                if (instance == null) {
                    throw new IllegalStateException("没有可用的 bean");
                }
                return instance;
            }

            @Override
            public T getObject(Object... args) {
                return getObject();
            }

            @Override
            public T getIfAvailable() {
                return instance;
            }

            @Override
            public T getIfUnique() {
                return instance;
            }
        };
    }
}
