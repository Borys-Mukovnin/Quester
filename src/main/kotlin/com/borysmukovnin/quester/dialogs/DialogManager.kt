package com.borysmukovnin.quester.dialogs

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.models.dataclasses.DialogNode
import com.borysmukovnin.quester.models.dataclasses.DialogOption
import com.borysmukovnin.quester.models.dataclasses.DialogSession
import com.borysmukovnin.quester.models.dataclasses.PlayerDialogData
import com.borysmukovnin.quester.models.dataclasses.Options
import com.borysmukovnin.quester.models.dataclasses.Status
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.PluginLogger
import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.time.Instant
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
    private val activePlayerDialogs: MutableMap<UUID, MutableMap<String, PlayerDialogData>> = mutableMapOf()

    fun startDialog(player: Player, startNode: DialogNode) {
        val playerDialogs = activePlayerDialogs[player.uniqueId] ?: mutableMapOf()

        val existingData = playerDialogs[startNode.id]
        if (existingData != null) {
            when (existingData.Status) {
                Status.ACTIVE -> {
                    player.sendMessage("Dialog is already active")
                    sessions[player.uniqueId] = DialogSession(player, startNode)
                    showDialog(player,startNode)
                    return
                }
                Status.COMPLETED -> {
                    val repeatable = nodes[startNode.id]?.Settings?.Repeatable ?: 1
                    if (repeatable >= 0 && existingData.TimesCompleted >= repeatable) {
                        player.sendMessage("Dialog has been completed maximum allowed times")
                        return
                    }
                    playerDialogs[startNode.id] = existingData.copy(
                        Status = Status.ACTIVE,
                        LastStarted = Instant.now()
                    )
                    activePlayerDialogs[player.uniqueId] = playerDialogs
                    sessions[player.uniqueId] = DialogSession(player, startNode)
                    showDialog(player, startNode)
                    return
                }
                Status.INACTIVE -> {
                    playerDialogs[startNode.id] = existingData.copy(
                        Status = Status.ACTIVE,
                        LastStarted = Instant.now()
                    )
                    activePlayerDialogs[player.uniqueId] = playerDialogs
                    sessions[player.uniqueId] = DialogSession(player, startNode)
                    showDialog(player, startNode)
                    return
                }
            }
        } else {
            val newData = PlayerDialogData(
                DialogNode = startNode,
                Status = Status.ACTIVE,
                LastStarted = Instant.now(),
                TimesCompleted = 0
            )
            playerDialogs[startNode.id] = newData
            activePlayerDialogs[player.uniqueId] = playerDialogs
            sessions[player.uniqueId] = DialogSession(player, startNode)
            showDialog(player, startNode)
        }
    }

    fun handleOptionSelection(player: Player, optionIndex: Int) {
        val session = sessions[player.uniqueId] ?: return
        val option = session.currentNode.options.getOrNull(optionIndex) ?: return
        val dialogId = session.currentNode.id

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

        val playerId = player.uniqueId

        val playerDialogs = activePlayerDialogs[playerId] ?: return
        val existing = playerDialogs[dialogId] ?: return
        playerDialogs[dialogId] = existing.copy(
            Status = Status.COMPLETED,
            TimesCompleted = existing.TimesCompleted + 1
        )
    }

    fun endDialog(player: Player) {
        sessions.remove(player.uniqueId)
    }

    fun showDialog(player: Player, node: DialogNode) {
        var component = node.text.applyVariables(player).asFormattedComponent()

        node.options.forEachIndexed { i, option ->
            if (!option.conditions.all { it.isMet(player) }) return@forEachIndexed

            val clickable = (option.text + "\n").applyVariables(player).asFormattedComponent()
                .clickEvent(ClickEvent.runCommand("/q dialog select $i"))
                .hoverEvent(HoverEvent.showText(option.hover.applyVariables(player).asFormattedComponent()))

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
                    val settingStr = section.getString("settings")
                    var settings = Options(1,true,null,null,null)
                    if (settingStr != null) {
                        settings = QuestManager.getOption(settingStr)
                    }


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

                    nodes[key] = DialogNode(id = key, text = prompt, options = options, Settings = settings)
                }
            }
    }

    fun loadPlayerDialogsAsync(player: Player, onComplete: Runnable? = null) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val file = File(plugin.dataFolder, "player_data/${player.uniqueId}.yml")

            if (!file.exists()) return@Runnable

            val config = YamlConfiguration.loadConfiguration(file)
            val completedDialogsSection = config.getConfigurationSection("dialogs") ?: return@Runnable

            val playerDialogDataMap = mutableMapOf<String, PlayerDialogData>()

            for (dialogKey in completedDialogsSection.getKeys(false)) {
                val dialogSection = completedDialogsSection.getConfigurationSection(dialogKey) ?: continue

                val status = try {
                    Status.valueOf(dialogSection.getString("status", "INACTIVE")!!.uppercase())
                } catch (e: Exception) {
                    Status.INACTIVE
                }

                val timesCompleted = dialogSection.getInt("times_completed", 0)

                val lastStarted = dialogSection.getString("last_started")?.let {
                    try {
                        Instant.parse(it)
                    } catch (e: Exception) {
                        Instant.EPOCH
                    }
                } ?: Instant.EPOCH

                val dialogNode = nodes[dialogKey] ?: continue

                val playerDialogData = PlayerDialogData(
                    DialogNode = dialogNode,
                    Status = status,
                    LastStarted = lastStarted,
                    TimesCompleted = timesCompleted
                )

                playerDialogDataMap[dialogKey] = playerDialogData
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                activePlayerDialogs[player.uniqueId] = playerDialogDataMap
                onComplete?.run()
            })
        })
    }

    fun savePlayerCompletedDialogAsync(player: Player, onComplete: Runnable? = null) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val file = File(plugin.dataFolder, "player_data/${player.uniqueId}.yml")
                if (!file.exists()) {
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                }

                val config = YamlConfiguration.loadConfiguration(file)

                val dialogsSection = config.getConfigurationSection("dialogs") ?: config.createSection("dialogs")

                val dialogs = activePlayerDialogs[player.uniqueId] ?: emptyMap()

                for ((dialogName, playerDialogData) in dialogs) {
                    val dialogSection = dialogsSection.createSection(dialogName)
                    dialogSection.set("status", playerDialogData.Status.name.lowercase())
                    dialogSection.set("times_completed", playerDialogData.TimesCompleted)
                    dialogSection.set("last_started", playerDialogData.LastStarted.toString())
                }

                config.save(file)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    this.activePlayerDialogs.remove(player.uniqueId)
                    onComplete?.run()
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })


    }

    fun getNode(name: String) : DialogNode {
        return nodes[name] ?: error("There is no dialog with name $name")
    }
}