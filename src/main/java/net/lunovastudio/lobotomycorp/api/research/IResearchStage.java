package net.lunovastudio.lobotomycorp.api.research;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * {@link IResearchEntry} 里的一个阶段。
 *
 * <p>阶段按顺序推进：界面会展示该阶段引用的配方，玩家需要满足收集/合成/前置研究/知识点数
 * 要求后才能进入下一阶段。</p>
 */
public interface IResearchStage {
    /** 阶段正文的翻译键。 */
    String textKey();

    /** 本阶段界面里展示的配方 id。 */
    List<ResourceLocation> recipes();

    /** 推进所需收集的物品。 */
    List<ResearchRequirement> obtain();

    /** 推进所需合成的物品。 */
    List<ResearchRequirement> craft();

    /** 完成本阶段时获得的知识点。 */
    List<KnowledgeReward> knowledge();

    /**
     * 推进本阶段所需持有的知识点数。完成阶段时会被扣除。
     */
    default List<KnowledgeReward> requiredKnowledge() {
        return List.of();
    }

    /** 推进本阶段前必须完成的其他研究条目。 */
    List<ResourceLocation> requiredResearch();
}
