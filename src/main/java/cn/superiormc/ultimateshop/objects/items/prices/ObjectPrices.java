package cn.superiormc.ultimateshop.objects.items.prices;

import cn.superiormc.ultimateshop.managers.ErrorManager;
import cn.superiormc.ultimateshop.objects.items.AbstractThings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ObjectPrices extends AbstractThings {

    public List<ObjectSinglePrice> singlePrices = new ArrayList<>();

    public ObjectPrices() {
        super();
        empty = true;
    }

    public ObjectPrices(ConfigurationSection section, String mode) {
        super(section, mode);
        initSinglePrices();
        empty = false;
    }

    public void initSinglePrices() {
        for (String s : section.getKeys(false)) {
            if (section.getConfigurationSection(s) == null) {
                ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get prices section in your shop config!!");
                singlePrices.add(new ObjectSinglePrice());
            }
            else {
                singlePrices.add(new ObjectSinglePrice(section.getConfigurationSection(s)));
            }
        }
    }

    public ObjectSinglePrice getAnyTargetPrice(Player player, int times, boolean takeOrGive, boolean buyOrSell) {
        List<ObjectSinglePrice> maybeResult = new ArrayList<>();
        for (ObjectSinglePrice tempVal1 : getPrices(times)) {
            if (tempVal1.getCondition(player)) {
                if (buyOrSell && tempVal1.checkHasEnough(player, takeOrGive, times)) {
                    return tempVal1;
                }
                else if (!buyOrSell) {
                    return tempVal1;
                }
                else {
                    maybeResult.add(tempVal1);
                }
            }
        }
        if (maybeResult.isEmpty()) {
            return new ObjectSinglePrice();
        }
        else {
            return maybeResult.get(0);
        }
    }

    private List<ObjectSinglePrice> getPrices(int times) {
        List<ObjectSinglePrice> applyThings = new ArrayList<>();
        for (ObjectSinglePrice tempVal1 : singlePrices) {
            if (tempVal1.getApplyCostMap().containsKey(times)) {
                applyThings.add(tempVal1);
            }
            else if (times >= tempVal1.getStartApply()) {
                applyThings.add(tempVal1);
            }
        }
        // 没有有效的价格
        if (applyThings.isEmpty()) {
            applyThings.add(new ObjectSinglePrice());
        }

        return applyThings;
    }

    @Override
    public void giveThing(Player player, int times) {
        if (section == null || singlePrices.isEmpty()) {
            return;
        }
        if (mode.equals("ANY")) {
            getAnyTargetPrice(player, times, true, false);
        } else if (mode.equals("ALL")) {
            for (ObjectSinglePrice tempVal2 : getPrices(times)) {
                tempVal2.playerGive(player, times);
            }
        } else {
            ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get price-mode section in your shop config!!");
        }
    }


    // 作为价格时候使用
    @Override
    public boolean takeThing(Player player, boolean take, int times) {
        switch (mode) {
            case "UNKNOWN":
                return false;
            case "ANY":
                if (section == null) {
                    return true;
                }
                if (getAnyTargetPrice(player, times, false, true)
                        .checkHasEnough(player, take, times)) {
                    if (take) {
                        getAnyTargetPrice(player, times, true, true);
                    }
                    return true;
                }
                return false;
            case "ALL":
                for (ObjectSinglePrice tempVal1 : getPrices(times)) {
                    if (!tempVal1.checkHasEnough(player, take, times)) {
                        return false;
                    }
                }
                return true;
            default:
                ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not get price-mode section in your shop config!!");
                return false;
        }
    }

    public List<String> getDisplayName(int times) {
        List<ObjectSinglePrice> prices = getPrices(times);
        List<String> tempVal1 = new ArrayList<>();
        for (ObjectSinglePrice tempVal2 : prices) {
            tempVal1.add(tempVal2.getDisplayName(times));
        }
        return tempVal1;
    }

    public String getDisplayNameWithOneLine(int times, String spliteSign) {
        List<String> tempVal1 = getDisplayName(times);
        StringBuilder tempVal2 = null;
        for (String tempVal3 : tempVal1) {
            if (tempVal2 == null) {
                tempVal2 = new StringBuilder(tempVal3);
            }
            else {
                tempVal2 = new StringBuilder(tempVal2 + spliteSign + tempVal3);
            }
        }
        return tempVal2.toString();
    }

}