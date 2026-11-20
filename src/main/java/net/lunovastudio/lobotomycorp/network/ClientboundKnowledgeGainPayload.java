package net.lunovastudio.lobotomycorp.network;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

/**
 * 服务端通知客户端“刚刚获得了知识点”，用于屏幕角落的浮字提示。
 *
 * @param type     知识种类
 * @param category 目标分类，空表示全局
 * @param count    本次获得的点数
 */
public record ClientboundKnowledgeGainPayload(
        KnowledgeType knowledgeType, Optional<ResourceKey<IResearchCategory>> category, int count)
        implements CustomPacketPayload {

    public static final Type<ClientboundKnowledgeGainPayload> TYPE =
            new Type<>(LCIds.rl("knowledge_gain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundKnowledgeGainPayload> STREAM_CODEC =
            StreamCodec.composite(
                    KnowledgeType.STREAM_CODEC,
                    ClientboundKnowledgeGainPayload::knowledgeType,
                    ByteBufCodecs.optional(ResourceKey.streamCodec(IResearchCategory.REGISTRY_KEY)),
                    ClientboundKnowledgeGainPayload::category,
                    ByteBufCodecs.VAR_INT,
                    ClientboundKnowledgeGainPayload::count,
                    ClientboundKnowledgeGainPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
