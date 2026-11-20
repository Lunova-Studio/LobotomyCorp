package net.lunovastudio.lobotomycorp.api;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 静态 API 门面背后的实现持有者。
 *
 * <p>每种门面在模组构造期绑定一次；重复绑定会被拒绝，避免附属模组替换实现；
 * 未绑定就调用会立即抛异常，而不是返回一个静默的默认值。</p>
 *
 * @param <T> 门面实现类型
 */
public final class ApiBinding<T> {
    private final String owner;
    private volatile @Nullable T value;

    /**
     * @param owner 出现在错误信息中的门面名称
     */
    public ApiBinding(String owner) {
        this.owner = owner;
    }

    /**
     * 安装实现。
     *
     * @param impl 实现，不能为 null
     * @throws IllegalStateException 已经安装过实现时
     */
    public void bind(T impl) {
        if (value != null) {
            throw new IllegalStateException(owner + " already bound");
        }
        value = Objects.requireNonNull(impl, owner + " binding");
    }

    /**
     * @return 已安装的实现
     * @throws IllegalStateException 尚未绑定任何实现时
     */
    public T get() {
        T current = value;
        if (current == null) {
            throw new IllegalStateException(owner + " used before LobotomyCorp bound it");
        }
        return current;
    }

    /**
     * @return 是否已绑定实现
     */
    public boolean isBound() {
        return value != null;
    }
}
