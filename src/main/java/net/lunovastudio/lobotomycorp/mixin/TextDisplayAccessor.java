package net.lunovastudio.lobotomycorp.mixin;

import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 为 {@link Display.TextDisplay} 暴露 private 的 {@code setTextOpacity}。
 * <p>
 * NeoForge 未提供任何公开 API 可在实体生成后修改 TextDisplay 透明度，
 * 因此使用纯访问器（@Invoker）桥接该方法，仅增加可见性，不注入任何逻辑。
 */
@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Invoker("setTextOpacity")
    void callSetTextOpacity(byte textOpacity);
}
