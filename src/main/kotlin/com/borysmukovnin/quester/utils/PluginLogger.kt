package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.Quester
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import java.util.logging.Logger

object PluginLogger {

    private const val PREFIX = "[Quester]"
    lateinit var logger: Logger

    fun init(plugin: Quester) {
        logger = plugin.logger
    }

    fun logInfo(msg: String) {
        logger.info("$PREFIX $msg")
    }
}