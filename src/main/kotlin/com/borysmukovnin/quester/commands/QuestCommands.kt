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
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /q <subcommand> [options]")
            return false
        }

        when (args[0].lowercase()) {
            "load" -> {
                handleLoadCommand(sender)
                return true
            }
            "save" -> {
                handleSaveCommand(sender)
                return true
            }
            else -> {
                sender.sendMessage("Unknown subcommand. Usage: /q <load|save>")
                return false
            }
        }
    }

    private fun handleStartQuestCommand(sender: CommandSender) {
        if (sender !is Player) println("Only players can execute this command"); return


    }

    private fun handleLoadCommand(sender: CommandSender) {
        if (true) {

            sender.sendMessage("Loading quests...")
            questManager.loadAllQuests()


        } else {
            sender.sendMessage("Only players can use the load command.")
        }
    }

    private fun handleSaveCommand(sender: CommandSender) {
        if (sender is Player) {
            sender.sendMessage("Saving quests...")
        } else {
            sender.sendMessage("Only players can use the save command.")
        }
    }
}