package net.lunovastudio.lobotomycorp.content.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.KnowledgeReward;
import net.lunovastudio.lobotomycorp.api.research.ResearchRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 研究阶段的数据包实现。
 */
public record ResearchStage(
        String textKey,
        List<ResourceLocation> recipes,
        List<ResearchRequirement> obtain,
        List<ResearchRequirement> craft,
        List<KnowledgeReward> knowledge,
        List<KnowledgeReward> requiredKnowledge,
        List<ResourceLocation> requiredResearch
) implements IResearchStage {
    public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("text").forGetter(ResearchStage::textKey),
                    ResourceLocation.CODEC
                            .listOf()
                            .optionalFieldOf("recipes", List.of())
                            .forGetter(ResearchStage::recipes),
                    ResearchRequirement.CODEC
                            .listOf()
                            .optionalFieldOf("obtain", List.of())
                            .forGetter(ResearchStage::obtain),
                    ResearchRequirement.CODEC
                            .listOf()
                            .optionalFieldOf("craft", List.of())
                            .forGetter(ResearchStage::craft),
                    KnowledgeReward.CODEC
                            .listOf()
                            .optionalFieldOf("knowledge", List.of())
                            .forGetter(ResearchStage::knowledge),
                    KnowledgeReward.CODEC
                            .listOf()
                            .optionalFieldOf("required_knowledge", List.of())
                            .forGetter(ResearchStage::requiredKnowledge),
                    ResourceLocation.CODEC
                            .listOf()
                            .optionalFieldOf("required_research", List.of())
                            .forGetter(ResearchStage::requiredResearch))
            .apply(instance, ResearchStage::new));

    public ResearchStage {
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        obtain = obtain == null ? List.of() : List.copyOf(obtain);
        craft = craft == null ? List.of() : List.copyOf(craft);
        knowledge = knowledge == null ? List.of() : List.copyOf(knowledge);
        requiredKnowledge = requiredKnowledge == null ? List.of() : List.copyOf(requiredKnowledge);
        requiredResearch = requiredResearch == null ? List.of() : List.copyOf(requiredResearch);
    }

    /** 本阶段是否有任何需要玩家满足的要求。 */
    public boolean hasRequirements() {
        return !obtain.isEmpty()
                || !craft.isEmpty()
                || !requiredKnowledge.isEmpty()
                || !requiredResearch.isEmpty();
    }
}
