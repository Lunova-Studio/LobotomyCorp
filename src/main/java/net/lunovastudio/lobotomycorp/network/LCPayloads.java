package net.lunovastudio.lobotomycorp.network;

import net.lunovastudio.lobotomycorp.LCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 全部研究相关网络包的注册。
 *
 * <p>注意：引用客户端处理器时一律使用 {@code (payload, context) -> Handler.handle(...)} 形式的
 * lambda，而不是方法引用。方法引用会在注册阶段解析客户端类，导致专用服务器启动崩溃。</p>
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class LCPayloads {
    public static final String VERSION = "1";

    private LCPayloads() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.playToServer(
                ServerboundAdvanceStagePayload.TYPE,
                ServerboundAdvanceStagePayload.STREAM_CODEC,
                ServerboundAdvanceStagePayload::handle);
        registrar.playToServer(
                ServerboundUnlockResearchPayload.TYPE,
                ServerboundUnlockResearchPayload.STREAM_CODEC,
                ServerboundUnlockResearchPayload::handle);
        registrar.playToServer(
                ServerboundClearResearchFlagsPayload.TYPE,
                ServerboundClearResearchFlagsPayload.STREAM_CODEC,
                ServerboundClearResearchFlagsPayload::handle);

        registrar.playToClient(
                ClientboundKnowledgeGainPayload.TYPE,
                ClientboundKnowledgeGainPayload.STREAM_CODEC,
                (payload, context) ->
                        net.lunovastudio.lobotomycorp.client.network.KnowledgeGainClientHandler.handle(payload, context));
    }
}
