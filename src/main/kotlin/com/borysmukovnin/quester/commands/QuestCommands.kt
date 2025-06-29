package com.borysmukovnin.quester.commands

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.guis.MainGui
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.Configurator
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
            "reload" -> {
                sender.sendMessage("Reloading configuration...")
                Configurator.reload(sender) {
                    QuestManager.reload(sender) {
                        DialogManager.reload(sender)
                    }
                }

                return true
            }
            "quest" -> {
                when (args[1].lowercase()) {
                    "start" -> {
                        if (sender !is Player) {
                            sender.sendMessage("Only players can use this command.")
                            return false
                        }

                        val questName = args.getOrNull(2)
                        if (questName == null) {
                            sender.sendMessage("Invalid quest name")
                            return false
                        }

                        QuestManager.startPlayerQuest(sender,questName)
                        return true
                    }
                    else -> return false
                }
            }
            "gui" -> {
                when (args[1].lowercase()) {
                    "main" -> {
                        if (sender !is Player) {
                            sender.sendMessage("Only players can use this command.")
                            return false
                        }

                        val mainGui = MainGui(sender)
                        mainGui.open()
                        return true
                    }
                    else -> return false
                }
            }
            "dialog" -> {
                when (args[1].lowercase()) {
                    "start" -> {
                        if (sender !is Player) {
                            sender.sendMessage("Only players can use this command.")
                            return false
                        }

                        val dialog = args.getOrNull(2)
                        if (dialog == null) {
                            sender.sendMessage("Invalid dialog")
                            return false
                        }

                        DialogManager.startDialog(sender, DialogManager.getNode(dialog))
                        return true
                    }
                    "select" -> {
                        if (sender !is Player) {
                            sender.sendMessage("Only players can use this command.")
                            return false
                        }

                        val index = args[2].toIntOrNull()
                        if (index == null) {
                            sender.sendMessage("Invalid selection index.")
                            return false
                        }

                        DialogManager.handleOptionSelection(sender, index)
                        return true
                    }
                    else -> {
                        sender.sendMessage("Unknown subcommand. Usage: /q <load|save>")
                        return false
                    }
                }

            }
            else -> {
                sender.sendMessage("Unknown subcommand. Usage: /q <load|save>")
                return false
            }
        }
    }

}