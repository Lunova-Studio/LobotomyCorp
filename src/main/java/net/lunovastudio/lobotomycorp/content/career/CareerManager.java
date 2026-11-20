package net.lunovastudio.lobotomycorp.content.career;

import net.lunovastudio.lobotomycorp.api.capability.PlayerCareer;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家档案的服务端入口与只读查询。
 */
public final class CareerManager {
    private CareerManager() {
    }

    public static PlayerCareerData data(Player player) {
        return player.getData(LCAttachments.career());
    }

    public static int employeeGrade(Player player) {
        return PlayerCareerAccess.of(player).employeeGrade();
    }

    public static int accessLevel(Player player, ResourceLocation department) {
        return PlayerCareerAccess.of(player).accessLevel(department);
    }

    public static boolean setEmployeeGrade(ServerPlayer player, int grade) {
        return data(player).setEmployeeGrade(grade, player);
    }

    public static boolean setAccessLevel(ServerPlayer player, ResourceLocation department, int level) {
        return data(player).setAccessLevel(department, level, player);
    }

    /**
     * 按分类的“默认权限”给玩家补齐许可：等级达标时授予分类声明的默认等级。
     *
     * @param categoryId 分类（部门）id
     * @param requiredGrade 进入该分类所需的员工等级
     * @param defaultLevel 达成条件后给予的权限等级
     * @return 权限确实变化时为 true
     */
    public static boolean grantDefaultAccess(ServerPlayer player, ResourceLocation categoryId,
                                             int requiredGrade, int defaultLevel) {
        if (PlayerCareerAccess.of(player).employeeGrade() < requiredGrade) {
            return false;
        }
        if (PlayerCareerAccess.of(player).accessLevel(categoryId) >= defaultLevel) {
            return false;
        }
        return setAccessLevel(player, categoryId, defaultLevel);
    }
}
