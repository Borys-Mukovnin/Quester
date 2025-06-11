package com.borysmukovnin.quester

import com.borysmukovnin.quester.commands.QuestCommands
import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.PluginLogger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import java.io.File
import java.io.InputStream

class Quester : JavaPlugin() {

    private val questManager = QuestManager(this)

    override fun onEnable() {

        logger.info("Enabled")
        PluginLogger.init(this)

        DialogManager.loadDialogNodes()

        questManager.loadAllQuests()

        copyResource("config.yml")
        copyResource("quests/questname.yml")
        copyResource("progress/uuid.yml")

        getCommand("q")?.setExecutor(QuestCommands(this,questManager))

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun copyResource(resourceName: String) {
        // Ensure plugin folder exists
        val pluginFolder = dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        // Get the resource as a stream from the JAR file
        val resourceStream: InputStream? = this.javaClass.classLoader.getResourceAsStream(resourceName)
            ?: run {
                logger.warning("Resource '$resourceName' not found!")
                return
            }

        // Determine the destination file
        val destinationFile = File(pluginFolder, resourceName)

        // Ensure the parent directory exists
        destinationFile.parentFile?.mkdirs()

        // Copy the resource only if it doesn't already exist
        if (!destinationFile.exists()) {
            try {
                resourceStream.use { input ->
                    if (input != null) {
                        destinationFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    logger.info("Successfully copied resource: $resourceName")
                }
            } catch (e: Exception) {
                logger.warning("Failed to copy resource '$resourceName': ${e.message}")
            }
        } else {
            logger.info("Resource '$resourceName' already exists, skipping copy.")
        }
    }
}
