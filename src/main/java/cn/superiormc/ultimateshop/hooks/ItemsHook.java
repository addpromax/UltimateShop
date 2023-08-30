package cn.superiormc.ultimateshop.hooks;

import cn.superiormc.ultimateshop.UltimateShop;
import cn.superiormc.ultimateshop.managers.ErrorManager;
import com.willfp.eco.core.items.Items;
import com.willfp.ecoarmor.sets.ArmorSet;
import com.willfp.ecoarmor.sets.ArmorSets;
import com.willfp.ecoarmor.sets.ArmorSlot;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;

public class ItemsHook {

    public static ItemStack getHookItem(String pluginName, String itemID) {
        if (UltimateShop.freeVersion) {
            ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: You are using free version, " +
                    "hook item can not be used in this version!");
            return null;
        }
        if (!UltimateShop.instance.getServer().getPluginManager().isPluginEnabled(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Your server don't have " + pluginName +
                    " plugin, but your UI config try use its hook!");
            return null;
        }
        switch (pluginName) {
            case "ItemsAdder":
                if (CustomStack.getInstance(itemID) == null) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                            + pluginName + " item: " + itemID + "!");
                    return null;
                } else {
                    CustomStack customStack = CustomStack.getInstance(itemID);
                    return customStack.getItemStack();
                }
            case "Oraxen":
                if (OraxenItems.getItemById(itemID) == null) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                            + pluginName + " item: " + itemID + "!");
                    return null;
                } else {
                    ItemBuilder builder = OraxenItems.getItemById(itemID);
                    return builder.build();
                }
            case "MMOItems":
                if (MMOItems.plugin.getTypes().get(itemID.split(";;")[0]) == null) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                            + pluginName + " item: " + itemID + "!");
                    return null;
                } else if (MMOItems.plugin.getItem(itemID.split(";;")[0], itemID.split(";;")[1]) == null) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                            + pluginName + " item: " + itemID + "!");
                    return null;
                }
                return MMOItems.plugin.getItem(itemID.split(";;")[0], itemID.split(";;")[1]);
            case "EcoItems":
                EcoItems ecoItems = EcoItems.INSTANCE;
                if (ecoItems.getByID(itemID) == null) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                            + pluginName + " item: " + itemID + "!");
                    return null;
                } else {
                    EcoItem ecoItem = ecoItems.getByID(itemID);
                    return ecoItem.getItemStack();
                }
            case "eco":
                return Items.lookup(itemID).getItem();
            case "EcoArmor":
                if (ArmorSets.getByID(itemID.split(";;")[0]) == null) {
                    return null;
                }
                ArmorSet armorSet = ArmorSets.getByID(itemID);
                ItemStack itemStack = null;
                switch (itemID.split(";;")[1].toUpperCase()) {
                    case "BOOTS":
                        itemStack = armorSet.getItemStack(ArmorSlot.BOOTS);
                        break;
                    case "CHESTPLATE":
                        itemStack = armorSet.getItemStack(ArmorSlot.CHESTPLATE);
                        break;
                    case "ELYTRA":
                        itemStack = armorSet.getItemStack(ArmorSlot.ELYTRA);
                        break;
                    case "HELMET":
                        itemStack = armorSet.getItemStack(ArmorSlot.HELMET);
                        break;
                    case "LEGGINGS":
                        itemStack = armorSet.getItemStack(ArmorSlot.LEGGINGS);
                        break;
                }
                if (itemStack == null) {
                    ErrorManager.errorManager.sendErrorMessage
                            ("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                                    + pluginName + " item: " + itemID + "!");
                    return null;
                } else {
                    return itemStack;
                }
            case "MythicMobs":
                try {
                    if (MythicBukkit.inst().getItemManager().getItemStack(itemID) == null) {
                        ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                                + pluginName + " item: " + itemID + "!");
                        return null;
                    } else {
                        return MythicBukkit.inst().getItemManager().getItemStack(itemID);
                    }
                } catch (NoClassDefFoundError ep) {
                    if (MythicMobs.inst().getItemManager().getItemStack(itemID) == null) {
                        ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get "
                                + pluginName + " v4 item: " + itemID + "!");
                        return null;
                    } else {
                        return MythicMobs.inst().getItemManager().getItemStack(itemID);
                    }
                }
        }
        ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: You set hook plugin to "
                + pluginName + " in UI config, however for now FlipCard is not support it!");
        return null;
    }

}