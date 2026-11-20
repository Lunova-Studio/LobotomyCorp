package net.lunovastudio.lobotomycorp.api.research;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResearchParent} 与 {@link ResearchIcon} 的字符串编解码测试。
 */
class ResearchParentIconTest {
    @Test
    void parsesPlainParent() {
        ResearchParent parent = decode("lobotomycorp:material_analysis");

        assertEquals(ResourceLocation.fromNamespaceAndPath("lobotomycorp", "material_analysis"), parent.id());
        assertEquals(0, parent.stage());
        assertFalse(parent.inherit());
    }

    @Test
    void parsesStageAndInheritMarkers() {
        ResearchParent parent = decode("~lobotomycorp:material_analysis@2");

        assertEquals(ResourceLocation.fromNamespaceAndPath("lobotomycorp", "material_analysis"), parent.id());
        assertEquals(2, parent.stage());
        assertTrue(parent.inherit());
    }

    @Test
    void serializesBackToTheSameString() {
        assertEquals("lobotomycorp:material_analysis", encode(ResearchParent.of(id("material_analysis"))));
        assertEquals("~lobotomycorp:material_analysis@3",
                encode(new ResearchParent(id("material_analysis"), 3, true)));
    }

    @Test
    void rejectsInvalidIdentifiers() {
        assertTrue(ResearchParent.CODEC.parse(NbtOps.INSTANCE, StringTag.valueOf("not a valid id"))
                .error()
                .isPresent());
    }

    @Test
    void iconsDistinguishTextureFromItem() {
        ResearchIcon texture = decodeIcon("lobotomycorp:textures/gui/projects/task.png");
        ResearchIcon item = decodeIcon("lobotomycorp:lc_item_penitence");

        assertTrue(texture.texture());
        assertEquals(ResearchIcon.Kind.TEXTURE, texture.kind());
        assertEquals(ResearchIcon.Kind.ITEM, item.kind());
        assertEquals("lobotomycorp:textures/gui/projects/task.png", encodeIcon(texture));
    }

    private static ResearchParent decode(String value) {
        return ResearchParent.CODEC.parse(NbtOps.INSTANCE, StringTag.valueOf(value))
                .getOrThrow(error -> new IllegalStateException(error));
    }

    private static String encode(ResearchParent parent) {
        return ResearchParent.CODEC.encodeStart(NbtOps.INSTANCE, parent)
                .getOrThrow(error -> new IllegalStateException(error))
                .getAsString();
    }

    private static ResearchIcon decodeIcon(String value) {
        return ResearchIcon.CODEC.parse(NbtOps.INSTANCE, StringTag.valueOf(value))
                .getOrThrow(error -> new IllegalStateException(error));
    }

    private static String encodeIcon(ResearchIcon icon) {
        return ResearchIcon.CODEC.encodeStart(NbtOps.INSTANCE, icon)
                .getOrThrow(error -> new IllegalStateException(error))
                .getAsString();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("lobotomycorp", path);
    }
}
