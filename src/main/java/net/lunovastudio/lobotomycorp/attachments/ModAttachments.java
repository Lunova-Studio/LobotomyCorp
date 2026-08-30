package net.lunovastudio.lobotomycorp.attachments;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LobotomyCorp.MODID);

    public static final Supplier<AttachmentType<PsychologicalData>> PSYCHOLOGICAL =
            ATTACHMENT_TYPES.register("psychological",
                    () -> AttachmentType.builder(() -> new PsychologicalData(PsychologicalData.MAX_PSYCHOLOGICAL))
                            .sync(PsychologicalData.STREAM_CODEC)
                            .serialize(PsychologicalData.CODEC)
                            .build()
            );

    /**
     * 自定义护甲槽位中的护甲。服务端 {@code setData} 后自动同步到客户端。
     */
    public static final Supplier<AttachmentType<ItemStack>> EGO_ARMOR =
            ATTACHMENT_TYPES.register("ego_armor",
                    () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                            .sync(ItemStack.OPTIONAL_STREAM_CODEC)
                            .serialize(ItemStack.OPTIONAL_CODEC)
                            .copyOnDeath()
                            .build()
            );

    //By JustRainy:玩家登录时初始化数据
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        // 即使 getData() 会自动创建，这里显式确保数据存在
        var data = player.getData(ModAttachments.PSYCHOLOGICAL.get());
        LobotomyCorp.LOGGER.debug("{}",data );
    }

    //By JustRainy:玩家重生时初始化数据
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        var data = player.getData(ModAttachments.PSYCHOLOGICAL.get());
        LobotomyCorp.LOGGER.debug("{}",data );
    }

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
