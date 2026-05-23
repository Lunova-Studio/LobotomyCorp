package net.lunovastudio.lobotomycorp.attachments.datas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record PsychologicalData(int psychological) {
    public static final int MAX_PSYCHOLOGICAL = 20;

    public PsychologicalData addPsychologicalValue(int amount) {
        return new PsychologicalData(this.psychological + amount);
    }

    public PsychologicalData subtractPsychologicalValue(int amount) {
        return new PsychologicalData(this.psychological - amount);
    }

    public float getPsychologicalPercentage() {
        return (float) psychological / MAX_PSYCHOLOGICAL;
    }

    public static final Codec<PsychologicalData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("psychological").forGetter(PsychologicalData::psychological)
            ).apply(instance, PsychologicalData::new)

    );

    public static final StreamCodec<ByteBuf, PsychologicalData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PsychologicalData::psychological,
            PsychologicalData::new
    );
}
