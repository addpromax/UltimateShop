package cn.superiormc.ultimateshop.objects.items;

import cn.superiormc.ultimateshop.hooks.EconomyHook;
import cn.superiormc.ultimateshop.hooks.PriceHook;
import cn.superiormc.ultimateshop.managers.ConfigManager;
import cn.superiormc.ultimateshop.methods.Items.BuildItem;
import cn.superiormc.ultimateshop.objects.items.prices.ObjectPrices;
import cn.superiormc.ultimateshop.objects.items.prices.ObjectSinglePrice;
import cn.superiormc.ultimateshop.objects.items.prices.PriceMode;
import cn.superiormc.ultimateshop.objects.items.products.ObjectSingleProduct;
import cn.superiormc.ultimateshop.objects.items.subobjects.ObjectDisplayPlaceholder;
import cn.superiormc.ultimateshop.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

public abstract class AbstractSingleThing implements Comparable<AbstractSingleThing> {

    public ThingType type;

    public ConfigurationSection singleSection;

    public ObjectCondition condition;

    public boolean empty;

    private final ObjectDisplayPlaceholder displayPlaceholder;

    private String id;

    public AbstractThings things;

    public AbstractSingleThing() {
        initType(null);
        this.empty = true;
        this.displayPlaceholder = new ObjectDisplayPlaceholder(this);
    }

    public AbstractSingleThing(String id, AbstractThings things) {
        this.id = id;
        this.singleSection = things.section.getConfigurationSection(id);
        if (singleSection != null && singleSection.contains("custom-type")) {
            initType(ConfigManager.configManager.config.getConfigurationSection("prices." +
                    singleSection.getString("custom-type")));
        }
        else {
            initType(singleSection);
        }
        this.empty = false;
        this.displayPlaceholder = new ObjectDisplayPlaceholder(this);
    }

    private void initType(ConfigurationSection section) {
        if (section == null) {
            type = ThingType.UNKNOWN;
        } else if (section.contains("hook-plugin") && section.contains("hook-item")) {
            type = ThingType.HOOK_ITEM;
        } else if (section.contains("match-item") && CommonUtil.checkPluginLoad("MythicChanger")) {
            type = ThingType.MATCH_ITEM;
        } else if (section.contains("material")) {
            type = ThingType.VANILLA_ITEM;
        } else if (section.contains("economy-plugin")) {
            type = ThingType.HOOK_ECONOMY;
        } else if (section.contains("economy-type") && !section.contains("economy-plugin")) {
            type = ThingType.VANILLA_ECONOMY;
        }  else {
            type = ThingType.FREE;
        }
    }

    protected void initCondition() {
        List<String> conditions = null;
        if (this instanceof ObjectSinglePrice) {
            ObjectPrices objectPrices = (ObjectPrices) this.things;
            if (objectPrices.getPriceMode() == PriceMode.BUY) {
                conditions = things.section.getStringList("buy-prices-conditions." + id);
            } else {
                conditions = things.section.getStringList("sell-prices-conditions." + id);
            }
        } else if (this instanceof ObjectSingleProduct) {
            conditions = things.section.getStringList("products-conditions." + id);
        }
        if (conditions == null || conditions.isEmpty()) {
            conditions = singleSection.getStringList("conditions");
        }
        if (conditions.isEmpty()) {
            condition = new ObjectCondition();
        }
        else {
            condition = new ObjectCondition(conditions);
        }
    }

    public void playerGive(Player player,
                           double cost) {
        if (singleSection == null) {
            return;
        }
        switch (type) {
            case VANILLA_ITEM:  case HOOK_ITEM: case MATCH_ITEM:
                if (getItemThing(singleSection,
                        player,
                        true,
                        cost) == null) {
                    return;
                }
                return;
            case HOOK_ECONOMY :
                EconomyHook.giveEconomy(singleSection.getString("economy-plugin"),
                        singleSection.getString("economy-type", "Unknown"),
                        player,
                        cost);

                return;
            case VANILLA_ECONOMY:
                EconomyHook.giveEconomy(singleSection.getString("economy-type"),
                        player,
                        (int) cost);
                return;
        }
    }

    public boolean getCondition(Player player) {
        if (condition == null) {
            return true;
        }
        return condition.getBoolean(player);
    }

    public double playerHasAmount(Inventory inventory, Player player) {
        return playerHasAmount(inventory, singleSection, player);
    }

    public double playerHasAmount(Inventory inventory, ConfigurationSection section, Player player) {
        if (section == null) {
            return 0;
        }
        switch (type) {
            case HOOK_ITEM:
                String pluginName = section.getString("hook-plugin", "");
                String itemID = section.getString("hook-item", "");
                if (pluginName.equals("MMOItems") && !itemID.contains(";;")) {
                    itemID = section.getString("hook-item-type") + ";;" + itemID;
                } else if (pluginName.equals("EcoArmor") && !itemID.contains(";;")) {
                    itemID = itemID + ";;" + section.getString("hook-item-type");
                }
                return PriceHook.getItemAmount(inventory,
                        pluginName,
                        itemID);
            case VANILLA_ITEM:
                ItemStack tempVal1 = getItemThing(section, player, false, 1);
                if (tempVal1 == null) {
                    return 0;
                }
                tempVal1.setAmount(1);
                return PriceHook.getItemAmount(inventory, tempVal1);
            case MATCH_ITEM:
                return PriceHook.getItemAmount(inventory, section);
            case HOOK_ECONOMY:
                return PriceHook.getEconomyAmount(player, section.getString("economy-plugin"),
                        section.getString("economy-type", "default"));
            case VANILLA_ECONOMY:
                return PriceHook.getEconomyAmount(player,
                        section.getString("economy-type"));
            case UNKNOWN:
                Bukkit.getConsoleSender().sendMessage("§x§9§8§F§B§9§8[UltimateShop] §c" +
                        "There is something wrong in your shop configs!");
                return 0;
        }
        return 0;
    }

    public boolean playerHasEnough(Inventory inventory,
                                   Player player,
                                   boolean take,
                                   double cost) {
        return playerHasEnough(inventory,
                singleSection,
                player,
                take,
                cost);
    }

    public boolean playerHasEnough(Inventory inventory,
                                   ConfigurationSection section,
                                   Player player,
                                   boolean take,
                                   double cost) {
        if (section == null) {
            return false;
        }

        if (cost < 0) {
            return false;
        }
        switch (type) {
            case HOOK_ITEM:
                String pluginName = section.getString("hook-plugin", "");
                String itemID = section.getString("hook-item", "");
                if (pluginName.equals("MMOItems") && !itemID.contains(";;")) {
                    itemID = section.getString("hook-item-type") + ";;" + itemID;
                } else if (pluginName.equals("EcoArmor") && !itemID.contains(";;")) {
                    itemID = itemID + ";;" + section.getString("hook-item-type");
                }
                return PriceHook.getPrice(inventory,
                        player,
                        pluginName,
                        itemID,
                        (int) cost, take);
            case VANILLA_ITEM:
                ItemStack itemStack = getItemThing(section, player, false, 1);
                if (itemStack == null) {
                    return false;
                }
                itemStack.setAmount(1);
                return PriceHook.getPrice(inventory, player, itemStack, (int) cost, take);
            case MATCH_ITEM:
                return PriceHook.getPrice(inventory, player, section, (int) cost, take);
            case HOOK_ECONOMY:
                return PriceHook.getPrice(player,
                        section.getString("economy-plugin"),
                        section.getString("economy-type", "default"),
                        cost, take);
            case VANILLA_ECONOMY:
                return PriceHook.getPrice(player,
                        section.getString("economy-type"),
                        (int) cost, take);
            case UNKNOWN:
                Bukkit.getConsoleSender().sendMessage("§x§9§8§F§B§9§8[UltimateShop] §c" +
                        "There is something wrong in your shop configs!");
                return false;
        }
        return false;
    }

    public ItemStack getItemThing(ConfigurationSection section,
                                  Player player,
                                  boolean give,
                                  double cost) {
        if (section == null) {
            if (singleSection == null) {
                return null;
            }
            section = singleSection;
        }
        ItemStack itemStack;
        itemStack = BuildItem.buildItemStack(player, section, (int) cost);
        if (itemStack == null) {
            return null;
        }
        if (give) {
            CommonUtil.giveOrDrop(player, itemStack);
        }
        return itemStack;
    }

    public abstract String getDisplayName(BigDecimal amount, boolean alwaysStatic);

    public String getId() {
        return id;
    }

    public ObjectDisplayPlaceholder getDisplayPlaceholder() {
        return displayPlaceholder;
    }

    @Override
    public int compareTo(@NotNull AbstractSingleThing otherThing) {
        int len1 = getId().length();
        int len2 = otherThing.getId().length();
        int minLength = Math.min(len1, len2);

        for (int i = 0; i < minLength; i++) {
            char c1 = getId().charAt(i);
            char c2 = otherThing.getId().charAt(i);

            if (c1 != c2) {
                if (Character.isDigit(c1) && Character.isDigit(c2)) {
                    // 如果字符都是数字，则按照数字大小进行比较
                    return Integer.compare(Integer.parseInt(getId().substring(i)), Integer.parseInt(otherThing.getId().substring(i)));
                } else {
                    // 否则，按照字符的unicode值进行比较
                    return c1 - c2;
                }
            }
        }

        return len1 - len2;
    }

}
