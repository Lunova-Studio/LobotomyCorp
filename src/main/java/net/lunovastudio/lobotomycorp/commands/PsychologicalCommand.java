package net.lunovastudio.lobotomycorp.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PsychologicalCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("psychological")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PsychologicalData psyState = player.getData(ModAttachments.PSYCHOLOGICAL.get());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("当前精神值: " + psyState.psychological() + "/" + PsychologicalData.MAX_PSYCHOLOGICAL),
                                            false
                                    );
                                    return psyState.psychological();
                                })
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            player.setData(ModAttachments.PSYCHOLOGICAL.get(), new PsychologicalData(value));
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("精神值已设置为: " + value),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(-100, 100))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            PsychologicalData currentState = player.getData(ModAttachments.PSYCHOLOGICAL.get());
                                            PsychologicalData newState = currentState.addPsychologicalValue(amount);
                                            player.setData(ModAttachments.PSYCHOLOGICAL.get(), newState);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("精神值变化: " + amount + "，当前: " + newState.psychological()),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }
}