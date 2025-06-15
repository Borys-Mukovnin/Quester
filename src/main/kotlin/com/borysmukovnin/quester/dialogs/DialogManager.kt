package com.borysmukovnin.quester.dialogs

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.dialogs.models.DialogNode
import com.borysmukovnin.quester.dialogs.models.DialogOption
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.PluginLogger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

object DialogManager {
    lateinit var plugin: Quester

    fun init(plugin: Quester) {
        this.plugin = plugin
        this.reload()
    }
    fun reload(sender: CommandSender? = null,onComplete: (() -> Unit)? = null) {
        if (sender == null) {
            this.loadDialogNodes()

            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(QuestManager.plugin, Runnable {
            this.loadDialogNodes()

            Bukkit.getScheduler().runTask(QuestManager.plugin, Runnable {
                sender.sendMessage("Dialog configuration reload complete.")
            })

            onComplete?.invoke()
        })
    }

    private val sessions = mutableMapOf<UUID, DialogSession>()
    private val nodes = mutableMapOf<String, DialogNode>()
    private val completedDialogs: MutableMap<UUID, MutableList<String>> = mutableMapOf()

    fun startDialog(player: Player, startNode: DialogNode) {
        sessions[player.uniqueId] = DialogSession(player, startNode)
        showDialog(player, startNode)
    }

    fun handleOptionSelection(player: Player, optionIndex: Int) {
        val session = sessions[player.uniqueId] ?: return
        val option = session.currentNode.options.getOrNull(optionIndex) ?: return

        if (option.conditions.all { it.isMet(player) }) {
            option.actions.forEach { it.execute(player) }

            if (option.nextNodeId != null) {
                val nextNode = nodes[option.nextNodeId] ?: return
                session.currentNode = nextNode
                showDialog(player, nextNode)
            } else {
                endDialog(player)
            }
        } else {
            player.sendMessage("You can't choose this option.")
        }
        this.savePlayerCompletedDialogAsync(player)
    }

    fun endDialog(player: Player) {
        sessions.remove(player.uniqueId)
    }

    fun showDialog(player: Player, node: DialogNode) {
        val mini = MiniMessage.miniMessage()
        var component = mini.deserialize("<gray>${node.text}</gray>\n")
        node.options.forEachIndexed { i, option ->
            if (!option.conditions.all { it.isMet(player) }) return@forEachIndexed

            val clickable = mini.deserialize("<green>[${option.text}]</green>")
                .clickEvent(ClickEvent.runCommand("/q dialog select $i"))
                .hoverEvent(HoverEvent.showText(mini.deserialize(option.hover)))

            component = component.append(clickable).append(Component.space())
        }

        player.sendMessage(component)
    }

    private fun loadDialogNodes() {
        nodes.clear()

        val dialogFolder = File(plugin.dataFolder, "dialogs")
        if (!dialogFolder.exists()) dialogFolder.mkdirs()

        dialogFolder.walkTopDown()
            .filter { it.isFile && it.extension.equals("yml", ignoreCase = true) }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: continue
                    val prompt = section.getString("prompt") ?: continue

                    val options = section.getMapList("options").mapNotNull { optMap ->
                        val optText = optMap["text"] as? String ?: return@mapNotNull null
                        val optHover = optMap["hover"] as? String ?: ""
                        val nextNode = optMap["nextNode"] as? String

                        val conditionNames = (optMap["conditions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val conditions = conditionNames.map { QuestManager.getCondition(it) }

                        val actionNames = (optMap["actions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val actions = actionNames.map { QuestManager.getAction(it) }

                        DialogOption(optText, optHover, nextNode, conditions, actions)
                    }

                    if (nodes.containsKey(key)) {
                        PluginLogger.logInfo("Duplicate dialog node ID '$key' in file: ${file.path}")
                        continue
                    }

                    nodes[key] = DialogNode(id = key, text = prompt, options = options)
                }
            }
    }

    fun loadPlayerCompletedDialogAsync(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val file = File(plugin.dataFolder, "player_data/${player.uniqueId}.yml")
            if (!file.exists()) return@Runnable

            val config = YamlConfiguration.loadConfiguration(file)
            val dialogs = config.getStringList("completed_dialogs")

            Bukkit.getScheduler().runTask(plugin, Runnable {
                completedDialogs[player.uniqueId] = dialogs.toMutableList()
            })
        })
    }

    fun savePlayerCompletedDialogAsync(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val file = File(plugin.dataFolder, "player_data/${player.uniqueId}.yml")
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val dialogs = completedDialogs[player.uniqueId]?.toList() ?: emptyList()
            config.set("completed_dialogs", dialogs)
            config.save(file)
        })
    }

    fun getNode(name: String) : DialogNode {
        return nodes[name] ?: error("There is no dialog with name $name")
    }
}

class DialogSession(val player: Player, var currentNode: DialogNode)