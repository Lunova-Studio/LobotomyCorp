package net.lunovastudio.lobotomycorp;

import net.minecraft.resources.ResourceLocation;

/**
 * 模组 id 与本模组内共享的资源位置。
 */
public final class LCIds {
    public static final String MODID = "lobotomycorp";

    /** 员工档案附件 id。 */
    public static final ResourceLocation CAREER = rl("career");
    /** 研究进度附件 id。 */
    public static final ResourceLocation KNOWLEDGE = rl("knowledge");
    /** 理智值附件 id。 */
    public static final ResourceLocation PSYCHOLOGICAL = rl("psychological");
    /** 研究界面默认图标。 */
    public static final ResourceLocation DEFAULT_RESEARCH_ICON = rl("textures/gui/projects/task.png");

    private LCIds() {
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation rl(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
