package net.lunovastudio.lobotomycorp.attachments;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

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

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
