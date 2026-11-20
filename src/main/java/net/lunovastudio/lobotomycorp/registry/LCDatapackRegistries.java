package net.lunovastudio.lobotomycorp.registry;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.content.research.ResearchCategory;
import net.lunovastudio.lobotomycorp.content.research.ResearchEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * 本模组的数据包注册表。
 *
 * <p>分类与条目都由数据包驱动，加载后随原版注册表自动同步到客户端，
 * 因此客户端可以直接用 {@code registryAccess().lookup(...)} 读取定义，无需额外的定义同步包。</p>
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class LCDatapackRegistries {
    private LCDatapackRegistries() {
    }

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(IResearchCategory.REGISTRY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
        event.dataPackRegistry(IResearchEntry.REGISTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
    }
}
