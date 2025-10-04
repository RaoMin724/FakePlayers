/*    */ package me.tsctutorial.utils;
/*    */ 
/*    */ import java.util.Map;
/*    */ import org.bukkit.enchantments.Enchantment;
/*    */ import org.bukkit.inventory.ItemStack;
///*    */ import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
///*    */ import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
/*    */
        public class ExcellentEnchantUtil {
            public static boolean shouldKeep(ItemStack item) {
                Map<Enchantment, Integer> enchantments = item.getEnchantments();
                if (enchantments.isEmpty()) {
                    return false;
                }

                // 检查物品的附魔是否包含指定的附魔（例如：Soulbound）
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    if (enchantment.getName().equalsIgnoreCase("Soulbound")) {
                        // 根据您的需求定制额外的检查条件
                        // 例如，您可以检查是否物品的附魔等级大于等于0
                        return entry.getValue() >= 0;
                    }
                }

                return false;
            }
        }


/* Location:              D:\Desktop\AntiCombatLog-2.6.0.jar!\net\badbird5907\anticombatlog\hooks\impl\ExcellentEnchantUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */