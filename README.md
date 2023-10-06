# AnvilGUI

AnvilGUI is a library to capture user input in Minecraft through an anvil inventory without the hassle of having to
handle every event and special case yourself.

**Note: This is a fork from the original project that removes the dependency on NMS code to be compatible with future
versions without updating**

## Requirements
Java 17 and recent versions of Paper.
Due to the usage of the Paper API no version dependent code is needed anymore.

## Usage

### As a dependency

Adding the required repository and dependency in Gradle KTS:
```kotlin
repositories {
  maven("https://repo.md5lukas.de/public")
}

dependencies {
  implementation("de.md5lukas:anvilgui:2.0.0-SNAPSHOT")
}
```
or Maven:
```xml
<repository>
    <id>md5lukas</id>
    <url>https://repo.md5lukas.de/public</url>
</repository>

<dependency>
    <groupId>de.md5lukas</groupId>
    <artifactId>anvilgui</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

It is best to be a good citizen and relocate the dependency to within your namespace in order
to prevent conflicts with other plugins. Here is an example of how to relocate the dependency in Gradle KTS:
```kotlin
plugins {
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks {
  shadowJar {
    archiveClassifier.set("")
    dependencies {
      include(dependency("de.md5lukas:anvilgui"))
    }
    relocate("net.wesjd.anvilgui", "[YOUR_PLUGIN_PACKAGE].anvilgui") // Replace [YOUR_PLUGIN_PACKAGE] with your package name
  }
}
```
Then you can build your final plugin jar using `./gradlew shadowJar` which will produces a jar in the `build/libs/` folder

or Maven:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>${shade.version}</version> <!-- The version must be at least 3.3.0 -->
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>net.wesjd.anvilgui</pattern>
                                <shadedPattern>[YOUR_PLUGIN_PACKAGE].anvilgui</shadedPattern> <!-- Replace [YOUR_PLUGIN_PACKAGE] with your package name -->
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### In your plugin

The `AnvilGUI.Builder` class is how you build an AnvilGUI.
The following methods allow you to modify various parts of the displayed GUI. Javadocs are available [here](https://repo.md5lukas.de/javadoc/snapshots/de/md5lukas/anvilgui/2.0.0-SNAPSHOT).

#### `onClose(Consumer<StateSnapshot>)`
Takes a `Consumer<StateSnapshot>` argument that is called when a player closes the anvil gui.
```java
builder.onClose(stateSnapshot -> {
    stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
});
```

#### `onClick(BiFunction<Integer, AnvilGUI.StateSnapshot, AnvilGUI.ResponseAction>)`
Takes a `BiFunction` with the slot that was clicked and a snapshot of the current gui state.
The function is called when a player clicks any slots in the inventory.
You must return a `List<AnvilGUI.ResponseAction>`, which could include:
- Closing the inventory (`AnvilGUI.ResponseAction.close()`)
- Replacing the input text (`AnvilGUI.ResponseAction.replaceInputText(String)`)
- Opening another inventory (`AnvilGUI.ResponseAction.openInventory(Inventory)`)
- Running generic code (`AnvilGUI.ResponseAction.run(Runnable)`)
- Nothing! (`Collections.emptyList()`)

The list of actions are ran in the order they are supplied on the next server tick.
```java
builder.onClick((slot, stateSnapshot) -> {
    if (slot != AnvilGUI.Slot.OUTPUT) {
        return Collections.emptyList();
    }

    if (stateSnapshot.getText().equalsIgnoreCase("you")) {
        stateSnapshot.getPlayer().sendMessage("You have magical powers!");
        return Arrays.asList(
            AnvilGUI.ResponseAction.close(),
            AnvilGUI.ResponseAction.run(() -> myCode(stateSnapshot.getPlayer()))
        );
    } else {
        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
    }
});
```

#### `onClickAsync(ClickHandler)`
Takes a `ClickHandler`, a shorthand for `BiFunction<Integer, AnvilGui.StateSnapshot, CompletableFuture<AnvilGUI.ResponseAction>>`,
that behaves exactly like `onClick()` with the difference that it returns a `CompletableFuture` and therefore allows for
asynchronous calculation of the `ResponseAction`s, for example performing database accesses.

```java
builder.onClickAsync((slot, stateSnapshot) -> CompletedFuture.supplyAsync(() -> {
    // this code is now running async
    if (slot != AnvilGUI.Slot.OUTPUT) {
        return Collections.emptyList();
    }

    if (database.isMagical(stateSnapshot.getText())) {
        // the `ResponseAction`s will run on the main server thread
        return Arrays.asList(
            AnvilGUI.ResponseAction.close(),
            AnvilGUI.ResponseAction.run(() -> myCode(stateSnapshot.getPlayer()))
        );
    } else {
        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
    }
}));
```

#### `allowConcurrentClickHandlerExecution()`
Tells the AnvilGUI to disable the mechanism that is put into place to prevent concurrent execution of the
click handler set by `onClickAsync(ClickHandler)`.
```java
builder.allowConcurrentClickHandlerExecution();
```

#### `interactableSlots(int... slots)`
This allows or denies users to take / input items in the anvil slots that are provided. This feature is useful when you try to make a inputting system using an anvil gui.
```java
builder.interactableSlots(Slot.INPUT_LEFT, Slot.INPUT_RIGHT);
```

#### `preventClose()`
Tells the AnvilGUI to prevent the user from pressing escape to close the inventory.
Useful for situations like password input to play.
```java
builder.preventClose();
```

#### `text(String)`
Takes a `String` that contains what the initial text in the renaming field should be set to.
If `itemLeft` is provided, then the display name is set to the provided text. If no `itemLeft`
is set, then a piece of paper will be used.
The provided text will be used literally without applying formatting.
```java
builder.text("What is the meaning of life?");
```

#### `text(Component)`
Takes a `Component` that contains what the initial text in the renaming field should be set to.
If `itemLeft` is provided, then the display name is set to the provided text. If no `itemLeft`
is set, then a piece of paper will be used.
```java
builder.text(Component.text("What is the meaning of life?", NamedTextColor.GOLD));
```

#### `itemLeft(ItemStack)`
Takes a custom `ItemStack` to be placed in the left input slot.
```java
ItemStack stack = new ItemStack(Material.IRON_SWORD);
stack.editMeta(meta -> {
    meta.setLore(Arrays.asList("Sharp iron sword"));
});
builder.itemLeft(stack);
```

#### `itemRight(ItemStack)`
Takes a custom `ItemStack` to be placed in the right input slot.
```java
ItemStack stack = new ItemStack(Material.IRON_INGOT);
stack.editMeta(meta -> {
    meta.setLore(Arrays.asList("A piece of metal"));
});
builder.itemRight(stack);
```

#### `itemOutput(ItemStack)`
Takes a custom `ItemStack` to be placed in the output slot.
This item will not be overwritten by the result of combining the input items
```java
ItemStack stack = new ItemStack(Material.IRON_SWORD);
stack.editMeta(meta -> {
    meta.setLore(Arrays.asList("An enhanced sharp iron sword"));
});
builder.itemRight(stack);
```

#### `title(String)`
Takes a `String` that will be used literally as the inventory title.
```java
builder.title("Enter your answer");
```

#### `title(Component)`
Takes a `Component` with formatting that will be used as the inventory title.
```java
builder.title(MiniMessage.miniMessage().deserialize("<rainbow>A colorful title :)"));
```

#### `plugin(Plugin)`
Takes the `Plugin` object that is making this anvil gui. It is needed to register listeners and
execute tasks on the main thread scheduler.
```java
builder.plugin(pluginInstance);
```

#### `open(Player)`
Takes a `Player` that the anvil gui should be opened for. This method can be called multiple times without needing to create
a new `AnvilGUI.Builder` object.
```java
builder.open(player);
```

### A Common Use Case Example
```java
AnvilGUI.builder()
    .onClose(stateSnapshot -> {
        stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
    })
    .onClick((slot, stateSnapshot) -> { // Either use sync or async variant, not both
        if(slot != AnvilGUI.Slot.OUTPUT) {
            return Collections.emptyList();
        }

        if(stateSnapshot.getText().equalsIgnoreCase("you")) {
            stateSnapshot.getPlayer().sendMessage("You have magical powers!");
            return Arrays.asList(AnvilGUI.ResponseAction.close());
        } else {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
        }
    })
    .preventClose()                                                    //prevents the inventory from being closed
    .text("What is the meaning of life?")                              //sets the text the GUI should start with
    .title("Enter your answer.")                                       //set the title of the GUI
    .plugin(myPluginInstance)                                          //set the plugin instance
    .open(myPlayer);                                                   //opens the GUI for the player provided
```


## Development
We use Gradle to handle our dependencies. Run `./gradlew build` using Java 17 to build the project.

### Spotless
The project utilizes the [Spotless Gradle Plugin](https://github.com/diffplug/spotless/tree/main/plugin-gradle) to
enforce style guidelines. You will not be able to build the project if your code does not meet the guidelines.
To fix all code formatting issues, simply run `./gradlew spotlessApply`.

## License
This project is licensed under the [MIT License](LICENSE).
