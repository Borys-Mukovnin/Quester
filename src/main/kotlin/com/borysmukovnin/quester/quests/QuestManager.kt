package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.models.conditions.*
import com.borysmukovnin.quester.models.objectives.*
import com.borysmukovnin.quester.models.actions.CommandAction
import com.borysmukovnin.quester.models.actions.ExpAction
import com.borysmukovnin.quester.models.actions.ItemAction
import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.models.actions.StartQuestAction
import com.borysmukovnin.quester.models.dataclasses.Action
import com.borysmukovnin.quester.models.dataclasses.Condition
import com.borysmukovnin.quester.models.dataclasses.ItemLocation
import com.borysmukovnin.quester.models.dataclasses.Mode
import com.borysmukovnin.quester.models.dataclasses.Objective
import com.borysmukovnin.quester.models.dataclasses.Options
import com.borysmukovnin.quester.models.dataclasses.PlayerQuestData
import com.borysmukovnin.quester.models.dataclasses.Quest
import com.borysmukovnin.quester.models.dataclasses.Stage
import com.borysmukovnin.quester.models.dataclasses.Status
import com.borysmukovnin.quester.models.dataclasses.TravelMode
import com.borysmukovnin.quester.models.dataclasses.Weather
import com.borysmukovnin.quester.utils.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*

object QuestManager {

    lateinit var plugin: Quester

    fun init(plugin: Quester) {
        this.plugin = plugin
        this.reload()
    }

    fun reload(sender: CommandSender? = null, onComplete: (() -> Unit)? = null) {
        if (sender == null) {
            this.loadConditions()
            this.loadActions()
            this.loadObjectives()
            this.loadOptions()

            this.loadQuests()

            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            this.loadConditions()
            this.loadActions()
            this.loadObjectives()
            this.loadOptions()

            this.loadQuests()

            Bukkit.getScheduler().runTask(plugin, Runnable {
                sender.sendMessage("Quest configuration reload complete.")
            })

            onComplete?.invoke()
        })
    }


    private val activePlayersQuests: MutableMap<UUID, MutableMap<String, PlayerQuestData>> = mutableMapOf()
    private val quests: MutableMap<String, Quest> = mutableMapOf()
    private val actions: MutableMap<String, Action> = mutableMapOf()
    private val conditions: MutableMap<String, Condition> = mutableMapOf()
    private val objectives: MutableMap<String, Objective> = mutableMapOf()
    private val options: MutableMap<String, Options> = mutableMapOf()


    private fun loadQuests() {
        quests.clear()

        val questsFolder = File(plugin.dataFolder, "quests")
        if (!questsFolder.exists()) return

        questsFolder.walkTopDown()
            .filter { it.isFile && it.extension.equals("yml", ignoreCase = true) }
            .forEach { file ->
                quests[file.nameWithoutExtension] = parseQuest(file)
            }
    }

    fun startPlayerQuest(player: Player, questName: String) {
        val playerId = player.uniqueId
        val playerQuests = activePlayersQuests.getOrPut(playerId) { mutableMapOf() }
        val existingData = playerQuests[questName]

        when (existingData?.Status) {
            Status.ACTIVE -> {
                player.sendMessage("Quest is already active")
                return
            }

            Status.COMPLETED -> {
                val repeatable = existingData.Quest.Options.Repeatable
                if (repeatable >= 0 && existingData.TimesCompleted >= repeatable) {
                    player.sendMessage("Quest has been completed maximum allowed times")
                    return
                }

                playerQuests[questName] = existingData.copy(
                    Status = Status.ACTIVE,
                    LastStarted = Instant.now()
                )
                player.sendMessage("Quest restarted")
                return
            }

            Status.INACTIVE -> {
                playerQuests[questName] = existingData.copy(
                    Status = Status.ACTIVE,
                    LastStarted = Instant.now()
                )
                player.sendMessage("Quest ${existingData.Quest.Name} started")
                return
            }

            null -> {
                val quest = quests[questName]?.deepCopy() ?: run {
                    player.sendMessage("No such quest exists")
                    return
                }

                val newData = PlayerQuestData(
                    Quest = quest,
                    Status = Status.ACTIVE,
                    LastStarted = Instant.now(),
                    TimesCompleted = 0
                )

                playerQuests[questName] = newData
                player.sendMessage("Quest ${existingData?.Quest?.Name} started")
            }
        }
    }

    fun stopPlayerQuest(player: Player, questName: String) {
        val playerQuestMap = activePlayersQuests[player.uniqueId]
        val playerQuestData = playerQuestMap?.get(questName)

        if (playerQuestData == null || playerQuestData.Status != Status.ACTIVE) {
            player.sendMessage("Quest is not active")
            return
        }

        val cancelable = playerQuestData.Quest.Options.Cancelable
        if (!cancelable) {
            player.sendMessage("This quest cannot be cancelled")
            return
        }

        playerQuestMap[questName] = playerQuestData.copy(Status = Status.INACTIVE)
        player.sendMessage("Quest has been cancelled")
    }

    fun saveQuestProgressAsync(player: Player, onComplete: Runnable? = null) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val playerFile = File(plugin.dataFolder, "player_data/${player.uniqueId}.yml")
                if (!playerFile.exists()) {
                    playerFile.parentFile.mkdirs()
                    playerFile.createNewFile()
                }

                val config = YamlConfiguration.loadConfiguration(playerFile)
                val questsSection = config.getConfigurationSection("quests") ?: config.createSection("quests")

                val playerQuests = activePlayersQuests[player.uniqueId] ?: return@Runnable
                for ((questName, playerQuestData) in playerQuests) {
                    val questSubSection = questsSection.getConfigurationSection(questName) ?: questsSection.createSection(questName)
                    questSubSection.set("status", playerQuestData.Status.name.lowercase())
                    questSubSection.set("times_completed", playerQuestData.TimesCompleted)
                    questSubSection.set("last_started", playerQuestData.LastStarted.toString())

                    val stagesSection = questSubSection.getConfigurationSection("stages") ?: questSubSection.createSection("stages")
                    for ((stageName, stage) in playerQuestData.Quest.Stages) {
                        val stageSection = stagesSection.getConfigurationSection(stageName) ?: stagesSection.createSection(stageName)
                        val objectiveProgress = stage.Objectives.map { it.ProgressCurrent }

                        stageSection.set("objectives", objectiveProgress)
                    }
                }

                config.save(playerFile)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    this.activePlayersQuests.remove(player.uniqueId)
                    onComplete?.run()
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

    }

    fun loadActivePlayerQuestsAsync(player: Player, onComplete: Runnable? = null) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val dir = File(plugin.dataFolder, "player_data")
            val configFile = File(dir, "${player.uniqueId}.yml")
            if (!configFile.exists()) return@Runnable

            val config = YamlConfiguration.loadConfiguration(configFile)
            val activeQuestsSection = config.getConfigurationSection("quests") ?: return@Runnable

            val playerQuestDataMap = mutableMapOf<String, PlayerQuestData>()

            for (questKey in activeQuestsSection.getKeys(false)) {
                val questSection = activeQuestsSection.getConfigurationSection(questKey) ?: continue
                val stagesSection = questSection.getConfigurationSection("stages") ?: continue

                val status = try {
                    Status.valueOf(questSection.getString("status", "INACTIVE")!!.uppercase())
                } catch (e: Exception) {
                    Status.INACTIVE
                }

                val timesCompleted = questSection.getInt("times_completed", 0)
                val lastStarted = questSection.getString("last_started")?.let {
                    try {
                        Instant.parse(it)
                    } catch (e: Exception) {
                        Instant.EPOCH
                    }
                } ?: Instant.EPOCH

                val quest = quests[questKey]?.deepCopy() ?: continue

                for (stageKey in stagesSection.getKeys(false)) {
                    val stageSection = stagesSection.getConfigurationSection(stageKey) ?: continue
                    val objectiveProgress = stageSection.getList("objectives")?.mapNotNull {
                        it as? Int
                    } ?: continue

                    val stage = quest.Stages[stageKey] ?: continue
                    if (objectiveProgress.size != stage.Objectives.size) continue

                    for ((i, obj) in stage.Objectives.withIndex()) {
                        obj.ProgressCurrent = objectiveProgress[i]
                    }
                }

                val playerQuestData = PlayerQuestData(
                    Quest = quest,
                    Status = status,
                    LastStarted = lastStarted,
                    TimesCompleted = timesCompleted
                )

                playerQuestDataMap[questKey] = playerQuestData
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                activePlayersQuests[player.uniqueId] = playerQuestDataMap
                onComplete?.run()
            })
        })
    }

    private fun parseQuest(file: File): Quest {
        val config = YamlConfiguration.loadConfiguration(file)
        val name = config.getRequiredString("name",file.name) ?: "Default Name"
        val description = config.getStringList("description")

        val startConditions = config.getStringList("start_conditions").mapNotNull { conditions[it] }
        val startActions = config.getStringList("start_actions").mapNotNull { actions[it] }

        val stagesSection = config.getRequiredConfigurationSection("stages",file.name) ?: YamlConfiguration().createSection("stages")
        val stages = mutableMapOf<String, Stage>()

        for (key in stagesSection.getKeys(false)) {
            val stageSection = stagesSection.getConfigurationSection(key) ?: continue

            val stageName = stageSection.getString("name") ?: key

            val stageDescription = stageSection.getStringList("description")

            val objectiveIds = stageSection.getStringList("objectives")
            val objectiveList = objectiveIds.mapNotNull { objectives[it]?.deepCopy() }.toMutableList()

            val actionIds = stageSection.getStringList("actions")
            val actionList = actionIds.mapNotNull { actions[it] }

            val stage = Stage(
                Name = stageName,
                Description = stageDescription,
                Objectives = objectiveList,
                Options = options[config.getString("options") ?: ""] ?: Options(1, true, null, null, null),
                Actions = actionList
            )

            stages[key] = stage
        }

        return Quest(
            Name = name,
            Description = description,
            Options = options[config.getString("options") ?: ""] ?: Options(1, true, null, null, null),
            StartConditions = startConditions,
            StartActions = startActions,
            Stages = stages
        )
    }

    private fun loadActions() {
        actions.clear()

        val actionsFolder = File(plugin.dataFolder, "actions")
        if (!actionsFolder.exists()) return

        actions.clear()

        actionsFolder.walkTopDown()
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: return@forEach
                    val type = section.getString("type")?.uppercase() ?: return@forEach

                    val action = when (type) {
                        "COMMAND" -> {
                            val command = section.getString("command") ?: ""
                            CommandAction().apply {
                                Command = command
                            }
                        }

                        "ITEM" -> {
                            val itemName = section.getString("item") ?: "DIRT"
                            val amount = section.getInt("amount", 1)
                            val itemStack = ItemStack(Material.valueOf(itemName), amount)
                            ItemAction().apply {
                                Item = itemStack
                            }
                        }

                        "EXP" -> {
                            val amount = section.getInt("amount", 0)
                            val modeStr = section.getString("mode") ?: "GAIN"
                            val mode = try {
                                Mode.valueOf(modeStr.uppercase())
                            } catch (e: IllegalArgumentException) {
                                Mode.GAIN
                            }

                            ExpAction().apply {
                                Mode = mode
                                Amount = amount
                            }
                        }
                        "START_QUEST" -> {
                            val questName = section.getString("quest") ?: ""

                            StartQuestAction().apply {
                                Quest = questName
                            }
                        }

                        else -> return@forEach
                    }

                    actions[key] = action
                    PluginLogger.logInfo("Loaded action: $key")
                }
            }
    }

    private fun loadConditions() {
        conditions.clear()

        val conditionsFolder = File(plugin.dataFolder, "conditions")
        if (!conditionsFolder.exists()) return

        conditions.clear()

        conditionsFolder.walkTopDown()
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: continue
                    val type = section.getString("type")?.uppercase() ?: continue

                    val condition = when (type) {
                        "ADVANCEMENT" -> AdvancementCondition().apply {
                            Advancements = section.getStringList("advancements").map { "minecraft:$it" }.toMutableList()
                        }

                        "BIOME" -> BiomeCondition().apply {
                            Biomes = section.getStringList("biomes").map { Biome.valueOf(it.uppercase()) }.toMutableList()
                        }

                        "BLOCK" -> BlockCondition().apply {
                            Block = section.getStringList("blocks").map { MainUtils.parseMaterial(it) }.toMutableList()
                        }

                        "COORDINATES" -> CoordinatesCondition().apply {
                            val x = section.getDouble("x",0.0)
                            val y = section.getDouble("y",0.0)
                            val z = section.getDouble("z",0.0)
                            val world = Bukkit.getWorld(section.getString("world") ?: "world") ?: return@forEach
                            Location = mutableListOf(Location(world, x, y, z))
                        }

                        "EXP" -> ExpCondition().apply {
                            MinExp = section.getInt("min",0)
                            MaxExp = section.getInt("max",99999)
                        }

                        "HEALTH" -> HealthCondition().apply {
                            MinHealth = section.getDouble("min",0.0)
                            MaxHealth = section.getDouble("max",99999.0)
                        }

                        "HUNGER" -> HungerCondition().apply {
                            MinHunger = section.getInt("min",0)
                            MaxHunger = section.getInt("max",99999)
                        }

                        "ITEM" -> ItemCondition().apply {
                            val locStr = section.getString("location") ?: "ANY"
                            Location =
                                try {
                                    ItemLocation.valueOf(locStr.uppercase())
                                } catch (_: IllegalArgumentException) {
                                    ItemLocation.ANY
                                }

                            val items = section.getMapList("items")
                            val itemStacks = mutableListOf<ItemStack>()
                            var totalAmount = 0

                            for (itemMap in items) {
                                val matStr = itemMap["item"] as? String ?: continue
                                val mat = MainUtils.parseMaterial(matStr)
                                val amt = (itemMap["amount"] as? Int) ?: 1

                                itemStacks.add(ItemStack(mat, amt))
                                totalAmount += amt
                            }

                            ItemType = itemStacks
                        }

                        "PERMISSION" -> PermissionCondition().apply {
                            Permission = section.getStringList("permissions").toMutableList()
                        }

                        "TIME" -> TimeCondition().apply {
                            StartTime = section.getLong("start")
                            EndTime = section.getLong("end")
                        }

                        "WEATHER" -> WeatherCondition().apply {
                            Weathers = when (section.getString("weather")?.uppercase()) {
                                "RAIN" -> Weather.RAIN
                                "THUNDER" -> Weather.THUNDER
                                else -> Weather.ANY
                            }
                        }

                        "WORLD" -> WorldCondition().apply {
                            val worldNames = section.getStringList("worlds")
                            val worldList = worldNames.mapNotNull { Bukkit.getWorld(it) }
                            Worlds = worldList.toMutableList()
                        }

                        else -> null
                    }

                    if (condition != null) {
                        conditions[key] = condition
                        PluginLogger.logInfo("Loaded condition: $key")
                    }
                }
            }
    }

    private fun loadObjectives() {
        objectives.clear()

        val objectivesDir = File(plugin.dataFolder, "objectives")
        if (!objectivesDir.exists() || !objectivesDir.isDirectory) return

        objectivesDir.walkTopDown().filter { it.isFile && it.extension == "yml" }.forEach { file ->
            val config = YamlConfiguration.loadConfiguration(file)
            for (key in config.getKeys(false)) {
                val section = config.getConfigurationSection(key) ?: continue
                val type = section.getString("type")?.uppercase() ?: continue

                val objective: Objective = when (type) {
                    "COMMAND" -> CommandObjective().apply {
                        Command = section.getString("command") ?: ""
                    }

                    "CRAFT" -> CraftObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "ENCHANT" -> EnchantObjective().apply {
                        val itemList = section.getStringList("items")
                        val enchantList = section.getStringList("enchants")

                        Item = if (itemList.isNotEmpty()) itemList.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        Enchant = if (enchantList.isNotEmpty()) enchantList.mapNotNull {
                            val split = it.split(" ")
                            MainUtils.getEnchantmentFromString(split[0])
                        } else null

                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "EXP" -> ExpObjective().apply {
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "GOTO" -> GotoObjective().apply {
                        val world = Bukkit.getWorld(section.getString("world") ?: "world")
                        val x = section.getDouble("x")
                        val y = section.getDouble("y")
                        val z = section.getDouble("z")
                        Goto = Location(world, x, y, z)
                    }

                    "INTERACT" -> InteractObjective().apply {
                        val targets = section.getStringList("targets")
                        Block = if (targets.isNotEmpty()) targets.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "KILL" -> KillObjective().apply {
                        val targets = section.getStringList("targets")
                        Target = if (targets.isNotEmpty()) targets.mapNotNull {
                            runCatching { EntityType.valueOf(it.uppercase()) }.getOrNull()
                        } else null

                        val items = section.getStringList("allowed_items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null

                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "LOOT" -> LootObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "MINE" -> MineObjective().apply {
                        val blocks = section.getStringList("blocks")
                        Block = if (blocks.isNotEmpty()) blocks.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "PICK" -> PickObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "PLACE" -> PlaceObjective().apply {
                        val blocks = section.getStringList("blocks")
                        Block = if (blocks.isNotEmpty()) blocks.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "TRADE" -> TradeObjective().apply {
                        ProgressGoal = section.getInt("amount", 1)
                    }

                    "TRAVEL" -> TravelObjective().apply {
                        ProgressGoal = section.getInt("distance", 1)
                        Mode = try {
                            TravelMode.valueOf(section.getString("mode")!!)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    else -> continue
                }

                if (section.contains("conditions")) {
                    val conditionList = section.getStringList("conditions")
                    objective.Conditions = conditionList.mapNotNull { conditions[it] }
                }

                objectives[key] = objective
            }
        }
    }

    private fun loadOptions() {
        options.clear()

        val optionsFolder = File(plugin.dataFolder, "options")
        if (!optionsFolder.exists()) return

        optionsFolder.walkTopDown()
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)

                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: continue

                    val repeatable = section.getInt("repeatable", -1)
                    val cancelable = section.getBoolean("cancelable", true)

                    val timeSection = section.getConfigurationSection("time_limit")
                    val timeLimit = timeSection?.let {
                        val days = it.getInt("days", 0)
                        val hours = it.getInt("hours", 0)
                        val minutes = it.getInt("minutes", 0)
                        val seconds = it.getInt("seconds", 0)
                        Duration.ofDays(days.toLong())
                            .plusHours(hours.toLong())
                            .plusMinutes(minutes.toLong())
                            .plusSeconds(seconds.toLong())
                    }

                    val dateSection = section.getConfigurationSection("date_limit")
                    val startDate = dateSection?.getString("from")?.let {
                        try {
                            Instant.parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val endDate = dateSection?.getString("until")?.let {
                        try {
                            Instant.parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val option = Options(
                        Repeatable = repeatable,
                        Cancelable = cancelable,
                        TimeLimit = timeLimit,
                        StartDate = startDate,
                        EndDate = endDate
                    )

                    options[key] = option
                    PluginLogger.logInfo("Loaded option: $key")
                }
            }
    }


    fun getAction(name: String): Action {
        return actions[name]
            ?: error("Action '$name' not found")
    }

    fun getCondition(name: String): Condition {
        return conditions[name]
            ?: error("Condition '$name' not found")
    }

    fun getObjective(name: String): Objective {
        return objectives[name]?.deepCopy()
            ?: error("Objective '$name' not found")
    }

    fun getOption(name: String) : Options {
        return options[name]
            ?: error("Option '$name' not found")
    }

    fun getActivePlayerQuests(id: UUID) : MutableMap<String, PlayerQuestData> {
        return activePlayersQuests.getOrPut(id) { mutableMapOf() }
    }

}