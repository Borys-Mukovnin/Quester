package com.borysmukovnin.quester.dialogs

import com.borysmukovnin.quester.dialogs.models.DialogNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

class DialogSession(val player: Player, var currentNode: DialogNode)

object DialogManager {
    private val sessions = mutableMapOf<UUID, DialogSession>()
    private val nodes = mutableMapOf<String, DialogNode>()

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
    }

    fun endDialog(player: Player) {
        sessions.remove(player.uniqueId)
    }

    fun showDialog(player: Player, node: DialogNode) {
        val component = Component.text(node.text + "\n")
        node.options.forEachIndexed { i, option ->
            val clickable = Component.text("[${option.text}]")
                .clickEvent(ClickEvent.runCommand("/dialog select $i"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to select")))
            component.append(clickable).append(Component.text(" "))
        }
        player.sendMessage(component)
    }

    fun loadDialogNodes() {
        val dialogFolder = File(Bukkit.getPluginManager().getPlugin("YourPluginName")!!.dataFolder, "dialogs")
        if (!dialogFolder.exists()) dialogFolder.mkdirs()

        dialogFolder.listFiles { file -> file.extension.equals("yml", ignoreCase = true) }?.forEach { file ->
            val config = YamlConfiguration.loadConfiguration(file)
            for (key in config.getKeys(false)) {
                val section = config.getConfigurationSection(key) ?: continue
                val text = section.getString("text") ?: continue

                val options = section.getMapList("options").mapNotNull { optMap ->
                    val optText = optMap["text"] as? String ?: return@mapNotNull null
                    val nextNodeId = optMap["nextNodeId"] as? String

                    val conditions = (optMap["conditions"] as? List<Map<String, Any>>)?.mapNotNull { map ->
                        parseCondition(map)
                    } ?: emptyList()

                    val actions = (optMap["actions"] as? List<Map<String, Any>>)?.mapNotNull { map ->
                        parseAction(map)
                    } ?: emptyList()

                    DialogOption(optText, nextNodeId, conditions, actions)
                }

                nodes[key] = DialogNode(id = key, text = text, options = options)
            }
        }
    }
}
