package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeAccess;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * {@link KnowledgeAccess} 的默认后端：直接读写玩家研究进度里的知识点计数器。
 *
 * <p>为本项目后续自建知识/aspect 体系预留替换点：绑定其他 {@link KnowledgeAccess.Sink}
 * 即可接管知识点的记账方式。</p>
 */
public final class PlayerKnowledgeSink implements KnowledgeAccess.Sink {
    public static final PlayerKnowledgeSink INSTANCE = new PlayerKnowledgeSink();

    private PlayerKnowledgeSink() {
    }

    @Override
    public boolean grant(ServerPlayer player, KnowledgeType type,
                         @Nullable Holder<IResearchCategory> category, int amount) {
        return ResearchManager.grantKnowledge(player, type, category, amount);
    }

    @Override
    public int points(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) {
        return PlayerResearchAccess.of(player).knowledgePoints(type, category);
    }

    @Override
    public int level(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) {
        return PlayerResearchAccess.of(player).knowledgeLevel(type, category);
    }
}
