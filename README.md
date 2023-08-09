
# Placeholder Annotation Processor

PAP (Placeholder Annotation Processor) intends to simplify the creation of placeholders for Minecraft Plugin Developers.

This project draws inspiration from similar project like [Aikar's command framework](https://github.com/aikar/commands) or [Lamp](https://github.com/Revxrsal/lamp).

This plugin is, for now, only working with [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/#:~:text=PlaceholderAPI%20is%20a%20plugin%20for%20Spigot%20servers%20that,be%20downloaded%20in-game%20through%20the%20PAPI%20Expansion%20Cloud.) but is intended to work with other plugins in the future.


## Usage/Examples

### How to use 

**Instantiate a PlaceholderAnnotationProcessor object**

```java
final PlaceholderAnnotationProcessor processor = PlaceholderAnnotationProcessor.create();
```

**Create a java class that extend 'BasePlaceholderExpansion'**

```java
public class MyCustomPlaceholder extends BasePlaceholderExpansion {
}
```

This class has the same properties than the 'PlaceholderExpansion' class from PlaceholderAPI.

**Add your placeholder's methods**

```java
@Placeholder(identifier = "done")
@RequireOnlinePlayer
public String onDone(RequestIssuer issuer) {
    final QuestPlayer questPlayer = QuestPlayer.getQuestPlayer(issuer.getPlayer().getUniqueId());

    if(questPlayer == null) {
        return null;
    }

    return String.valueOf(questPlayer.getNumberOfQuestsDone());
}

@DefaultPlaceholder
@Placeholder(identifier = "quest")
@RequireOnlinePlayer
public String onQuest(RequestIssuer issuer, String serviceId, Integer index, @Optional(defaultArg = "display") String param) {

    final QuestPlayer questPlayer = QuestPlayer.getQuestPlayer(issuer.getPlayer().getUniqueId());
    if(questPlayer == null) {
        return null;
    }
        
    final List<ActiveQuest> activeQuests = questPlayer.getActiveQuests(serviceId);

    if(activeQuests.size() <= index) {
        return null;
    }

    final ActiveQuest activeQuest = activeQuests.get(index);
    final Quest quest = plugin.getQuestManager().fromId(activeQuest.getQuestId());

    switch (param.toLowerCase(Locale.ROOT)) {
        case "display":
            return quest.getDisplay();
        case "type":
            return quest.getType().name().toLowerCase(Locale.ROOT);
        case "progress":
            return String.valueOf(activeQuest.getProgress());
        case "required":
            return String.valueOf(quest.getRequiredAmount());
        case "progressbar":
            return ProgressUtil.getProcessBar(activeQuest.getProgress(), quest.getRequiredAmount());
        default:
            return null;
        }
    }
```

**Finally, register your expansion to the PlaceholderAnnotationProcessor AND PlaceholderAPI**

```java
MyCustomPlaceholder myCustomPlaceholder = new MyCustomPlaceholder(processor);
processor.register(clipPlaceholder);
myCustomPlaceholder.register();
```

And Your Done !


## Installation

Current Version: **1.3-SNAPSHOT**

### Maven

**pom.xml**

  ``` xml
<repositories>
    <!-- Other repositories... -->
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- Other dependencies... -->
    <dependency>
        <groupId>com.github.Robotv2</groupId>
        <artifactId>PlaceholderAnnotationProcessor</artifactId>
        <version>[VERSION]</version>
    </dependency>
</dependencies>
  ```

### Gradle

**build.gradle (Groovy)**

```groovy
repositories {
    // Other repositories...
    maven { url = 'https://jitpack.io' }
}

dependencies {
    // Other dependencies...
    implementation 'com.github.Robotv2:PlaceholderAnnotationProcessor:[VERSION]'
}
```
