package net.lunovastudio.lobotomycorp.api.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 玩家在公司内的职级与部门权限。
 *
 * <p>与研究进度分开存放：研究进度只记录条目/阶段/知识点，职级与部门权限属于玩家档案，
 * 由 {@code content.career.CareerManager} 维护。通过 {@link PlayerCareerAccess#of} 获取。</p>
 */
public interface PlayerCareer {
    /** 员工等级，最低 1。 */
    int employeeGrade();

    /** 某部门的权限等级，0 表示没有权限。 */
    int accessLevel(ResourceLocation department);

    /** 玩家是否拥有某部门的至少一级权限。 */
    default boolean hasAccess(ResourceLocation department) {
        return accessLevel(department) > 0;
    }

    /**
     * 设置员工等级。
     *
     * @param grade 新等级，会被夹取到 1..{@value #MAX_GRADE}
     * @return 等级确实变化时为 {@code true}
     */
    boolean setEmployeeGrade(int grade, ServerPlayer owner);

    /**
     * 设置某部门的权限等级。
     *
     * @param level 新等级，0 表示无权限
     * @return 权限确实变化时为 {@code true}
     */
    boolean setAccessLevel(ResourceLocation department, int level, ServerPlayer owner);

    /** 允许的最大员工等级。 */
    int MAX_GRADE = 10;
}
