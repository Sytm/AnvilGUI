package net.wesjd.anvilgui.testplugin;

import static java.util.Collections.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

  private final Map<String, BuilderModifier> builderModifier = new HashMap<>();

  public AnvilGUICommand(TestPlugin plugin) {
    this.plugin = plugin;

    builderModifier.put(
        "preventclose",
        new BuilderModifier("preventclose", (builder, arg) -> builder.preventClose()));
    builderModifier.put("title", new BuilderModifier("title=Some-Text", AnvilGUI.Builder::title));
    builderModifier.put(
        "rainbowtitle",
        new BuilderModifier(
            "rainbowtitle",
            (builder, arg) ->
                builder.title(MiniMessage.miniMessage().deserialize("<rainbow>Rainbow Title"))));
    builderModifier.put(
        "text", new BuilderModifier("text=Some-Text-for-the-item", AnvilGUI.Builder::text));
    builderModifier.put(
        "itemleft",
        new BuilderModifier(
            "itemright=GRASS_BLOCK",
            (builder, arg) -> builder.itemLeft(new ItemStack(Material.matchMaterial(arg)))));
    builderModifier.put("itemright", new BuilderModifier("itemleft=GRASS_BLOCK", (builder, arg) -> {
      ItemStack stack = new ItemStack(Material.matchMaterial(arg));
      stack.editMeta(meta -> meta.displayName(Component.text("Custom right item")));
      builder.itemRight(stack);
    }));
    builderModifier.put(
        "itemoutput", new BuilderModifier("itemoutput=GRASS_BLOCK", (builder, arg) -> {
          ItemStack stack = new ItemStack(Material.matchMaterial(arg));
          stack.editMeta(meta -> meta.displayName(Component.text("Custom output item")));
          builder.itemOutput(stack);
        }));
    builderModifier.put("items", new BuilderModifier("items", (builder, arg) -> {
      ItemStack left = new ItemStack(Material.GREEN_WOOL),
          right = new ItemStack(Material.YELLOW_WOOL),
          output = new ItemStack(Material.RED_WOOL);
      left.editMeta(meta -> meta.displayName(Component.text("Custom left item")));
      right.editMeta(meta -> meta.displayName(Component.text("Custom right item")));
      output.editMeta(meta -> meta.displayName(Component.text("Custom output item")));
      builder.itemLeft(left).itemRight(right).itemOutput(output);
    }));
    builderModifier.put(
        "concurrent",
        new BuilderModifier(
            "concurrent", (builder, arg) -> builder.allowConcurrentClickHandlerExecution()));
    builderModifier.put(
        "asyncclick",
        new BuilderModifier(
            "asyncclick",
            (builder, arg) ->
                builder.onClickAsync((slot, state) -> CompletableFuture.supplyAsync(() -> {
                  int id = asyncCounter.getAndIncrement();

                  state
                      .player()
                      .sendMessage(
                          "[Async] Slot: " + slot + " Text: " + state.text() + " ID: #" + id);

                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }

                  state.player().sendMessage("[Async] Done #" + id);

                  return emptyList();
                }))));
    builderModifier.put(
        "onclick",
        new BuilderModifier(
            "onclick=close closes the anvilgui\n"
                + "onclick=text replaces the current text with 'text'\n"
                + "onclick=inventory opens the own inventory",
            (builder, arg) -> builder.onClick((slot, state) -> {
              state.player().sendMessage("Slot: " + slot + " Text: " + state.text());
              if (slot != AnvilGUI.Slot.OUTPUT) {
                return emptyList();
              }

              return switch (arg.toLowerCase(Locale.ROOT)) {
                default -> emptyList();
                case "close" -> singletonList(AnvilGUI.ResponseAction.close());
                case "text" -> singletonList(AnvilGUI.ResponseAction.replaceInputText("text"));
                case "inventory" -> singletonList(
                    AnvilGUI.ResponseAction.openInventory(state.player().getInventory()));
              };
            })));
    builderModifier.put(
        "interactableslots", new BuilderModifier("interactableslots=0,1", ((builder, arg) -> {
          String[] parts = arg.split(",");
          int[] slots = new int[parts.length];
          for (int i = 0; i < parts.length; i++) {
            slots[i] = Integer.parseInt(parts[i]);
          }
          builder.interactableSlots(slots);
        })));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) return false;
    if (args.length == 0) {
      builderModifier.values().stream().map(BuilderModifier::example).forEach(sender::sendMessage);
      return true;
    }

    AnvilGUI.Builder builder = AnvilGUI.builder().plugin(plugin);

    builder.onClick((slot, state) -> {
      state.player().sendMessage("Slot: " + slot + " Text: " + state.text());
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
        builderModifier.get(key).modifier().accept(builder, value);
      } else {
        player.sendMessage("BuilderModifier " + key + " not found");
      }
    }

    builder.onClose(state -> state.player().sendMessage("Closed"));

    builder.open(player);
    return true;
  }

  private record BuilderModifier(String example, BiConsumer<AnvilGUI.Builder, String> modifier) {}
}
