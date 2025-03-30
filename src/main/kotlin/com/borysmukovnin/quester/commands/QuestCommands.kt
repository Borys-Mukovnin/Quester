package com.borysmukovnin.quester.commands

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.quests.QuestManager
import com.sun.source.util.Plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QuestCommands(private val plugin: Quester, private val questManager: QuestManager) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // If no arguments are provided, show the help message
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /q <subcommand> [options]")
            return false
        }

        when (args[0].lowercase()) {
            "load" -> {
                // Handle /q load
                handleLoadCommand(sender)
                return true
            }
            "save" -> {
                // Handle /q save
                handleSaveCommand(sender)
                return true
            }
            else -> {
                // If the subcommand is unknown
                sender.sendMessage("Unknown subcommand. Usage: /q <load|save>")
                return false
            }
        }
    }

    // Handle /q load
    private fun handleLoadCommand(sender: CommandSender) {
        if (true) {

            // Add logic for loading quests here
            sender.sendMessage("Loading quests...")
            questManager.loadAllQuests()


        } else {
            sender.sendMessage("Only players can use the load command.")
        }
    }

    // Handle /q save
    private fun handleSaveCommand(sender: CommandSender) {
        if (sender is Player) {
            // Add logic for saving quests here
            sender.sendMessage("Saving quests...")
        } else {
            sender.sendMessage("Only players can use the save command.")
        }
    }
}