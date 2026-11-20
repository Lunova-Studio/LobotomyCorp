package net.lunovastudio.lobotomycorp.content.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 研究分类（部门）的数据包实现。
 *
 * <p>分类的玩法字段全部来自 JSON，代码里只负责解析与默认值；部门色以 {@code "AARRGGBB"}
 * 字符串书写，便于数据包手工编辑。</p>
 */
public record ResearchCategory(
        String nameKey,
        String descriptionKey,
        Optional<ResourceLocation> requiredResearch,
        Map<ResourceLocation, Integer> formula,
        ResourceLocation icon,
        Optional<ResourceLocation> background,
        Optional<ResourceLocation> overlayBackground,
        int color,
        int index,
        int requiredEmployeeGrade,
        int defaultAccessLevel,
        double treeX,
        double treeY,
        List<ResourceLocation> treeConnections
) implements IResearchCategory {
    public static final ResourceLocation DEFAULT_ICON = LCIds.rl("textures/gui/projects/task.png");
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    public static final Codec<ResearchCategory> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(ResearchCategory::nameKey),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(ResearchCategory::descriptionKey),
                    ResourceLocation.CODEC
                            .optionalFieldOf("required_research")
                            .forGetter(ResearchCategory::requiredResearch),
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                            .optionalFieldOf("formula", Map.of())
                            .forGetter(ResearchCategory::formula),
                    ResourceLocation.CODEC
                            .optionalFieldOf("icon", DEFAULT_ICON)
                            .forGetter(ResearchCategory::icon),
                    ResourceLocation.CODEC
                            .optionalFieldOf("background")
                            .forGetter(ResearchCategory::background),
                    ResourceLocation.CODEC
                            .optionalFieldOf("overlay_background")
                            .forGetter(ResearchCategory::overlayBackground),
                    Codec.STRING
                            .optionalFieldOf("color", formatColor(DEFAULT_COLOR))
                            .xmap(ResearchCategory::parseColor, ResearchCategory::formatColor)
                            .forGetter(ResearchCategory::color),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(ResearchCategory::index),
                    ExtraCodecs.intRange(0, 10)
                            .optionalFieldOf("required_employee_grade", 1)
                            .forGetter(ResearchCategory::requiredEmployeeGrade),
                    ExtraCodecs.intRange(0, 10)
                            .optionalFieldOf("default_access_level", 0)
                            .forGetter(ResearchCategory::defaultAccessLevel),
                    Codec.DOUBLE.optionalFieldOf("tree_x", 0.5D).forGetter(ResearchCategory::treeX),
                    Codec.DOUBLE.optionalFieldOf("tree_y", 0.5D).forGetter(ResearchCategory::treeY),
                    ResourceLocation.CODEC
                            .listOf()
                            .optionalFieldOf("tree_connections", List.of())
                            .forGetter(ResearchCategory::treeConnections))
            .apply(instance, ResearchCategory::new));

    public static final Codec<IResearchCategory> CODEC =
            DIRECT_CODEC.xmap(c -> (IResearchCategory) c, ResearchCategory::ofInterface);

    public ResearchCategory {
        descriptionKey = descriptionKey == null ? "" : descriptionKey;
        formula = formula == null ? Map.of() : Map.copyOf(formula);
        icon = icon == null ? DEFAULT_ICON : icon;
        requiredResearch = requiredResearch == null ? Optional.empty() : requiredResearch;
        background = background == null ? Optional.empty() : background;
        overlayBackground = overlayBackground == null ? Optional.empty() : overlayBackground;
        treeConnections = treeConnections == null ? List.of() : List.copyOf(treeConnections);
    }

    /**
     * 把 {@code "AARRGGBB"} 或 {@code "RRGGBB"} 字符串解析成颜色；无法解析时返回不透明白色。
     */
    public static int parseColor(String value) {
        if (value == null) {
            return DEFAULT_COLOR;
        }
        String hex = value.trim().replace("#", "").replace("0x", "");
        if (hex.length() == 6) {
            hex = "FF" + hex;
        }
        if (hex.length() != 8) {
            return DEFAULT_COLOR;
        }
        try {
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException exception) {
            return DEFAULT_COLOR;
        }
    }

    public static String formatColor(int color) {
        return String.format("%08X", color);
    }

    private static ResearchCategory ofInterface(IResearchCategory category) {
        if (category instanceof ResearchCategory concrete) {
            return concrete;
        }
        return new ResearchCategory(
                category.nameKey(),
                category.descriptionKey(),
                category.requiredResearch(),
                category.formula(),
                category.icon(),
                category.background(),
                category.overlayBackground(),
                category.color(),
                category.index(),
                category.requiredEmployeeGrade(),
                category.defaultAccessLevel(),
                category.treeX(),
                category.treeY(),
                category.treeConnections());
    }
}
