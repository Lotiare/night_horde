package com.lotiare.nighthorde;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.literal;

/** Admin commands (requires OP level 2). */
public final class NightHordeCommands {

    private NightHordeCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, NightHordeMod mod) {
        dispatcher.register(
                literal("nighthorde")
                        // In 1.21.x, ServerCommandSource permission helpers differ by mapping.
                        // This check works for both player and console sources.
                        .requires(src -> (src.getEntity() == null)
                                || (src.getEntity() instanceof net.minecraft.server.network.ServerPlayerEntity p
                                && p.hasPermissionLevel(2)))
                        .then(literal("reload").executes(ctx -> {
                            var server = ctx.getSource().getServer();
                            Path configDir = server.getRunDirectory().resolve("config");
                            mod.reloadConfig(configDir);
                            ctx.getSource().sendFeedback(() -> Text.literal("NightHorde: Config reloaded."), false);
                            return 1;
                        }))
                        .then(literal("test").executes(ctx -> {
                            boolean ok = mod.tryForceTestRaid();
                            if (ok) {
                                ctx.getSource().sendFeedback(() -> Text.literal("NightHorde: Test raid triggered (requirements still apply)."), false);
                                return 1;
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("NightHorde: Test raid failed (no qualified players)."), false);
                                return 0;
                            }
                        }))
        );
    }
}
