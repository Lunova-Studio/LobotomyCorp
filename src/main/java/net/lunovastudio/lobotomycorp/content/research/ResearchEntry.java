package net.lunovastudio.lobotomycorp.content.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.ResearchEntryMeta;
import net.lunovastudio.lobotomycorp.api.research.ResearchIcon;
import net.lunovastudio.lobotomycorp.api.research.ResearchParent;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 研究条目的数据包实现。
 */
public record ResearchEntry(
        Holder<IResearchCategory> category,
        String nameKey,
        Set<ResearchParent> parents,
        int column,
        int row,
        List<IResearchStage> stages,
        Set<ResearchEntryMeta> meta,
        List<ResearchIcon> icons,
        List<ResourceLocation> unlocksRecipes
) implements IResearchEntry {
    public static final Codec<ResearchEntry> DIRECT_CODEC = RecordCodecBuilder.<ResearchEntry>create(instance ->
                    instance.group(
                                    RegistryFixedCodec.create(IResearchCategory.REGISTRY_KEY)
                                            .fieldOf("category")
                                            .forGetter(ResearchEntry::category),
                                    Codec.STRING.fieldOf("name").forGetter(ResearchEntry::nameKey),
                                    ResearchParent.CODEC
                                            .listOf()
                                            .optionalFieldOf("parents", List.of())
                                            .forGetter(e -> List.copyOf(e.parents())),
                                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("column").forGetter(ResearchEntry::column),
                                    Codec.INT.fieldOf("row").forGetter(ResearchEntry::row),
                                    ResearchStage.CODEC
                                            .listOf()
                                            .fieldOf("stages")
                                            .forGetter(e -> e.stages().stream()
                                                    .map(s -> (ResearchStage) s)
                                                    .toList()),
                                    ResearchEntryMeta.CODEC
                                            .listOf()
                                            .optionalFieldOf("meta", List.of())
                                            .forGetter(e -> List.copyOf(e.meta())),
                                    ResearchIcon.CODEC
                                            .listOf()
                                            .optionalFieldOf("icons", List.of())
                                            .forGetter(ResearchEntry::icons),
                                    ResourceLocation.CODEC
                                            .listOf()
                                            .optionalFieldOf("unlocks_recipes", List.of())
                                            .forGetter(ResearchEntry::unlocksRecipes))
                            .apply(instance, ResearchEntry::create))
            .validate(ResearchEntry::validate);

    public static final Codec<IResearchEntry> CODEC =
            DIRECT_CODEC.xmap(e -> (IResearchEntry) e, ResearchEntry::ofInterface);

    private static ResearchEntry create(
            Holder<IResearchCategory> category,
            String nameKey,
            List<ResearchParent> parents,
            int column,
            int row,
            List<ResearchStage> stages,
            List<ResearchEntryMeta> meta,
            List<ResearchIcon> icons,
            List<ResourceLocation> unlocksRecipes) {
        return new ResearchEntry(
                category,
                nameKey,
                new LinkedHashSet<>(parents),
                column,
                row,
                List.copyOf(stages),
                meta.isEmpty() ? EnumSet.noneOf(ResearchEntryMeta.class) : EnumSet.copyOf(meta),
                List.copyOf(icons),
                List.copyOf(unlocksRecipes));
    }

    private static DataResult<ResearchEntry> validate(ResearchEntry entry) {
        if (entry.stages.isEmpty()) {
            return DataResult.error(() -> "Research entry '" + entry.nameKey + "' must declare at least one stage");
        }
        return DataResult.success(entry);
    }

    private static ResearchEntry ofInterface(IResearchEntry entry) {
        if (entry instanceof ResearchEntry concrete) {
            return concrete;
        }
        return new ResearchEntry(
                entry.category(),
                entry.nameKey(),
                entry.parents(),
                entry.column(),
                entry.row(),
                entry.stages(),
                entry.meta(),
                entry.icons(),
                entry.unlocksRecipes());
    }
}
