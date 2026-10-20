# Chat Notifier

A client-side Fabric mod for Minecraft Java 26.3. It watches incoming player chat for configured words and phrases, then publishes matching messages to an ntfy topic. It does not require a mod on the multiplayer server.

## Build compatibility checked

The project targets Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.163.0+26.3, Fabric Loom 1.18.4, Gradle 9.7.0, Java 25 and Mod Menu 21.0.0. Fabric's archived 26.3 release announcement recommended Loom 1.17 and Gradle 9.6; the current 1.18 Loom artifact requires Gradle 9.7. The Gradle wrapper is pinned to 9.7.0.

## Install

1. Install Minecraft Java 26.3 and Fabric Loader 0.19.5 for that version.
2. Start the Fabric profile once, then close Minecraft.
3. Download Fabric API 0.163.0+26.3 and Mod Menu 21.0.0 for Fabric 26.3.
4. Put both downloaded JAR files and `chat-notifier-1.0.2.jar` in the instance's `mods` folder.
5. Start the Fabric profile. The mod is client-side; the server does not need it.

## Configure ntfy

1. Install the ntfy app on Android or iPhone and subscribe to a topic.
2. Choose a long, difficult-to-guess topic name. Public topics are not private: anyone who knows the topic may read notifications or publish to it.
3. Open Chat Notifier from Mod Menu, the Chat Notifier button on the pause menu (Esc), or `/chatnotify`.
4. Choose **Configure ntfy**, enter the server URL (`https://ntfy.sh` by default) and the exact topic name, then save.
5. For a private or self-hosted server, use its URL and an optional bearer token if required by that server. Do not share the token. The local JSON configuration is plain text and is not encrypted.
6. Use **Send test notification** or `/chatnotify test` and check the app. Failures are reported in the game log with an HTTP status hint; token contents are never included in logs.

## Keywords and commands

Use **Manage keywords** to add or edit a word or phrase, choose a row and enable/disable or delete it. Case sensitivity and whole-word matching can be changed on that screen. Changes are saved to `config/chat-notifier.json`.

- `/chatnotify` opens settings.
- `/chatnotify test` sends a test notification.
- `/chatnotify reload` reloads the local configuration file.
- `/chatnotify toggle` switches all notifications on or off immediately.

You can also edit JSON while Minecraft is closed. Make a backup first. If the file is missing or invalid, the mod creates defaults.

## Testing

Build with `gradlew.bat build` on Windows or `./gradlew build` on macOS/Linux using Java 25. In game, confirm the mod appears in Mod Menu, open the settings screen from Mod Menu and Esc, add a test keyword, enable notifications, and check that a matching incoming player message produces a notification while Minecraft is unfocused. Also test the test button, disable a keyword, switch off the master toggle, restart Minecraft, and confirm settings persisted.

## Troubleshooting and limitations

- Install matching Fabric API and Mod Menu versions for Minecraft 26.3. The Mod Menu integration is optional at runtime, but its config button requires Mod Menu.
- If no notification arrives, check server URL, topic spelling, network access, token permissions, and the game log. HTTP 401/403 indicates authentication/permission trouble; 404 suggests URL/topic trouble; 429 means the server rate-limited the request.
- Notifications require Minecraft to be running and connected to the chat. They cannot arrive after the game is completely closed.
- Chat sender identity is currently shown as “Unknown sender”; custom server chat formatting cannot be reliably parsed generically.
- Settings are stored locally without encryption. Treat the token file as sensitive.
- The JAR was installed and launched successfully with Minecraft 26.3 and Fabric. The pause-menu button and settings screens were included in that build.

### Build the JAR yourself

Install a 64-bit Java 25 JDK, open PowerShell in this project folder, and run:

```powershell
java -version
.\gradlew.bat build
```

Gradle downloads its own 9.7.0 distribution and the required Fabric dependencies. The finished JAR should appear at `build\libs\chat-notifier-1.0.2.jar`. If PowerShell says scripts are blocked, use `gradlew.bat` exactly as shown (do not use `gradlew`).

The completed JAR was built with Java 25 and tested in Minecraft 26.3. The source includes the corrected pause-menu integration. GitHub Actions or a local build will create the JAR from these source files.
