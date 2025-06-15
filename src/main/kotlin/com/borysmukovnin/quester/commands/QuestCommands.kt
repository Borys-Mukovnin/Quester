package com.borysmukovnin.quester.commands

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.quests.QuestManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QuestCommands(private val plugin: Quester) : CommandExecutor {

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
            "dialog" -> {
                handleDialogSelectCommand(sender,args.drop(1).toTypedArray())
                return true
            }
            "startdialog" -> {
                handleDialogStartCommand(sender,args.drop(1).toTypedArray())
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
            QuestManager.loadQuests()


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

    fun handleDialogSelectCommand(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return false
        }

        val index = args.getOrNull(0)?.toIntOrNull()
        if (index == null) {
            sender.sendMessage("Invalid selection index.")
            return false
        }

        DialogManager.handleOptionSelection(sender, index)
        return true
    }

    fun handleDialogStartCommand(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return false
        }

        val dialog = args.getOrNull(0)
        if (dialog == null) {
            sender.sendMessage("Invalid dialog")
            return false
        }

        DialogManager.startDialog(sender, DialogManager.getNode(dialog))
        return true
    }

}