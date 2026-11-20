package net.lunovastudio.lobotomycorp.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

/**
 * 阶段奖励或阶段门槛上的一条知识点数。
 *
 * @param type     知识种类
 * @param category 目标分类
 * @param amount   原始点数，必须为正
 */
public record KnowledgeReward(KnowledgeType type, Holder<IResearchCategory> category, int amount) {
    public static final Codec<KnowledgeReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeReward::type),
                    RegistryFixedCodec.create(IResearchCategory.REGISTRY_KEY)
                            .fieldOf("category")
                            .forGetter(KnowledgeReward::category),
                    Codec.INT.fieldOf("amount").forGetter(KnowledgeReward::amount))
            .apply(instance, KnowledgeReward::new));
}
