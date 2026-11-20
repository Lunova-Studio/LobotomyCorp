package net.lunovastudio.lobotomycorp.api.research;

import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 研究进度相关事件。全部在服务端触发；取消事件会阻止对应的状态写入。
 */
public abstract class ResearchEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;

    protected ResearchEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }

    /** 条目即将被解锁。 */
    public static final class Unlocked extends ResearchEvent {
        private final ResourceLocation entry;

        public Unlocked(ServerPlayer player, ResourceLocation entry) {
            super(player);
            this.entry = entry;
        }

        public ResourceLocation entry() {
            return entry;
        }
    }

    /** 条目阶段即将推进。 */
    public static final class StageAdvanced extends ResearchEvent {
        private final ResourceLocation entry;
        private final int from;
        private final int to;

        public StageAdvanced(ServerPlayer player, ResourceLocation entry, int from, int to) {
            super(player);
            this.entry = entry;
            this.from = from;
            this.to = to;
        }

        public ResourceLocation entry() {
            return entry;
        }

        public int from() {
            return from;
        }

        public int to() {
            return to;
        }
    }

    /** 条目即将完成。 */
    public static final class Completed extends ResearchEvent {
        private final ResourceLocation entry;

        public Completed(ServerPlayer player, ResourceLocation entry) {
            super(player);
            this.entry = entry;
        }

        public ResourceLocation entry() {
            return entry;
        }
    }

    /** 知识点即将增加。 */
    public static final class KnowledgeGained extends ResearchEvent {
        private final KnowledgeType type;
        private final Holder<IResearchCategory> category;
        private final int amount;

        public KnowledgeGained(ServerPlayer player, KnowledgeType type,
                               Holder<IResearchCategory> category, int amount) {
            super(player);
            this.type = type;
            this.category = category;
            this.amount = amount;
        }

        public KnowledgeType type() {
            return type;
        }

        public Holder<IResearchCategory> category() {
            return category;
        }

        public int amount() {
            return amount;
        }
    }
}
