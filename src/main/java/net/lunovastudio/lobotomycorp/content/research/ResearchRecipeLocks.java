package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配方门控索引。
 *
 * <p>条目的 {@code unlocks_recipes} 声明“完成该条目后解锁哪些配方”。服务端在数据包加载后
 * 建立“配方 → 条目”与“产物物品 → 条目”两张反向索引，取结果槽时据此判定。</p>
 *
 * <p>只有被条目显式声明的配方受管理，原版与无关模组配方不受影响。</p>
 */
public final class ResearchRecipeLocks {
    private static volatile Map<ResourceLocation, ResourceLocation> ownerByRecipe = Map.of();
    private static volatile Map<Item, Set<ResourceLocation>> ownersByItem = Map.of();
    private static volatile Set<ResourceLocation> unknownRecipes = Set.of();

    private ResearchRecipeLocks() {
    }

    /**
     * 重建索引。数据包加载完成或服务器启动后调用。
     */
    public static void rebuild(@Nullable MinecraftServer server) {
        if (server == null) {
            return;
        }
        Map<ResourceLocation, ResourceLocation> recipeOwners = new HashMap<>();
        Map<Item, Set<ResourceLocation>> itemOwners = new HashMap<>();
        Set<ResourceLocation> unknown = new LinkedHashSet<>();

        var entries = server.registryAccess().lookup(IResearchEntry.REGISTRY_KEY);
        if (entries.isEmpty()) {
            ownerByRecipe = Map.of();
            ownersByItem = Map.of();
            unknownRecipes = Set.of();
            return;
        }
        for (Holder.Reference<IResearchEntry> entry : entries.get().listElements().toList()) {
            ResourceLocation entryId = entry.key().location();
            for (ResourceLocation recipeId : entry.value().unlocksRecipes()) {
                RecipeHolder<?> recipe = server.getRecipeManager().byKey(recipeId).orElse(null);
                if (recipe == null) {
                    unknown.add(recipeId);
                    continue;
                }
                recipeOwners.put(recipeId, entryId);
                ItemStack result = recipe.value().getResultItem(server.registryAccess());
                if (!result.isEmpty()) {
                    itemOwners.computeIfAbsent(result.getItem(), item -> new HashSet<>()).add(entryId);
                }
            }
        }
        ownerByRecipe = Map.copyOf(recipeOwners);
        Map<Item, Set<ResourceLocation>> frozen = new HashMap<>();
        itemOwners.forEach((item, owners) -> frozen.put(item, Set.copyOf(owners)));
        ownersByItem = Map.copyOf(frozen);
        unknownRecipes = Set.copyOf(unknown);
        if (!unknown.isEmpty()) {
            LobotomyCorp.LOGGER.warn("Research unlocks reference {} unknown recipe(s): {}", unknown.size(), unknown);
        }
    }

    /**
     * 判断玩家能否取出某合成结果。
     *
     * @param player 玩家
     * @param result 结果槽物品
     * @return 允许取出时为 true
     */
    public static boolean canCraft(ServerPlayer player, ItemStack result) {
        if (player.isCreative() || result.isEmpty()) {
            return true;
        }
        Set<ResourceLocation> owners = ownersByItem.get(result.getItem());
        if (owners == null || owners.isEmpty()) {
            return true;
        }
        var knowledge = PlayerResearchAccess.of(player);
        for (ResourceLocation owner : owners) {
            if (knowledge.hasCompleted(owner)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个配方是否受研究门控。
     */
    public static boolean isManaged(ResourceLocation recipeId) {
        return ownerByRecipe.containsKey(recipeId);
    }

    /**
     * 某个配方由哪个条目解锁。
     */
    public static @Nullable ResourceLocation ownerOf(ResourceLocation recipeId) {
        return ownerByRecipe.get(recipeId);
    }

    public static int managedRecipeCount() {
        return ownerByRecipe.size();
    }

    public static Set<ResourceLocation> unknownRecipes() {
        return unknownRecipes;
    }

    public static void clear() {
        ownerByRecipe = Map.of();
        ownersByItem = Map.of();
        unknownRecipes = Set.of();
    }
}
