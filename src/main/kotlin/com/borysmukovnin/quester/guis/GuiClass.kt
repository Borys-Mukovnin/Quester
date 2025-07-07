package com.borysmukovnin.quester.guis

import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class GuiClass(val player: Player) : InventoryHolder {
    protected var _inventory: Inventory = Bukkit.createInventory(this, 54, Component.text(""))

    var page: Int = 1

    protected abstract fun build(page: Int = 1)

    fun open(page: Int = 1) {
        build(page)
        fillInventory()
        player.openInventory(_inventory)
    }

    fun fillInventory() {
        val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE)

        val meta = glassPane.itemMeta
        if (meta != null) {
            meta.displayName(Component.text(""))

            val loreComponents = "".map {
                Component.text(it)
            }
            meta.lore(loreComponents)

            glassPane.itemMeta = meta
        }

        val fillList = listOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            17, 18,
            26, 27,
            35, 36,
            44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54
        )

        for (slot in fillList) {
            if (slot in 0 until _inventory.size) {
                _inventory.setItem(slot, glassPane.clone())
            }
        }

        val nextPageItem = ItemStack(Material.LIME_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName("<green>Next Page</green>".asFormattedComponent())
                lore(listOf(
                    "<green>Click to go to the next page.</green>".asFormattedComponent(),
                    "<green>Page ${page + 1}</green>".asFormattedComponent()
                ))
            }
        }

        val prevPageItem = ItemStack(Material.PURPLE_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName("<green>Previous Page</green>".asFormattedComponent())
                lore(listOf(
                    "<green>Click to go to the previous page.</green>".asFormattedComponent(),
                    "<green>Page ${page-1}</green>".asFormattedComponent()
                ))
            }
        }

        _inventory.setItem(47,prevPageItem)
        _inventory.setItem(51,nextPageItem)

    }

    fun getEmptySlots(): List<Int> {
        return _inventory.contents
            .mapIndexedNotNull { index, item ->
                if (item == null || item.type == Material.AIR) index else null
            }
    }

    override fun getInventory(): Inventory = _inventory
}
