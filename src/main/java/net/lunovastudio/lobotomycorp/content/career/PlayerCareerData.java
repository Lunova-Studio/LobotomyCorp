package net.lunovastudio.lobotomycorp.content.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareer;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 玩家档案：员工等级与部门权限等级。
 *
 * <p>与研究进度分开放置，避免研究记录被部门权限这类“人事数据”污染。</p>
 */
public record PlayerCareerData(int employeeGrade, Map<ResourceLocation, Integer> departmentAccess)
        implements PlayerCareer {

    public static final int DEFAULT_GRADE = 1;

    public static final Codec<PlayerCareerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("employee_grade", DEFAULT_GRADE).forGetter(PlayerCareerData::employeeGrade),
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                            .optionalFieldOf("department_access", Map.of())
                            .forGetter(PlayerCareerData::departmentAccess))
            .apply(instance, PlayerCareerData::new));

    public static final StreamCodec<ByteBuf, PlayerCareerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PlayerCareerData::employeeGrade,
            ByteBufCodecs.map(
                    LinkedHashMap::new,
                    ResourceLocation.STREAM_CODEC,
                    ByteBufCodecs.VAR_INT),
            PlayerCareerData::departmentAccess,
            PlayerCareerData::new);

    public PlayerCareerData {
        employeeGrade = Math.max(DEFAULT_GRADE, Math.min(PlayerCareer.MAX_GRADE, employeeGrade));
        departmentAccess = departmentAccess == null ? Map.of() : Map.copyOf(departmentAccess);
    }

    public static PlayerCareerData empty() {
        return new PlayerCareerData(DEFAULT_GRADE, Map.of());
    }

    @Override
    public int accessLevel(ResourceLocation department) {
        if (department == null) {
            return 0;
        }
        return departmentAccess.getOrDefault(department, 0);
    }

    @Override
    public boolean setEmployeeGrade(int grade, ServerPlayer owner) {
        int clamped = Math.max(DEFAULT_GRADE, Math.min(PlayerCareer.MAX_GRADE, grade));
        if (clamped == employeeGrade) {
            return false;
        }
        PlayerCareerData next = new PlayerCareerData(clamped, departmentAccess);
        owner.setData(LCAttachments.career(), next);
        owner.syncData(LCAttachments.career());
        return true;
    }

    @Override
    public boolean setAccessLevel(ResourceLocation department, int level, ServerPlayer owner) {
        if (department == null) {
            return false;
        }
        int clamped = Math.max(0, Math.min(PlayerCareer.MAX_GRADE, level));
        if (clamped == accessLevel(department)) {
            return false;
        }
        Map<ResourceLocation, Integer> next = new LinkedHashMap<>(departmentAccess);
        if (clamped == 0) {
            next.remove(department);
        } else {
            next.put(department, clamped);
        }
        PlayerCareerData data = new PlayerCareerData(employeeGrade, next);
        owner.setData(LCAttachments.career(), data);
        owner.syncData(LCAttachments.career());
        return true;
    }
}
