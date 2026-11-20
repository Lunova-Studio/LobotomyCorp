package net.lunovastudio.lobotomycorp.registry;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.content.career.PlayerCareerData;
import net.lunovastudio.lobotomycorp.content.psychology.PsychologicalData;
import net.lunovastudio.lobotomycorp.content.research.PlayerKnowledge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 本模组的玩家/实体数据附件。
 */
public final class LCAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LCIds.MODID);

    /** 理智值：随玩家存档并同步给本人。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PsychologicalData>> PSYCHOLOGICAL =
            ATTACHMENT_TYPES.register("psychological",
                    () -> AttachmentType.builder(() -> new PsychologicalData(PsychologicalData.MAX_PSYCHOLOGICAL))
                            .serialize(PsychologicalData.CODEC)
                            .sync(PsychologicalData.STREAM_CODEC)
                            .build());

    /** 员工档案：员工等级与部门权限，随存档并同步给本人。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCareerData>> CAREER =
            ATTACHMENT_TYPES.register("career",
                    () -> AttachmentType.builder(PlayerCareerData::empty)
                            .serialize(PlayerCareerData.CODEC)
                            .sync((holder, player) -> holder == player, PlayerCareerData.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    /** 研究进度：服务端权威，随存档、死亡保留，并只同步给拥有者。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerKnowledge>> KNOWLEDGE =
            ATTACHMENT_TYPES.register("knowledge",
                    () -> AttachmentType.builder(PlayerKnowledge::new)
                            .serialize(PlayerKnowledge.CODEC.codec())
                            .sync((holder, player) -> holder == player, PlayerKnowledge.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    private LCAttachments() {
    }

    public static AttachmentType<PsychologicalData> psychological() {
        return PSYCHOLOGICAL.get();
    }

    public static AttachmentType<PlayerCareerData> career() {
        return CAREER.get();
    }

    public static AttachmentType<PlayerKnowledge> knowledge() {
        return KNOWLEDGE.get();
    }

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
