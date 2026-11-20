package net.lunovastudio.lobotomycorp.client.hud;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 知识点获得提示：在屏幕右侧短暂浮出“+N 知识名”。
 */
@EventBusSubscriber(modid = LCIds.MODID, value = Dist.CLIENT)
public final class KnowledgeGainOverlay {
    private static final List<Tracker> TRACKERS = new ArrayList<>();
    private static final int MAX_VISIBLE = 8;

    private KnowledgeGainOverlay() {
    }

    /**
     * 添加一条提示。同一 tick 内多次添加会在渲染时逐条上浮。
     */
    public static void addTracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category,
                                  int duration, long seed) {
        TRACKERS.add(new Tracker(type, category, Math.max(1, duration), seed));
        while (TRACKERS.size() > MAX_VISIBLE) {
            TRACKERS.removeFirst();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (TRACKERS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int index = 0;
        Iterator<Tracker> iterator = TRACKERS.iterator();
        while (iterator.hasNext()) {
            Tracker tracker = iterator.next();
            tracker.age++;
            if (tracker.age > tracker.duration) {
                iterator.remove();
                continue;
            }
            float alpha = fade(tracker);
            Component text = Component.literal("+1 ")
                    .append(Component.translatable(tracker.type.translationKey()));
            int x = width - font.width(text) - 10;
            int y = height / 2 - 24 - index * 10 - Math.round(tracker.age / 6.0F);
            graphics.drawString(font, text, x, y, color(tracker.type, alpha), true);
            index++;
        }
    }

    private static float fade(Tracker tracker) {
        float progress = tracker.age / (float) tracker.duration;
        if (progress < 0.15F) {
            return progress / 0.15F;
        }
        if (progress > 0.7F) {
            return Mth.clamp((1.0F - progress) / 0.3F, 0.0F, 1.0F);
        }
        return 1.0F;
    }

    private static int color(KnowledgeType type, float alpha) {
        int base = type == KnowledgeType.THEORY ? 0xFFCC88FF : 0xFF66DDFF;
        int a = (int) (alpha * 255.0F);
        return (a << 24) | (base & 0x00FFFFFF);
    }

    private static final class Tracker {
        private final KnowledgeType type;
        private final ResourceKey<IResearchCategory> category;
        private final int duration;
        private final long seed;
        private int age;

        private Tracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category,
                        int duration, long seed) {
            this.type = type;
            this.category = category;
            this.duration = duration;
            this.seed = seed;
        }
    }
}
