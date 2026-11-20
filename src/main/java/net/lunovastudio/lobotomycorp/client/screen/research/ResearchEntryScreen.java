package net.lunovastudio.lobotomycorp.client.screen.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.capability.ResearchFlag;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.KnowledgeReward;
import net.lunovastudio.lobotomycorp.api.research.ResearchIcon;
import net.lunovastudio.lobotomycorp.api.research.ResearchRequirement;
import net.lunovastudio.lobotomycorp.client.render.research.ChamferGeometry;
import net.lunovastudio.lobotomycorp.client.render.research.EntryNodeRenderer;
import net.lunovastudio.lobotomycorp.client.render.research.RequirementWidget;
import net.lunovastudio.lobotomycorp.client.render.research.StageNavigatorWidget;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.lunovastudio.lobotomycorp.network.ServerboundAdvanceStagePayload;
import net.lunovastudio.lobotomycorp.network.ServerboundClearResearchFlagsPayload;
import net.lunovastudio.lobotomycorp.network.ServerboundUnlockResearchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 条目详情页：阶段翻页、要求说明与“推进阶段”操作。
 */
public final class ResearchEntryScreen extends Screen {
    private static final int PANEL_WIDTH = 384;
    private static final int PANEL_HEIGHT = 236;
    private static final int BAND_HEIGHT = 8;
    private static final int PADDING = 14;
    private static final int BUTTON_WIDTH = 132;
    private static final int BUTTON_HEIGHT = 18;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int BODY_COLOR = 0xFFD0D0D0;
    private static final int MUTED_COLOR = 0xFF9E9E9E;
    private static final int OK_COLOR = 0xFF7CFF7C;
    private static final int LOCKED_COLOR = 0xFFFF5555;
    private static final int DONE_COLOR = 0xFFFFD700;

    private final ResourceLocation entryId;
    private int page;
    private boolean hoverBack;
    private boolean hoverAction;
    private boolean hoverLeft;
    private boolean hoverRight;

    public ResearchEntryScreen(ResourceLocation entryId) {
        super(Component.empty());
        this.entryId = entryId;
    }

    @Override
    protected void init() {
        super.init();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        IResearchEntry entry = ResearchManager.entryOf(player, this.entryId);
        int stageCount = entry == null ? 1 : entry.stages().size();
        this.page = Mth.clamp(knowledge.stageOf(this.entryId), 0, Math.max(0, stageCount - 1));
        if (knowledge.hasCompleted(this.entryId)) {
            this.page = Math.max(0, stageCount - 1);
        }
        if (knowledge.hasFlag(this.entryId, ResearchFlag.PAGE) || knowledge.hasFlag(this.entryId, ResearchFlag.POPUP)) {
            PacketDistributor.sendToServer(new ServerboundClearResearchFlagsPayload(this.entryId));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        IResearchEntry entry = ResearchManager.entryOf(player, this.entryId);
        if (entry == null) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("research.lobotomycorp.entry.missing", this.entryId.toString()),
                    this.width / 2, this.height / 2, LOCKED_COLOR);
            return;
        }
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        IResearchCategory category = entry.category().value();
        int accent = ChamferGeometry.multiply(category.color(), 0.9F);

        ChamferGeometry.fillChamferedBordered(graphics, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 8,
                accent, 0xF0121212);
        ChamferGeometry.hazardBand(graphics, panelX + 1, panelY + 1, PANEL_WIDTH - 2, BAND_HEIGHT,
                accent, 4, 3);

        Component title = Component.translatable(entry.nameKey());
        Component fitted = EntryNodeRenderer.fit(this.font, title, PANEL_WIDTH - PADDING * 2 - 40);
        graphics.drawCenteredString(this.font, fitted, panelX + PANEL_WIDTH / 2, panelY + BAND_HEIGHT + 6, TITLE_COLOR);
        graphics.drawString(this.font, Component.translatable(category.nameKey()),
                panelX + PADDING, panelY + BAND_HEIGHT + 18, MUTED_COLOR, false);

        this.hoverBack = this.inside(mouseX, mouseY, panelX + PADDING, panelY + BAND_HEIGHT + 4, 56, 14);
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.screen.back"),
                panelX + PADDING, panelY + BAND_HEIGHT + 8,
                this.hoverBack ? TITLE_COLOR : MUTED_COLOR, false);

        IResearchStage stage = entry.stages().get(Mth.clamp(this.page, 0, entry.stages().size() - 1));
        int navigatorY = panelY + BAND_HEIGHT + 16;
        int navigatorX = panelX + PANEL_WIDTH - PADDING - 40;
        StageNavigatorWidget.draw(graphics, this.font, navigatorX, navigatorY, this.page,
                entry.stages().size(), MUTED_COLOR, this.hoverLeft, this.hoverRight);
        this.hoverLeft = StageNavigatorWidget.hitLeft(mouseX, mouseY, navigatorX, this.font, navigatorY,
                this.page, entry.stages().size());
        this.hoverRight = StageNavigatorWidget.hitRight(mouseX, mouseY, navigatorX, this.font, navigatorY,
                this.page, entry.stages().size());

        int bodyX = panelX + PADDING;
        int bodyWidth = PANEL_WIDTH - PADDING * 2;
        int cursorY = panelY + BAND_HEIGHT + 32;
        graphics.drawString(this.font, Component.translatable(stage.textKey()),
                bodyX, cursorY, BODY_COLOR, false);
        cursorY += this.font.lineHeight + 4;

        cursorY = this.renderRequirements(graphics, player, entry, stage, bodyX, cursorY, bodyWidth);
        cursorY = this.renderKnowledge(graphics, entry, bodyX, cursorY, bodyWidth);
        this.renderRecipes(graphics, stage, bodyX, cursorY, bodyWidth);

        this.renderAction(graphics, player, entry, panelX, panelY, mouseX, mouseY);
    }

    private int renderRequirements(GuiGraphics graphics, LocalPlayer player, IResearchEntry entry,
                                   IResearchStage stage, int x, int y, int width) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        List<Component> reasons = ResearchManager.lockedReasons(player, entry, this.entryId, this.page);
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.entry.requirements"),
                x, y, MUTED_COLOR, false);
        y += RequirementWidget.lineHeight();

        if (reasons.isEmpty()) {
            String text = knowledge.hasCompleted(this.entryId) ? "research.lobotomycorp.entry.completed"
                    : "research.lobotomycorp.entry.ready";
            graphics.drawString(this.font, Component.translatable(text), x, y,
                    knowledge.hasCompleted(this.entryId) ? DONE_COLOR : OK_COLOR, false);
            y += RequirementWidget.lineHeight();
        } else {
            for (Component reason : reasons) {
                y = RequirementWidget.drawLine(graphics, this.font, x, y, width, "-", reason, LOCKED_COLOR);
            }
        }
        for (ResearchRequirement requirement : stage.obtain()) {
            int held = ResearchManager.countMatching(player, requirement);
            y = RequirementWidget.drawLine(graphics, this.font, x, y, width, ">",
                    Component.translatable("research.lobotomycorp.entry.obtain", held, requirement.amount()),
                    BODY_COLOR);
        }
        for (ResourceLocation required : stage.requiredResearch()) {
            y = RequirementWidget.drawLine(graphics, this.font, x, y, width, ">",
                    Component.translatable("research.lobotomycorp.entry.requires", required.toString()),
                    BODY_COLOR);
        }
        return y + 2;
    }

    private int renderKnowledge(GuiGraphics graphics, IResearchEntry entry, int x, int y, int width) {
        IResearchStage stage = entry.stages().get(Mth.clamp(this.page, 0, entry.stages().size() - 1));
        if (stage.knowledge().isEmpty() && stage.requiredKnowledge().isEmpty()) {
            return y;
        }
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.entry.knowledge"),
                x, y, MUTED_COLOR, false);
        y += RequirementWidget.lineHeight();
        for (KnowledgeReward reward : stage.knowledge()) {
            y = RequirementWidget.drawLine(graphics, this.font, x, y, width, "+",
                    Component.translatable("research.lobotomycorp.entry.knowledge.line",
                            reward.amount(), Component.translatable(reward.type().translationKey())),
                    OK_COLOR);
        }
        for (KnowledgeReward required : stage.requiredKnowledge()) {
            y = RequirementWidget.drawLine(graphics, this.font, x, y, width, "-",
                    Component.translatable("research.lobotomycorp.entry.knowledge.line",
                            required.amount(), Component.translatable(required.type().translationKey())),
                    BODY_COLOR);
        }
        return y + 2;
    }

    private void renderRecipes(GuiGraphics graphics, IResearchStage stage, int x, int y, int width) {
        if (stage.recipes().isEmpty()) {
            return;
        }
        String joined = stage.recipes().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.entry.recipes"),
                x, y, MUTED_COLOR, false);
        y += RequirementWidget.lineHeight();
        for (var line : this.font.split(Component.literal(joined), width)) {
            graphics.drawString(this.font, line, x, y, BODY_COLOR, false);
            y += RequirementWidget.lineHeight();
        }
    }

    private void renderAction(GuiGraphics graphics, LocalPlayer player, IResearchEntry entry,
                              int panelX, int panelY, int mouseX, int mouseY) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        boolean known = knowledge.knows(this.entryId);
        boolean completed = knowledge.hasCompleted(this.entryId);
        Component label;
        int color;
        if (completed) {
            label = Component.translatable("research.lobotomycorp.entry.completed");
            color = DONE_COLOR;
        } else if (!known) {
            label = Component.translatable("research.lobotomycorp.entry.start");
            color = OK_COLOR;
        } else {
            label = Component.translatable("research.lobotomycorp.entry.advance");
            color = accentColor(entry);
        }
        int buttonX = panelX + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = panelY + PANEL_HEIGHT - BUTTON_HEIGHT - 10;
        this.hoverAction = this.inside(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        ChamferGeometry.fillChamferedBordered(graphics, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, 4,
                ChamferGeometry.multiply(color, this.hoverAction ? 1.0F : 0.7F),
                this.hoverAction ? 0xF0202020 : 0xF0161616);
        graphics.drawCenteredString(this.font, label, buttonX + BUTTON_WIDTH / 2, buttonY + 5,
                completed ? DONE_COLOR : TITLE_COLOR);
    }

    private static int accentColor(IResearchEntry entry) {
        return entry.category().value().color() | 0xFF000000;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        IResearchEntry entry = ResearchManager.entryOf(player, this.entryId);
        if (entry == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        if (this.inside(mouseX, mouseY, panelX + PADDING, panelY + BAND_HEIGHT + 4, 56, 14)) {
            this.onClose();
            return true;
        }
        int navigatorY = panelY + BAND_HEIGHT + 16;
        int navigatorX = panelX + PANEL_WIDTH - PADDING - 40;
        if (StageNavigatorWidget.hitLeft(mouseX, mouseY, navigatorX, this.font, navigatorY,
                this.page, entry.stages().size()) && this.page > 0) {
            this.page--;
            return true;
        }
        if (StageNavigatorWidget.hitRight(mouseX, mouseY, navigatorX, this.font, navigatorY,
                this.page, entry.stages().size())
                && this.page < entry.stages().size() - 1) {
            this.page++;
            return true;
        }
        int buttonX = panelX + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = panelY + PANEL_HEIGHT - BUTTON_HEIGHT - 10;
        if (this.inside(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            this.performAction(player);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void performAction(LocalPlayer player) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        if (knowledge.hasCompleted(this.entryId)) {
            return;
        }
        if (!knowledge.knows(this.entryId)) {
            PacketDistributor.sendToServer(new ServerboundUnlockResearchPayload(this.entryId));
            return;
        }
        PacketDistributor.sendToServer(new ServerboundAdvanceStagePayload(this.entryId));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_H) {
            this.onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT && this.page > 0) {
            this.page--;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            LocalPlayer player = Minecraft.getInstance().player;
            IResearchEntry entry = player == null ? null : ResearchManager.entryOf(player, this.entryId);
            if (entry != null && this.page < entry.stages().size() - 1) {
                this.page++;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        ResearchScreenState.setEntry(this.entryId);
        Minecraft.getInstance().setScreen(ResearchBrowserScreen.reopen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Nullable
    private static ResearchIcon firstIcon(IResearchEntry entry) {
        return entry.icons().isEmpty() ? null : entry.icons().getFirst();
    }
}
