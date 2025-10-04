/*    */ package me.tsctutorial.object;
/*    */ import java.util.List;
/*    */ import java.util.UUID;
/*    */ import net.citizensnpcs.api.trait.Trait;
/*    */ import org.bukkit.entity.LivingEntity;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ 
/*    */ public class CombatNPCTrait extends Trait {
/*    */   private final float xp;
/*    */   private final UUID uuid;
/*    */   
/*    */   public float getXp() {
/* 13 */     return this.xp;
/*    */   } private final List<ItemStack> items; private final double health; private final String name; public UUID getUuid() {
/* 15 */     return this.uuid;
/*    */   } public List<ItemStack> getItems() {
/* 17 */     return this.items;
/*    */   } public double getHealth() {
/* 19 */     return this.health;
/*    */   }
/*    */   private boolean indefinite = false;
/* 22 */   public boolean isIndefinite() { return this.indefinite; } public void setIndefinite(boolean indefinite) {
/* 23 */     this.indefinite = indefinite;
/*    */   }
/*    */   
/*    */   public CombatNPCTrait(String name, float xp, UUID uuid, List<ItemStack> items, double health) {
/* 27 */     super("anticombatlog");
/* 28 */     this.xp = xp;
/* 29 */     this.uuid = uuid;
/* 30 */     this.items = items;
/* 31 */     this.health = health;
/* 32 */     this.name = name;
/*    */   }
/*    */ 
/*    */   
/*    */   public void onSpawn() {
/* 37 */     super.onSpawn();
/* 38 */     getNPC().setProtected(false);
///* 39 */     getNPC().getEntity().setInvulnerable(false);
/* 40 */     LivingEntity le = (LivingEntity)getNPC().getEntity();
/* 41 */     le.setHealth(Math.max(Math.min(this.health, 20.0D), 0.0D));
/*    */     
///* 43 */     le.setInvulnerable(false);
/* 44 */     le.setNoDamageTicks(0);
/* 45 */     le.setMaximumNoDamageTicks(0);
/*    */   }
/*    */   
/*    */   public String getRawName() {
/* 49 */     return this.name;
/*    */   }
/*    */ 
/*    */   
/*    */   public void onDespawn() {
/* 54 */     super.onDespawn();
/*    */   }
/*    */ }


/* Location:              D:\Desktop\AntiCombatLog-2.6.0.jar!\net\badbird5907\anticombatlog\object\CombatNPCTrait.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */