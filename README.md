
# Placeholder Annotation Processor

PAP (Placeholder Annotation Processor) is a project with the main goal is to simplify the creation of placeholders for Minecraft Plugin Developer. 

This project is greatly inspired by other similar project such as [Aikar's command framework](https://github.com/aikar/commands) or [Lamp](https://github.com/Revxrsal/lamp).

This plugin is, for now, only working with [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/#:~:text=PlaceholderAPI%20is%20a%20plugin%20for%20Spigot%20servers%20that,be%20downloaded%20in-game%20through%20the%20PAPI%20Expansion%20Cloud.) but is intended to work with other plugins in the future.


## Installation

Current Version: **1.3-SNAPSHOT**

### Maven

**pom.xml**

  ``` xml
  <repositories>
      <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
      </repository>
  </repositories>

  <dependencies>
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
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Robotv2:PlaceholderAnnotationProcessor:[VERSION]'
}
```
## Usage/Examples

```java
@Placeholder(identifier = "done")
@RequireOnlinePlayer
public String onDone(RequestIssuer issuer) {
    final QuestPlayer questPlayer = QuestPlayer.getQuestPlayer(issuer.getPlayer().getUniqueId());
    return String.valueOf(questPlayer.getNumberOfQuestsDone());
}

@DefaultPlaceholder
@Placeholder(identifier = "quest")
@RequireOnlinePlayer
public String onQuest(RequestIssuer issuer, String serviceId, Integer index, @Optional(defaultArg = "display") String param) {

    final QuestPlayer questPlayer = QuestPlayer.getQuestPlayer(issuer.getPlayer().getUniqueId());
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

