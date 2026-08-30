package net.lunovastudio.lobotomycorp.combat.visual;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.mixin.TextDisplayAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * E.G.O 命中飘字：在受击者头顶生成一个数字，逐渐上浮并淡出。
 * <p>
 * 该实例仅存在于服务端，动画（上浮 + 透明度）通过同步数据驱动；
 * 客户端会把 {@link EntityType#TEXT_DISPLAY} 重建为普通 TextDisplay，仅负责渲染。
 * <p>
 * 外观通过 NBT {@code load()} 配置（避免依赖 private setter），
 * 唯一需要的 setter {@code setTextOpacity} 由 {@link TextDisplayAccessor} 桥接。
 */
public class EgoFloatingDamageText extends Display.TextDisplay {
    private static final int LIFETIME = 15;
    private static final float RISE_SPEED = 0.05F;

    private final double startY;
    private int age;

    public EgoFloatingDamageText(Level level, double x, double y, double z, Component text) {
        super(EntityType.TEXT_DISPLAY, level);
        this.startY = y;

        CompoundTag tag = new CompoundTag();
        tag.putString("text", Component.Serializer.toJson(text, level.registryAccess()));
        tag.putInt("text_opacity", 255);
        tag.putInt("background", 0);
        tag.putBoolean("shadow", true);
        tag.putBoolean("see_through", true);
        tag.putString("billboard", "center");
        tag.putInt("line_width", 300);
        tag.putInt("teleport_duration", 3);

        this.load(tag);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
        this.setPos(this.getX(), this.startY + this.age * RISE_SPEED, this.getZ());

        float progress = 1.0F - (float) this.age / LIFETIME;
        int opacity = (int) (255.0F * progress);
        ((TextDisplayAccessor) this).callSetTextOpacity((byte) opacity);

        if (this.age >= LIFETIME) {
            this.discard();
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    /**
     * 在目标头顶生成一条飘字。
     *
     * @param resistance 目标对 {@code damageType} 的抗性倍率
     */
    public static void spawn(ServerLevel level, LivingEntity target, EgoDamageType damageType, float damage, float resistance) {
        if (damage <= 0.0F) {
            return;
        }

        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.9 + 0.2;
        double z = target.getZ();

        level.addFreshEntity(new EgoFloatingDamageText(level, x, y, z, buildText(damageType, damage, resistance)));
    }

    private static Component buildText(EgoDamageType damageType, float damage, float resistance) {
        String label = resistanceLabel(resistance);
        String number = String.format(Locale.ROOT, "%.1f", damage);

        ChatFormatting color = switch (damageType) {
            case RED -> ChatFormatting.RED;
            case WHITE -> ChatFormatting.WHITE;
            case BLACK -> ChatFormatting.DARK_PURPLE;
            case PALE -> ChatFormatting.BLUE;
        };
        Style style = Style.EMPTY.withColor(TextColor.fromLegacyFormat(color));

        return Component.literal(label + " " + number).withStyle(style);
    }

    /**
     * 在目标头顶生成一条 IMMUNE 飘字（非 E.G.O 武器攻击异想体时）。
     */
    public static void spawnImmune(ServerLevel level, LivingEntity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.9 + 0.2;
        double z = target.getZ();

        level.addFreshEntity(new EgoFloatingDamageText(level, x, y, z, buildImmuneText()));
    }

    private static Component buildImmuneText() {
        return Component.literal("IMMUNE").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
    }

    private static String resistanceLabel(float resistance) {
        if (resistance <= 0.01F) {
            return "IMMUNE";
        }
        if (resistance < 1.0F) {
            return "WEAK";
        }
        if (resistance > 1.0F) {
            return "RESIST";
        }
        return "NORMAL";
    }
}
