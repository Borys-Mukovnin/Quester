package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.Models.ActiveQuest
import com.borysmukovnin.quester.Models.Objective
import com.borysmukovnin.quester.Models.Objectives.*
import com.borysmukovnin.quester.Models.QuestData
import com.borysmukovnin.quester.Models.StageSection
import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.utils.MainUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

class QuestManager(private val plugin: Quester) {

    private val activePlayersQuests: MutableMap<UUID,List<ActiveQuest>> = mutableMapOf()
    private val questList: MutableMap<String,QuestData> = mutableMapOf()
    private val questsFolder: File = File(plugin.dataFolder, "quests")

    fun loadActivePlayersQuests(playerUUID: UUID) {
        val progressFolder = File(plugin.dataFolder, "progress")

        val fileToSearch = playerUUID.toString()
        val file = progressFolder.listFiles()?.find { it.name.equals("${fileToSearch}.yml", ignoreCase = true) } ?: return
        val config = YamlConfiguration.loadConfiguration(file)

        val playerName = config.getString("player_name")

        val questsSection = config.getConfigurationSection("active_quests")
    }

    fun loadAllQuests() {
        questsFolder.listFiles()?.forEach { file ->
            questList[file.name] = CreateQuestData(YamlConfiguration.loadConfiguration(file))
        }

        questList.forEach({file ->

            file.value.Stages.forEach { stage ->
                stage.value.StageObjectives.forEach { obj ->
                    when (obj) {
                        is KillObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Target} | ${obj.TargetAmount} | ${obj.Item}")
                        }
                        is ExpObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Mode} | ${obj.Amount}")
                        }
                        is CraftObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Item} | ${obj.Amount}")
                        }
                        is PlaceObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Block} | ${obj.Amount}")
                        }
                        is InteractObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Block} | ${obj.Amount}")
                        }
                        is MineObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Block} | ${obj.Amount}")
                        }
                        is UseObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Block} | ${obj.Amount}")
                        }
                        is TradeObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Amount} | ${obj.Amount}")
                        }
                        is EnchantObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Enchant} | ${obj.Item} | ${obj.Amount}")
                        }
                        is PickObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Item} | ${obj.Amount}")
                        }
                        is LootObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Item} | ${obj.Amount}")
                        }
                        is GotoObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Goto}")
                        }
                        is TravelObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Amount}")
                        }
                        is CommandObjective -> {
                            plugin.logger.info("${file.key}-${stage.key} | ${obj.Command}")
                        }
                    }
                }
            }
        })
    }

    private fun CreateQuestData(config: YamlConfiguration): QuestData {
        val stagesList: MutableMap<String,StageSection> = mutableMapOf()

        val questName = config.getString("name") ?: "unknown name"

        val questDesc = config.getStringList("description")

        val startConditions = config.getStringList("start_conditions")

        val stagesSection = config.getConfigurationSection("stages")
        val stageNames = stagesSection!!.getKeys(false)

        for (stageName in stageNames) {
            val stageConfig = stagesSection.getConfigurationSection(stageName)

            val stageSubName = stageConfig!!.getString("name") ?: "unknown name"
            val stageDescription = stageConfig.getStringList("description")
            val stageObjectives = ParseObjective(stageConfig.getStringList("objectives"))
            val stageRewards = stageConfig.getStringList("rewards")

            stagesList[stageName] = StageSection(stageSubName,stageDescription,stageObjectives,stageRewards)
        }

        return QuestData(questName,questDesc,startConditions,stagesList)
    }

    private fun ParseObjective(objectivesList: MutableList<String>) : MutableList<Objective> {
        val objList: MutableList<Objective> = emptyList<Objective>().toMutableList()

        objectivesList.forEach { o ->
            val objectiveSplit = o.split(" ")
            var objective: Objective? = null

            when (objectiveSplit[0].uppercase()) {
                "KILL" -> {
                    objective = KillObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val targetList: MutableList<EntityType> = mutableListOf()
                            val targetString: List<String> = objectiveSplit[1].split(",")
                            targetString.forEach { e ->
                                targetList.add(EntityType.valueOf(e.uppercase()))
                            }
                            Target = targetList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            TargetAmount = objectiveSplit.getOrNull(2)!!.toIntOrNull()!!
                        }
                        if (objectiveSplit.getOrNull(3) != null && objectiveSplit.getOrNull(3) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[3].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(Material.valueOf(i.uppercase())))
                            }
                            Item = itemList
                        }
                    }
                }
                "EXP" -> {
                    objective = ExpObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null) {
                            if (enumValues<ExpMode>().any { it.name == objectiveSplit[1].uppercase() }) {
                                Mode = ExpMode.valueOf(objectiveSplit[1].uppercase())
                            }
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "CRAFT" -> {
                    objective = CraftObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(Material.valueOf(i.uppercase())))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "PLACE" -> {
                    objective = PlaceObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(Material.valueOf(b.uppercase()))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "INTERACT" -> {
                    objective = InteractObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(Material.valueOf(b.uppercase()))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "MINE" -> {
                    objective = MineObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(Material.valueOf(b.uppercase()))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "USE" -> {
                    objective = UseObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(Material.valueOf(b.uppercase()))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "TRADE" -> {
                    objective = TradeObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(1)!!.toInt()
                        }
                    }
                }
                "ENCHANT" -> {
                    objective = EnchantObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val enchantList: MutableList<Enchantment> = mutableListOf()
                            val enchantString: List<String> = objectiveSplit[1].split(",")
                            enchantString.forEach { e ->
                                enchantList.add(MainUtils.getEnchantmentFromString(e)!!)
                            }
                            Enchant = enchantList
                        }
                        if (objectiveSplit.getOrNull(2) != null && objectiveSplit.getOrNull(2) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[2].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(Material.valueOf(i.uppercase())))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(3)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(3)!!.toInt()
                        }
                    }
                }
                "PICK" -> {
                    objective = PickObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(Material.valueOf(i.uppercase())))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "LOOT" -> {
                    objective = LootObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(Material.valueOf(i.uppercase())))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "GOTO" -> {
                    objective = GotoObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(2)?.toDoubleOrNull() != null && objectiveSplit.getOrNull(3)?.toDoubleOrNull() != null && objectiveSplit.getOrNull(4) != null) {
                            Goto = Location(Bukkit.getWorld(objectiveSplit[4]),objectiveSplit[1].toDouble(),objectiveSplit[2].toDouble(),objectiveSplit[3].toDouble())
                        }
                    }
                }
                "TRAVEL" -> {
                    objective = TravelObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            Amount = objectiveSplit[1].toInt()
                        }
                    }
                }
                "COMMAND" -> {
                    objective = CommandObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null) {
                            Command = objectiveSplit.drop(1).joinToString(" ")
                        }
                    }
                }
                else -> {
                    objective = KillObjective()
                }
            }

            objList.add(objective)
        }

        return objList
    }
}