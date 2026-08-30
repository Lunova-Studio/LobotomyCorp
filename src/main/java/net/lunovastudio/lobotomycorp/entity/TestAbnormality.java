package net.lunovastudio.lobotomycorp.entity;

import net.lunovastudio.lobotomycorp.enums.ego.EgoTiers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 测试异想体：模型 / AI 复用原版猪。
 */
public class TestAbnormality extends Abnormality {
    public TestAbnormality(EntityType<? extends TestAbnormality> type, Level level) {
        super(type, level, EgoTiers.ZAYIN);
    }
}
