 package me.tsctutorial.npclib;
 
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
 
 
 
public class NPCEntity extends EntityPlayer
 {
    public NPCEntity(MinecraftServer server, World world, GameProfile g, PlayerInteractManager itemInWorldManager) {
        super(world.getServer().getServer(), (WorldServer)world, g, itemInWorldManager);
   }
   public void setBukkitEntity(Entity entity) {
     this.bukkitEntity = (CraftEntity)entity;
   }
 }