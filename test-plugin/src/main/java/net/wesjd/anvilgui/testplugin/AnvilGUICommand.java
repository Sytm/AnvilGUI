package net.wesjd.anvilgui.testplugin;

import static java.util.Collections.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AnvilGUICommand implements CommandExecutor {

    private final AtomicInteger asyncCounter = new AtomicInteger();
    private final TestPlugin plugin;

    private final Map<String, Map.Entry<String, BiConsumer<AnvilGUI.Builder, String>>> builderModifier =
            new HashMap<>();

    public AnvilGUICommand(TestPlugin plugin) {
        this.plugin = plugin;

        builderModifier.put(
                "preventclose",
                new AbstractMap.SimpleEntry<>("preventclose", (builder, arg) -> builder.preventClose()));
        builderModifier.put("title", new AbstractMap.SimpleEntry<>("title=Some-Text", AnvilGUI.Builder::title));
        builderModifier.put(
                "rainbowTitle",
                new AbstractMap.SimpleEntry<>(
                        "rainbowTitle",
                        (builder, arg) -> builder.jsonTitle(
                                "{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"R\"},{\"color\":\"#FFDA00\",\"text\":\"a\"},{\"color\":\"#48FF00\",\"text\":\"i\"},{\"color\":\"#00FF91\",\"text\":\"n\"},{\"color\":\"#0091FF\",\"text\":\"b\"},{\"color\":\"#4800FF\",\"text\":\"o\"},{\"color\":\"#FF00DA\",\"text\":\"w\"}],\"text\":\"\"}")));
        builderModifier.put(
                "text", new AbstractMap.SimpleEntry<>("text=Some-Text-for-the-item", AnvilGUI.Builder::text));
        builderModifier.put(
                "itemleft",
                new AbstractMap.SimpleEntry<>(
                        "itemright=GRASS_BLOCK",
                        (builder, arg) -> builder.itemLeft(new ItemStack(Material.matchMaterial(arg)))));
        builderModifier.put(
                "itemright",
                new AbstractMap.SimpleEntry<>(
                        "itemleft=GRASS_BLOCK",
                        (builder, arg) -> builder.itemRight(new ItemStack(Material.matchMaterial(arg)))));
        builderModifier.put(
                "itemoutput",
                new AbstractMap.SimpleEntry<>(
                        "itemoutput=GRASS_BLOCK",
                        (builder, arg) -> builder.itemOutput(new ItemStack(Material.matchMaterial(arg)))));
        builderModifier.put(
                "concurrent",
                new AbstractMap.SimpleEntry<>(
                        "concurrent", (builder, arg) -> builder.allowConcurrentClickHandlerExecution()));
        builderModifier.put(
                "asyncClick",
                new AbstractMap.SimpleEntry<>(
                        "asyncClick",
                        (builder, arg) -> builder.onClickAsync((slot, state) -> CompletableFuture.supplyAsync(() -> {
                            int id = asyncCounter.getAndIncrement();

                            state.getPlayer()
                                    .sendMessage("[Async] Slot: " + slot + " Text: " + state.getText() + " ID: #" + id);

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            state.getPlayer().sendMessage("[Async] Done #" + id);

                            return emptyList();
                        }))));
        builderModifier.put(
                "onclick",
                new AbstractMap.SimpleEntry<>(
                        "onclick=close closes the anvilgui\n"
                                + "onclick=text replaces the current text with 'text'\n"
                                + "onclick=inventory opens the own inventory",
                        (builder, arg) -> builder.onClick((slot, state) -> {
                            state.getPlayer().sendMessage("Slot: " + slot + " Text: " + state.getText());
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return emptyList();
                            }

                            switch (arg.toLowerCase(Locale.ROOT)) {
                                default:
                                    return emptyList();
                                case "close":
                                    return singletonList(AnvilGUI.ResponseAction.close());
                                case "text":
                                    return singletonList(AnvilGUI.ResponseAction.replaceInputText("text"));
                                case "inventory":
                                    return singletonList(AnvilGUI.ResponseAction.openInventory(
                                            state.getPlayer().getInventory()));
                            }
                        })));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (args.length == 0) {
            builderModifier.values().stream().map(Map.Entry::getKey).forEach(sender::sendMessage);
            return true;
        }

        Player player = (Player) sender;

        AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(plugin);

        builder.onClick((slot, state) -> {
            state.getPlayer().sendMessage("Slot: " + slot + " Text: " + state.getText());
            return emptyList();
        });

        for (String arg : args) {
            int splitIndex = arg.indexOf('=');
            String key = arg, value = "";
            if (splitIndex >= 0) {
                key = arg.substring(0, splitIndex);
                value = arg.substring(splitIndex + 1);
            }

            if (builderModifier.containsKey(key)) {
                builderModifier.get(key).getValue().accept(builder, value);
            } else {
                player.sendMessage("BuilderModifier " + key + " not found");
            }
        }

        builder.onClose(state -> state.getPlayer().sendMessage("Closed"));

        builder.open(player);
        return true;
    }
}
