package com.freddy.chat.event;

/**
 * 对象池中的对象要求实现PoolableObject接口
 */
public interface PoolableObject {

    /**
     * 恢复到默认状态
     */
    void reset();
}
