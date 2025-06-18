package com.borysmukovnin.quester

import com.borysmukovnin.quester.commands.QuestCommands
import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.listeners.PlayerJoinListener
import com.borysmukovnin.quester.listeners.PlayerQuitListener
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.PluginLogger
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.jar.JarInputStream

class Quester : JavaPlugin() {


    override fun onEnable() {

        PluginLogger.init(this)
        this.saveAllResources(this)

        QuestManager.init(this)
        DialogManager.init(this)

        getCommand("q")?.setExecutor(QuestCommands(this))

        this.server.pluginManager.registerEvents(PlayerJoinListener(), this)
        this.server.pluginManager.registerEvents(PlayerQuitListener(), this)

    }

    override fun onDisable() {
    }

    fun saveAllResources(plugin: Quester) {
        val jarUrl = plugin.javaClass.protectionDomain.codeSource.location
        val jarStream = JarInputStream(jarUrl.openStream())

        var entry = jarStream.nextJarEntry
        while (entry != null) {
            val name = entry.name
            if (!entry.isDirectory &&
                name.endsWith(".yml") &&
                !name.startsWith("META-INF") &&
                !name.contains("__MACOSX")
            ) {
                val outFile = File(plugin.dataFolder, name)
                if (!outFile.exists()) {
                    outFile.parentFile.mkdirs()
                    plugin.saveResource(name, false)
                }
            }
            entry = jarStream.nextJarEntry
        }
    }


}
