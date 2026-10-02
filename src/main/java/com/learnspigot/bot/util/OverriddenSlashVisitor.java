package com.learnspigot.bot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.LampVisitor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.SlashActorFactory;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.slash.JDAParser;

import java.util.HashMap;
import java.util.Map;

// TODO remove this when Lamp adds name-value pair support for suggestions
/**
 * This class is a 1:1 copy of {@link revxrsal.commands.jda.JDAVisitors#slashCommands(JDA, SlashActorFactory)},
 * except it registers a {@link BlockingJDAListener}, which prevents Lamp from trying to parse
 * {@code /reference} as it is parsed manually until Lamp expands auto-completion support.
 * @blame ehilynxin
 */
public final class OverriddenSlashVisitor {

    private OverriddenSlashVisitor(){
        throw new UnsupportedOperationException();
    }

    public static <A extends SlashCommandActor> @NotNull LampVisitor<A> slashCommands(@NotNull JDA jda, @NotNull SlashActorFactory<A> actorFactory) {
        return lamp -> {
            JDAParser<A> parser = new JDAParser<>();
            for (ExecutableCommand<A> child : lamp.registry().commands()) parser.parse(child);
            jda.retrieveCommands().submit().thenAccept(commands -> {
                Map<String, SlashCommandData> notRegistered = new HashMap<>(parser.commands());

                for (Command command : commands) {
                    if (command.getType() != Command.Type.SLASH) continue;
                    SlashCommandData data = notRegistered.remove(command.getName());
                    if (data == null) {
                        String rename = parser.renamedCommands().get(command.getName());
                        if (rename != null) {
                            SlashCommandData renamedData = parser.commands().get(rename);
                            if (renamedData != null) {
                                jda.editCommandById(command.getType(), command.getId()).apply(renamedData).queue();
                                notRegistered.remove(rename);
                            }
                        } else command.delete().queue();
                    } else jda.editCommandById(command.getType(), command.getId()).apply(data).queue();
                }
                notRegistered.values().forEach(newCommand -> jda.upsertCommand(newCommand).queue());
            });
            jda.addEventListener(new BlockingJDAListener<>(lamp, actorFactory));
        };
    }

}