package net.lunovastudio.lobotomycorp;

import com.mojang.logging.LogUtils;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.recipe.ResearchGate;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeAccess;
import net.lunovastudio.lobotomycorp.content.research.PlayerKnowledgeSink;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.lunovastudio.lobotomycorp.registry.LCAttributes;
import net.lunovastudio.lobotomycorp.registry.LCItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * 模组入口：只负责注册表与 API 门面的绑定。
 *
 * <p>具体玩法逻辑分散在 {@code content/}、{@code client/}、{@code server/} 里，通过
 * {@code @EventBusSubscriber} 自行挂接事件。</p>
 */
@Mod(LCIds.MODID)
public final class LobotomyCorp {
    public static final String MODID = LCIds.MODID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public LobotomyCorp(IEventBus modBus, ModContainer container) {
        LCItems.register(modBus);
        LCAttributes.register(modBus);
        LCAttachments.register(modBus);

        PlayerResearchAccess.bind(player -> player.getData(LCAttachments.knowledge()));
        PlayerCareerAccess.bind(player -> player.getData(LCAttachments.career()));
        KnowledgeAccess.bind(PlayerKnowledgeSink.INSTANCE);
        ResearchGate.bind(ResearchManager::doesPassGate);
    }
}
