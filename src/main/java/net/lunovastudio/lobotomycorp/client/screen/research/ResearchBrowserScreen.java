package net.lunovastudio.lobotomycorp.client.screen.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.ResearchIcon;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.lunovastudio.lobotomycorp.client.render.research.ChamferGeometry;
import net.lunovastudio.lobotomycorp.client.render.research.ConnectorRenderer;
import net.lunovastudio.lobotomycorp.client.render.research.EntryNodeRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 研究界面：部门生命之树 → 部门内条目网格。
 *
 * <p>结构对齐参考项目的浏览器式界面（分类栏/节点网格/条目详情页），视觉沿用本项目的
 * 生命之树：深色切角节点、部门色条纹、细灰连线、打开时从 30% 缓动到 100%。</p>
 */
public final class ResearchBrowserScreen extends Screen {
    private static final int NODE_WIDTH = 116;
    private static final int NODE_HEIGHT = 46;
    private static final int GRID_WIDTH = 138;
    private static final int GRID_HEIGHT = 44;
    private static final int GRID_GAP_X = 14;
    private static final int GRID_GAP_Y = 12;
    private static final int MARGIN = 26;
    private static final int TOP_BAR = 34;
    private static final int OPEN_ANIMATION_MS = 300;
    private static final float HOVER_SCALE = 0.1F;
    private static final float HOVER_LERP = 0.35F;
    private static final int LOCKED_COLOR = 0xFFFF5555;
    private static final int STATUS_COLOR = 0xFFE8E8E8;
    private static final int DONE_COLOR = 0xFF7CFF7C;
    private static final int IDLE_COLOR = 0xFF8C8C8C;

    private final List<Holder.Reference<IResearchCategory>> categories = new ArrayList<>();
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final Map<ResourceLocation, Float> hoverAnimation = new HashMap<>();
    private final List<Component> pendingTooltip = new ArrayList<>();

    private @Nullable Holder.Reference<IResearchCategory> activeCategory;
    private ResearchScreenState.Mode mode = ResearchScreenState.mode();
    private long openedAt;
    private boolean hoveringBack;

    public ResearchBrowserScreen() {
        super(Component.translatable("research.lobotomycorp.screen.title"));
    }

    /**
     * 打开研究界面（沿用上次的视图状态）。
     */
    public static ResearchBrowserScreen open() {
        return new ResearchBrowserScreen();
    }

    /**
     * 从条目详情返回研究界面。
     */
    public static ResearchBrowserScreen reopen() {
        return new ResearchBrowserScreen();
    }

    @Override
    protected void init() {
        super.init();
        this.openedAt = Util.getMillis();
        this.categories.clear();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            this.mode = ResearchScreenState.Mode.TREE;
            return;
        }
        this.categories.addAll(ResearchManager.categoriesSorted(player));
        ResourceLocation wanted = ResearchScreenState.category();
        this.activeCategory = this.categories.stream()
                .filter(holder -> wanted != null && holder.key().location().equals(wanted))
                .findFirst()
                .orElse(null);
        if (this.mode == ResearchScreenState.Mode.DEPARTMENT && this.activeCategory == null) {
            this.mode = ResearchScreenState.Mode.TREE;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0xA0050505);
        this.hotspots.clear();
        this.pendingTooltip.clear();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float open = this.openProgress();
        float scale = 0.3F + 0.7F * ChamferGeometry.easeOutCubic(open);
        float alpha = Mth.clamp(open / 0.4F, 0.0F, 1.0F);
        int treeMouseX = this.toTreeX(mouseX, scale);
        int treeMouseY = this.toTreeY(mouseY, scale);

        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-this.width / 2.0F, -this.height / 2.0F, 0.0F);
        if (this.mode == ResearchScreenState.Mode.TREE) {
            this.renderTree(graphics, player, treeMouseX, treeMouseY, alpha);
        } else {
            this.renderDepartment(graphics, player, treeMouseX, treeMouseY, alpha);
        }
        graphics.pose().popPose();

        this.renderHud(graphics, player, alpha);
        if (!this.pendingTooltip.isEmpty() && this.mode == ResearchScreenState.Mode.TREE) {
            graphics.renderComponentTooltip(this.font, this.pendingTooltip, mouseX, mouseY);
        }
    }

    private void renderTree(GuiGraphics graphics, Player player, int mouseX, int mouseY, float alpha) {
        int areaX = MARGIN;
        int areaY = TOP_BAR + 12;
        int areaWidth = Math.max(1, this.width - MARGIN * 2);
        int areaHeight = Math.max(1, this.height - areaY - 34);

        List<NodeBox> boxes = new ArrayList<>(this.categories.size());
        for (Holder.Reference<IResearchCategory> category : this.categories) {
            IResearchCategory value = category.value();
            int centerX = areaX + (int) Math.round(value.treeX() * areaWidth);
            int centerY = areaY + (int) Math.round(value.treeY() * areaHeight);
            centerX = Mth.clamp(centerX, MARGIN + NODE_WIDTH / 2, this.width - MARGIN - NODE_WIDTH / 2);
            centerY = Mth.clamp(centerY, areaY + NODE_HEIGHT / 2, this.height - 30 - NODE_HEIGHT / 2);
            boxes.add(new NodeBox(category, centerX - NODE_WIDTH / 2, centerY - NODE_HEIGHT / 2));
        }

        for (NodeBox box : boxes) {
            boolean open = ResearchManager.categoryOpen(player, box.category());
            for (ResourceLocation link : box.category().value().treeConnections()) {
                NodeBox target = this.findBox(boxes, link);
                if (target == null) {
                    continue;
                }
                boolean connectedOpen = open || ResearchManager.categoryOpen(player, target.category());
                ConnectorRenderer.drawLink(graphics,
                        box.centerX(), box.centerY(), target.centerX(), target.centerY(), connectedOpen);
            }
        }

        for (NodeBox box : boxes) {
            boolean open = ResearchManager.categoryOpen(player, box.category());
            int color = ChamferGeometry.withAlpha(box.category().value().color(), alpha);
            Component title = Component.translatable(box.category().value().nameKey());
            int[] progress = ResearchManager.progress(player, box.category());
            Component subtitle;
            int subtitleColor;
            if (!open) {
                subtitle = Component.translatable("research.lobotomycorp.node.locked");
                subtitleColor = LOCKED_COLOR;
            } else {
                int percent = progress[1] == 0 ? 0 : progress[0] * 100 / progress[1];
                subtitle = Component.translatable("research.lobotomycorp.node.progress", percent);
                subtitleColor = STATUS_COLOR;
            }

            boolean hovered = box.contains(mouseX, mouseY);
            float hover = this.animate(box.category().key().location(), hovered);
            this.drawNode(graphics, box.x(), box.y(), NODE_WIDTH, NODE_HEIGHT, color, title, subtitle,
                    subtitleColor, ResearchIcon.ofTexture(box.category().value().icon()), hover);

            if (hovered && !open) {
                this.pendingTooltip.addAll(ResearchManager.categoryLockedReasons(player, box.category()));
            }
            this.hotspots.add(new Hotspot(box.x(), box.y(), NODE_WIDTH, NODE_HEIGHT,
                    () -> this.openDepartment(box.category())));
        }
    }

    private void renderDepartment(GuiGraphics graphics, Player player, int mouseX, int mouseY, float alpha) {
        Holder.Reference<IResearchCategory> category = this.activeCategory;
        if (category == null) {
            this.mode = ResearchScreenState.Mode.TREE;
            return;
        }
        IResearchCategory value = category.value();
        int color = ChamferGeometry.withAlpha(value.color(), alpha);
        Component title = Component.translatable(value.nameKey());
        graphics.drawCenteredString(this.font, title, this.width / 2, TOP_BAR - 8, 0xFFFFFFFF);

        this.hoveringBack = mouseX >= MARGIN && mouseX <= MARGIN + 64 && mouseY >= TOP_BAR - 18 && mouseY <= TOP_BAR - 2;
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.screen.back"),
                MARGIN, TOP_BAR - 14, this.hoveringBack ? 0xFFFFFFFF : 0xFF9E9E9E, false);
        this.hotspots.add(new Hotspot(MARGIN, TOP_BAR - 18, 64, 16, this::backToTree));

        List<Holder.Reference<IResearchEntry>> entries = ResearchManager.visibleEntries(player, category);
        entries.sort(Comparator
                .comparingInt((Holder.Reference<IResearchEntry> holder) -> holder.value().row())
                .thenComparingInt(holder -> holder.value().column()));

        int usableWidth = Math.max(GRID_WIDTH, this.width - MARGIN * 2);
        int columns = Math.max(1, (usableWidth + GRID_GAP_X) / (GRID_WIDTH + GRID_GAP_X));
        int gridWidth = columns * GRID_WIDTH + (columns - 1) * GRID_GAP_X;
        int originX = (this.width - gridWidth) / 2;
        int originY = TOP_BAR + 14;

        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        for (int index = 0; index < entries.size(); index++) {
            Holder.Reference<IResearchEntry> entry = entries.get(index);
            int column = index % columns;
            int row = index / columns;
            int x = originX + column * (GRID_WIDTH + GRID_GAP_X);
            int y = originY + row * (GRID_HEIGHT + GRID_GAP_Y);
            ResourceLocation id = entry.key().location();

            Component entryTitle = Component.translatable(entry.value().nameKey());
            Component subtitle;
            int subtitleColor;
            if (knowledge.hasCompleted(id)) {
                subtitle = Component.translatable("research.lobotomycorp.entry.done");
                subtitleColor = DONE_COLOR;
            } else if (knowledge.knows(id)) {
                subtitle = Component.translatable("research.lobotomycorp.entry.stage",
                        knowledge.stageOf(id) + 1, entry.value().stages().size());
                subtitleColor = STATUS_COLOR;
            } else {
                subtitle = Component.translatable("research.lobotomycorp.entry.unknown");
                subtitleColor = IDLE_COLOR;
            }

            ResearchIcon icon = entry.value().icons().isEmpty()
                    ? ResearchIcon.ofTexture(net.lunovastudio.lobotomycorp.LCIds.DEFAULT_RESEARCH_ICON)
                    : entry.value().icons().getFirst();

            boolean hovered = mouseX >= x && mouseX < x + GRID_WIDTH
                    && mouseY >= y && mouseY < y + GRID_HEIGHT;
            float hover = this.animate(id, hovered);
            this.drawNode(graphics, x, y, GRID_WIDTH, GRID_HEIGHT, color, entryTitle, subtitle,
                    subtitleColor, icon, hover);

            if (hovered) {
                this.hotspots.add(new Hotspot(x, y, GRID_WIDTH, GRID_HEIGHT, () -> this.openEntry(id)));
            }
        }

        if (entries.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("research.lobotomycorp.screen.empty"),
                    this.width / 2, originY + 12, IDLE_COLOR);
        }
        if (columns == 1 && entries.size() > 1) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("research.lobotomycorp.screen.narrow"),
                    this.width / 2, this.height - 22, IDLE_COLOR);
        }
    }

    private void drawNode(GuiGraphics graphics, int x, int y, int width, int height, int color,
                          Component title, Component subtitle, int subtitleColor,
                          @Nullable ResearchIcon icon, float hover) {
        float scale = 1.0F + HOVER_SCALE * hover;
        graphics.pose().pushPose();
        graphics.pose().translate(x + width / 2.0F, y + height / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-(x + width / 2.0F), -(y + height / 2.0F), 0.0F);
        EntryNodeRenderer.draw(graphics, this.font, x, y, width, height, color, title, subtitle,
                subtitleColor, icon);
        graphics.pose().popPose();
    }

    private void renderHud(GuiGraphics graphics, Player player, float alpha) {
        int grade = PlayerCareerAccess.of(player).employeeGrade();
        graphics.drawString(this.font, Component.translatable("research.lobotomycorp.grade", grade),
                10, 8, ChamferGeometry.withAlpha(0xFFE8E8E8, alpha), false);
        if (this.mode == ResearchScreenState.Mode.TREE) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("research.lobotomycorp.screen.title"),
                    this.width / 2, 8, ChamferGeometry.withAlpha(0xFFFFFFFF, alpha));
        }
        graphics.drawCenteredString(this.font,
                Component.translatable("research.lobotomycorp.screen.hint"),
                this.width / 2, this.height - 16, ChamferGeometry.withAlpha(0xFF9E9E9E, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            float animScale = 0.3F + 0.7F * ChamferGeometry.easeOutCubic(this.openProgress());
            int treeX = this.toTreeX(mouseX, animScale);
            int treeY = this.toTreeY(mouseY, animScale);
            for (int index = this.hotspots.size() - 1; index >= 0; index--) {
                Hotspot hotspot = this.hotspots.get(index);
                if (hotspot.contains(treeX, treeY)) {
                    hotspot.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_H) {
            if (this.mode == ResearchScreenState.Mode.DEPARTMENT) {
                this.backToTree();
            } else {
                this.onClose();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        ResearchScreenState.setMode(this.mode);
        ResearchScreenState.setCategory(this.activeCategory == null
                ? null
                : this.activeCategory.key().location());
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openDepartment(Holder.Reference<IResearchCategory> category) {
        this.activeCategory = category;
        this.mode = ResearchScreenState.Mode.DEPARTMENT;
        ResearchScreenState.setMode(this.mode);
        ResearchScreenState.setCategory(category.key().location());
    }

    private void backToTree() {
        this.activeCategory = null;
        this.mode = ResearchScreenState.Mode.TREE;
        ResearchScreenState.setMode(this.mode);
        ResearchScreenState.setCategory(null);
    }

    private void openEntry(ResourceLocation id) {
        ResearchScreenState.setEntry(id);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ResearchEntryScreen(id));
    }

    private NodeBox findBox(List<NodeBox> boxes, ResourceLocation categoryId) {
        for (NodeBox box : boxes) {
            if (box.category().key().location().equals(categoryId)) {
                return box;
            }
        }
        return null;
    }

    private float animate(ResourceLocation id, boolean hovered) {
        float target = hovered ? 1.0F : 0.0F;
        return this.hoverAnimation.merge(id, target, (previous, next) -> Mth.lerp(HOVER_LERP, previous, next));
    }

    private float openProgress() {
        long elapsed = Util.getMillis() - this.openedAt;
        return Mth.clamp(elapsed / (float) OPEN_ANIMATION_MS, 0.0F, 1.0F);
    }

    private int toTreeX(double screenCoordinate, float scale) {
        return (int) Math.round((screenCoordinate - this.width / 2.0) / scale + this.width / 2.0);
    }

    private int toTreeY(double screenCoordinate, float scale) {
        return (int) Math.round((screenCoordinate - this.height / 2.0) / scale + this.height / 2.0);
    }

    private record NodeBox(Holder.Reference<IResearchCategory> category, int x, int y) {
        private int centerX() {
            return this.x + NODE_WIDTH / 2;
        }

        private int centerY() {
            return this.y + NODE_HEIGHT / 2;
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + NODE_WIDTH
                    && mouseY >= this.y && mouseY < this.y + NODE_HEIGHT;
        }
    }

    private record Hotspot(int x, int y, int width, int height, Runnable action) {
        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y && mouseY < this.y + this.height;
        }
    }
}
