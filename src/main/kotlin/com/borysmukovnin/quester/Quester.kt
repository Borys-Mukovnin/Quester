package com.borysmukovnin.quester

import com.borysmukovnin.quester.commands.QuestCommands
import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.listeners.*
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.Configurator
import com.borysmukovnin.quester.utils.PluginLogger
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.jar.JarInputStream

class Quester : JavaPlugin() {


    override fun onEnable() {

        PluginLogger.init(this)
        this.saveAllResources(this)

        Configurator.init(this)
        QuestManager.init(this)
        DialogManager.init(this)

        getCommand("q")?.setExecutor(QuestCommands(this))

        this.server.pluginManager.registerEvents(PlayerJoinListener(), this)
        this.server.pluginManager.registerEvents(PlayerQuitListener(), this)
        this.server.pluginManager.registerEvents(PlayerCommandListener(), this)
        this.server.pluginManager.registerEvents(PlayerCraftListener(), this)
        this.server.pluginManager.registerEvents(PlayerEnchantListener(), this)
        this.server.pluginManager.registerEvents(PlayerExpListener(), this)
        this.server.pluginManager.registerEvents(PlayerGotoListener(), this)
        this.server.pluginManager.registerEvents(PlayerInteractListener(), this)
        this.server.pluginManager.registerEvents(PlayerKillListener(), this)
        this.server.pluginManager.registerEvents(PlayerLootListener(), this)
        this.server.pluginManager.registerEvents(PlayerMineListener(), this)
        this.server.pluginManager.registerEvents(PlayerPickListener(), this)
        this.server.pluginManager.registerEvents(PlayerPlaceListener(), this)
        this.server.pluginManager.registerEvents(PlayerTradeListener(), this)
        this.server.pluginManager.registerEvents(PlayerTravelListener(), this)

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
