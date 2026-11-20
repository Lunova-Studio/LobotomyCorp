package net.lunovastudio.lobotomycorp.api.research;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * 研究条目。条目是研究界面里的节点，每个条目包含一个或多个按顺序推进的
 * {@link IResearchStage 阶段}。
 *
 * <p>条目从 {@link #REGISTRY_KEY} 数据包注册表加载，路径为
 * {@code data/<namespace>/lobotomycorp/research_entry/}。</p>
 */
public interface IResearchEntry {
    /** 研究条目的数据包注册表键。 */
    ResourceKey<Registry<IResearchEntry>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("lobotomycorp", "research_entry"));

    /** 条目所属分类。 */
    Holder<IResearchCategory> category();

    /** 条目显示名的翻译键。 */
    String nameKey();

    /**
     * 条目可用前必须满足的父条目引用。
     */
    Set<ResearchParent> parents();

    /** 在分类网格里的列。 */
    int column();

    /** 在分类网格里的行。 */
    int row();

    /** 按顺序排列的阶段，永不为空。 */
    List<IResearchStage> stages();

    /** 影响显示与解锁行为的元标记。 */
    Set<ResearchEntryMeta> meta();

    /** 条目节点图标；为空时退回到第一阶段配方或默认图标。 */
    default List<ResearchIcon> icons() {
        return List.of();
    }

    /**
     * 条目完成后解锁的配方 id。服务端据此建立配方门控索引。
     */
    default List<ResourceLocation> unlocksRecipes() {
        return List.of();
    }

    /** 是否带有某个元标记。 */
    default boolean hasMeta(ResearchEntryMeta flag) {
        return meta().contains(flag);
    }
}
