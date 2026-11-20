package net.lunovastudio.lobotomycorp.client.screen.research;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * 研究界面的跨屏幕视图状态。
 *
 * <p>关闭界面再打开时回到上次的位置，与参考项目的 {@code persisted*} 字段同构。</p>
 */
public final class ResearchScreenState {
    /** 研究界面视图。 */
    public enum Mode {
        /** 部门生命之树。 */
        TREE,
        /** 部门内的条目网格。 */
        DEPARTMENT
    }

    private static Mode mode = Mode.TREE;
    private static @Nullable ResourceLocation category;
    private static @Nullable ResourceLocation entry;

    private ResearchScreenState() {
    }

    public static Mode mode() {
        return mode;
    }

    public static void setMode(Mode next) {
        mode = next == null ? Mode.TREE : next;
    }

    public static @Nullable ResourceLocation category() {
        return category;
    }

    public static void setCategory(@Nullable ResourceLocation next) {
        category = next;
    }

    public static @Nullable ResourceLocation entry() {
        return entry;
    }

    public static void setEntry(@Nullable ResourceLocation next) {
        entry = next;
    }

    public static void reset() {
        mode = Mode.TREE;
        category = null;
        entry = null;
    }
}
