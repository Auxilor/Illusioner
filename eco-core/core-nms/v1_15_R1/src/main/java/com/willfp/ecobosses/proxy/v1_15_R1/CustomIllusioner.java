package com.willfp.ecobosses.proxy.v1_15_R1;

import com.willfp.ecobosses.proxy.proxies.CustomIllusionerProxy;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityIllagerIllusioner;
import net.minecraft.server.v1_15_R1.EntityIllagerWizard;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityIronGolem;
import net.minecraft.server.v1_15_R1.EntityRaider;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityVillagerAbstract;
import net.minecraft.server.v1_15_R1.PathfinderGoalBowShoot;
import net.minecraft.server.v1_15_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_15_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_15_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_15_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStroll;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Illusioner;
import org.jetbrains.annotations.NotNull;

public class CustomIllusioner extends EntityIllagerIllusioner implements CustomIllusionerProxy {
    /**
     * Instantiate a new custom illusioner entity.
     *
     * @param location The location to spawn it at.
     */
    public CustomIllusioner(@NotNull final Location location) {
        super(EntityTypes.ILLUSIONER, ((CraftWorld) location.getWorld()).getHandle());

        this.setPosition(location.getX(), location.getY(), location.getZ());

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new EntityIllagerWizard.b());
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(2, new PathfinderGoalBowShoot<>(this, 1.0D, 20, 15.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(6, new PathfinderGoalBowShoot<>(this, 0.5D, 20, 15.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(1, (new PathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).a(new Class[0]));
        this.targetSelector.a(2, (new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)).a(300));
        this.targetSelector.a(3, (new PathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false)).a(300));
        this.targetSelector.a(3, (new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, false)).a(300));
    }

    /**
     * Spawn illusioner.
     *
     * @param location The location.
     * @return The illusioner.
     */
    public static Illusioner spawn(@NotNull final Location location) {
        CustomIllusioner illusioner = new CustomIllusioner(location);
        ((CraftWorld) location.getWorld()).getHandle().addEntity(illusioner);
        return (Illusioner) illusioner.getBukkitEntity();
    }
}
