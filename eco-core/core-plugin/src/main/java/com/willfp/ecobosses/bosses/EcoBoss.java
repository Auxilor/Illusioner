package com.willfp.ecobosses.bosses;

import com.willfp.eco.internal.config.AbstractUndefinedConfig;
import com.willfp.eco.util.NumberUtils;
import com.willfp.eco.util.StringUtils;
import com.willfp.eco.util.internal.PluginDependent;
import com.willfp.eco.util.plugin.AbstractEcoPlugin;
import com.willfp.eco.util.tuples.Pair;
import com.willfp.ecobosses.bosses.util.bosstype.BossEntityUtils;
import com.willfp.ecobosses.bosses.util.bosstype.BossType;
import com.willfp.ecobosses.bosses.util.obj.BossbarProperties;
import com.willfp.ecobosses.bosses.util.obj.ExperienceOptions;
import com.willfp.ecobosses.bosses.util.obj.ImmunityOptions;
import com.willfp.ecobosses.bosses.util.obj.OptionedSound;
import com.willfp.ecobosses.bosses.util.obj.SpawnTotem;
import com.willfp.ecobosses.bosses.util.obj.TeleportOptions;
import com.willfp.ecobosses.bosses.util.obj.attacks.EffectOption;
import com.willfp.ecobosses.bosses.util.obj.attacks.SummonsOption;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EcoBoss extends PluginDependent {
    /**
     * The name of the boss.
     */
    @Getter
    private final String name;

    /**
     * The config of the set.
     */
    @Getter(AccessLevel.PRIVATE)
    private final AbstractUndefinedConfig config;

    /**
     * The display name of the boss.
     */
    @Getter
    private final String displayName;

    /**
     * The base entity spawner.
     */
    private final BossType bossType;

    /**
     * If the boss bar is enabled.
     */
    @Getter
    private final boolean bossbarEnabled;

    /**
     * The BossBar properties.
     */
    @Getter
    private final BossbarProperties bossbarProperties;

    /**
     * If spawn totem is enabled.
     */
    @Getter
    private final boolean spawnTotemEnabled;

    /**
     * The spawn totem.
     */
    @Getter
    private final SpawnTotem spawnTotem;

    /**
     * Disabled world names for spawn totem.
     */
    @Getter
    private final List<String> spawnTotemDisabledWorldNames;

    /**
     * The max health.
     */
    @Getter
    private final int maxHealth;

    /**
     * The attack damage.
     */
    @Getter
    private final int attackDamage;

    /**
     * The movement speed.
     */
    @Getter
    private final int movementSpeed;

    /**
     * The immunity options.
     */
    @Getter
    private final ImmunityOptions immunityOptions;

    /**
     * The drops.
     */
    @Getter
    private final Map<ItemStack, Double> drops;

    /**
     * The exp to drop.
     */
    @Getter
    private final ExperienceOptions experienceOptions;

    /**
     * The effects.
     */
    @Getter
    private final Set<EffectOption> effects;

    /**
     * The summons.
     */
    @Getter
    private final Set<SummonsOption> summons;

    /**
     * The shuffle chance.
     */
    @Getter
    private final double shuffleChance;

    /**
     * If attacks should be called on injury.
     */
    @Getter
    private final boolean attackOnInjure;

    /**
     * Sounds played on injure.
     */
    @Getter
    private final List<OptionedSound> injureSounds;

    /**
     * Spawn sounds.
     */
    @Getter
    private final List<OptionedSound> spawnSounds;

    /**
     * Death sounds.
     */
    @Getter
    private final List<OptionedSound> deathSounds;

    /**
     * Summon sounds.
     */
    @Getter
    private final List<OptionedSound> summonSounds;

    /**
     * If the boss can teleport.
     */
    @Getter
    private final boolean teleportationEnabled;

    /**
     * Teleport options.
     */
    @Getter
    private final TeleportOptions teleportOptions;

    /**
     * Spawn messages.
     */
    @Getter
    private final List<String> spawnMessages;

    /**
     * Death messages.
     */
    @Getter
    private final List<String> deathMessages;

    /**
     * Nearby players radius.
     */
    @Getter
    private final double nearbyRadius;

    /**
     * Nearby players commands.
     */
    @Getter
    private final Map<String, Double> nearbyPlayersCommands;

    /**
     * Top damager commands.
     */
    @Getter
    private final Map<Integer, List<Pair<Double, String>>> topDamagerCommands;

    /**
     * Incoming damage multipliers.
     */
    @Getter
    private final Map<EntityDamageEvent.DamageCause, Double> incomingMultipliers;

    /**
     * Create a new Boss.
     *
     * @param name   The name of the set.
     * @param config The set's config.
     * @param plugin Instance of EcoBosses.
     */
    public EcoBoss(@NotNull final String name,
                   @NotNull final AbstractUndefinedConfig config,
                   @NotNull final AbstractEcoPlugin plugin) {
        super(plugin);
        this.config = config;
        this.name = name;

        this.displayName = this.getConfig().getString("name");

        // Boss Type
        this.bossType = BossEntityUtils.getBossType(this.getConfig().getString("base-mob"));

        // Boss Bar
        this.bossbarEnabled = this.getConfig().getBool("bossbar.enabled");
        this.bossbarProperties = new BossbarProperties(
                BarColor.valueOf(this.getConfig().getString("bossbar.color").toUpperCase()),
                BarStyle.valueOf(this.getConfig().getString("bossbar.style").toUpperCase())
        );

        // Attributes
        this.attackDamage = this.getConfig().getInt("attack-damage");
        this.maxHealth = this.getConfig().getInt("max-health");
        this.movementSpeed = this.getConfig().getInt("movement-speed");

        // Spawn Totem
        this.spawnTotemEnabled = this.getConfig().getBool("spawn-totem.enabled");
        this.spawnTotem = new SpawnTotem(
                Material.getMaterial(this.getConfig().getString("spawn-totem.bottom").toUpperCase()),
                Material.getMaterial(this.getConfig().getString("spawn-totem.middle").toUpperCase()),
                Material.getMaterial(this.getConfig().getString("spawn-totem.top").toUpperCase())
        );
        this.spawnTotemDisabledWorldNames = this.getConfig().getStrings("spawn-totem.world-blacklist").stream().map(String::toLowerCase).collect(Collectors.toList());

        // Rewards
        this.drops = new HashMap<>();
        for (String string : this.getConfig().getStrings("rewards.drops")) {
            YamlConfiguration tempConfig = new YamlConfiguration();
            double chance = 100;
            if (string.contains("::")) {
                String[] split = string.split("::");
                chance = Double.parseDouble(split[0]);
                string = split[1];
            }
            String tempConfigString = new String(Base64.getDecoder().decode(string));
            try {
                tempConfig.loadFromString(tempConfigString);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ItemStack itemStack = tempConfig.getItemStack("drop-key");
            this.drops.put(itemStack, chance);
        }
        this.experienceOptions = new ExperienceOptions(
                this.getConfig().getInt("rewards.xp.minimum"),
                this.getConfig().getInt("rewards.xp.maximum")
        );

        // Immunities
        this.immunityOptions = new ImmunityOptions(
                this.getConfig().getBool("defence.immunities.fire"),
                this.getConfig().getBool("defence.immunities.suffocation"),
                this.getConfig().getBool("defence.immunities.drowning"),
                this.getConfig().getBool("defence.immunities.projectiles"),
                this.getConfig().getBool("defence.immunities.explosion")
        );

        // Multipliers
        this.incomingMultipliers = new HashMap<>();
        double melee = this.getConfig().getDouble("defence.incoming-multipliers.melee");
        this.incomingMultipliers.put(EntityDamageEvent.DamageCause.ENTITY_ATTACK, melee);
        this.incomingMultipliers.put(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, melee);

        double projectile = this.getConfig().getDouble("defence.incoming-multipliers.projectile");
        this.incomingMultipliers.put(EntityDamageEvent.DamageCause.PROJECTILE, projectile);

        // Effects
        this.effects = new HashSet<>();
        for (String string : this.getConfig().getStrings("attacks.potion-effects")) {
            String[] split = string.split(":");
            PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
            assert type != null;
            this.effects.add(new EffectOption(
                    Double.parseDouble(split[3]),
                    Integer.parseInt(split[1]) - 1,
                    Integer.parseInt(split[2]),
                    type
            ));
        }

        // Summons
        this.summons = new HashSet<>();
        for (String string : this.getConfig().getStrings("attacks.summons")) {
            String[] split = string.split(":");
            this.summons.add(new SummonsOption(
                    Double.parseDouble(split[1]),
                    EntityType.valueOf(split[0].toUpperCase())
            ));
        }

        // Shuffle
        this.shuffleChance = this.getConfig().getDouble("attacks.shuffle-chance");

        // Attack on injure
        this.attackOnInjure = this.getConfig().getBool("attacks.on-injure");

        // Sounds
        this.injureSounds = new ArrayList<>();
        for (String string : this.getConfig().getStrings("sounds.injure")) {
            String[] split = string.split(":");
            this.injureSounds.add(new OptionedSound(
                    Sound.valueOf(split[0].toUpperCase()),
                    Float.parseFloat(split[1]),
                    Float.parseFloat(split[2])
            ));
        }

        this.deathSounds = new ArrayList<>();
        for (String string : this.getConfig().getStrings("sounds.death")) {
            String[] split = string.split(":");
            this.deathSounds.add(new OptionedSound(
                    Sound.valueOf(split[0].toUpperCase()),
                    Float.parseFloat(split[1]),
                    Float.parseFloat(split[2])
            ));
        }

        this.summonSounds = new ArrayList<>();
        for (String string : this.getConfig().getStrings("sounds.summon")) {
            String[] split = string.split(":");
            this.summonSounds.add(new OptionedSound(
                    Sound.valueOf(split[0].toUpperCase()),
                    Float.parseFloat(split[1]),
                    Float.parseFloat(split[2])
            ));
        }

        this.spawnSounds = new ArrayList<>();
        for (String string : this.getConfig().getStrings("sounds.spawn")) {
            String[] split = string.split(":");
            this.spawnSounds.add(new OptionedSound(
                    Sound.valueOf(split[0].toUpperCase()),
                    Float.parseFloat(split[1]),
                    Float.parseFloat(split[2])
            ));
        }

        // Spawn egg
        /*
        Material eggMaterial = Material.matchMaterial(this.getConfig().getString("spawn-egg.egg-material").toUpperCase());
        assert eggMaterial != null;
        this.spawnEgg = new ItemStack(eggMaterial);
        SpawnEggMeta meta = (SpawnEggMeta) this.spawnEgg.getItemMeta();
        assert meta != null;
        List<String> lore = new ArrayList<>();
        for (String string : this.getConfig().getStrings("spawn-egg.lore")) {
            lore.add(StringUtils.translate(string));
        }
        meta.setLore(lore);
        meta.setDisplayName(this.getConfig().getString("spawn-egg.display-name"));
        meta.getPersistentDataContainer().set(this.getPlugin().getNamespacedKeyFactory().create("spawn_egg"), PersistentDataType.STRING, this.getName());
        this.spawnEgg.setItemMeta(meta);

         */

        // Messages
        this.spawnMessages = new ArrayList<>();
        for (String string : this.getConfig().getStrings("broadcast.spawn")) {
            this.spawnMessages.add(StringUtils.translate(string));
        }
        this.deathMessages = new ArrayList<>();
        for (String string : this.getConfig().getStrings("broadcast.death")) {
            this.deathMessages.add(StringUtils.translate(string));
        }

        // Teleportation
        this.teleportationEnabled = this.getConfig().getBool("defence.teleport.enabled");
        this.teleportOptions = new TeleportOptions(
                this.getConfig().getInt("defence.teleport.range"),
                this.getConfig().getDouble("defence.teleport.chance")
        );

        // Top Damager Commands
        this.topDamagerCommands = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            this.topDamagerCommands.put(i, new ArrayList<>());
            for (String string : this.getConfig().getStrings("rewards.top-damager-commands." + i)) {
                double chance = 100;
                if (string.contains("::")) {
                    String[] split = string.split("::");
                    chance = Double.parseDouble(split[0]);
                    string = split[1];
                }
                List<Pair<Double, String>> commands = this.topDamagerCommands.get(i) == null ? new ArrayList<>() : this.topDamagerCommands.get(i);
                commands.add(new Pair<>(chance, string));
                this.topDamagerCommands.put(i, commands);
            }
        }

        // Nearby Rewards
        this.nearbyRadius = this.getConfig().getDouble("rewards.nearby-player-commands.radius");
        this.nearbyPlayersCommands = new HashMap<>();
        for (String string : this.getConfig().getStrings("rewards.nearby-player-commands.commands")) {
            double chance = 100;
            if (string.contains("::")) {
                String[] split = string.split("::");
                chance = Double.parseDouble(split[0]);
                string = split[1];
            }
            this.nearbyPlayersCommands.put(string, chance);
        }

        if (this.getConfig().getBool("enabled")) {
            EcoBosses.addBoss(this);
        }
    }

    /**
     * Spawn the boss.
     *
     * @param location The location.
     */
    public void spawn(@NotNull final Location location) {
        LivingEntity entity = bossType.spawnBossEntity(location);
        entity.getPersistentDataContainer().set(this.getPlugin().getNamespacedKeyFactory().create("boss"), PersistentDataType.STRING, name);
        entity.setPersistent(true);

        entity.setCustomName(this.getDisplayName());
        entity.setCustomNameVisible(true);

        AttributeInstance movementSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert movementSpeed != null;
        movementSpeed.setBaseValue(this.getMovementSpeed());

        AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert maxHealth != null;
        maxHealth.setBaseValue(this.getMaxHealth());

        entity.setHealth(maxHealth.getValue());

        AttributeInstance attackDamage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        assert attackDamage != null;
        attackDamage.setBaseValue(this.getAttackDamage());

        for (OptionedSound sound : this.getSpawnSounds()) {
            location.getWorld().playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
        }

        for (String spawnMessage : this.getSpawnMessages()) {
            Bukkit.broadcastMessage(spawnMessage
                    .replace("%x%", StringUtils.internalToString(location.getBlockX()))
                    .replace("%y%", StringUtils.internalToString(location.getBlockY()))
                    .replace("%z%", StringUtils.internalToString(location.getBlockZ()))
            );
        }

        if (this.isBossbarEnabled()) {
            createBossBar(entity);
        }
    }

    private void createBossBar(@NotNull final LivingEntity entity) {
        BossBar bossBar = Bukkit.getServer().createBossBar(
                entity.getCustomName(),
                this.getBossbarProperties().getColor(),
                this.getBossbarProperties().getStyle(),
                (BarFlag) null
        );

        int radius = this.getPlugin().getConfigYml().getInt("bossbar-radius");

        this.getPlugin().getRunnableFactory().create(runnable -> {
            if (!entity.isDead()) {
                bossBar.getPlayers().forEach(bossBar::removePlayer);
                entity.getNearbyEntities(radius, radius, radius).forEach(entity1 -> {
                    if (entity1 instanceof Player) {
                        bossBar.addPlayer((Player) entity1);
                    }
                });
            } else {
                runnable.cancel();
            }
        }).runTaskTimer(0, 40);

        this.getPlugin().getRunnableFactory().create(runnable -> {
            if (!entity.isDead()) {
                bossBar.setProgress(entity.getHealth() / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            } else {
                bossBar.getPlayers().forEach(bossBar::removePlayer);
                bossBar.setVisible(false);
                runnable.cancel();
            }
        }).runTaskTimer(0, 1);
    }

    /**
     * Handle an attack to the player.
     *
     * @param entity The entity.
     * @param player The player.
     */
    public void handleAttack(@NotNull final LivingEntity entity,
                             @NotNull final Player player) {
        for (OptionedSound sound : this.getInjureSounds()) {
            player.getWorld().playSound(entity.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
        }

        for (EffectOption effect : this.getEffects()) {
            if (NumberUtils.randFloat(0, 100) > effect.getChance()) {
                continue;
            }

            player.addPotionEffect(new PotionEffect(effect.getEffectType(), effect.getDuration(), effect.getLevel()));
        }

        if (NumberUtils.randFloat(0, 100) < this.getShuffleChance()) {
            List<ItemStack> hotbar = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                hotbar.add(player.getInventory().getItem(i));
            }
            Collections.shuffle(hotbar);
            int i2 = 0;
            for (ItemStack item : hotbar) {
                player.getInventory().setItem(i2, item);
                i2++;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1, 0.5f);
        }

        for (SummonsOption summon : this.getSummons()) {
            if (NumberUtils.randFloat(0, 100) > summon.getChance()) {
                continue;
            }

            Location loc = player.getLocation().add(NumberUtils.randInt(2, 6), 0, NumberUtils.randInt(2, 6));
            while (!loc.getBlock().getType().equals(Material.AIR)) {
                loc.add(0, 1, 0);
            }
            player.getWorld().spawnEntity(loc, summon.getType());

            for (OptionedSound sound : this.getSummonSounds()) {
                player.getWorld().playSound(entity.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EcoBoss)) {
            return false;
        }

        EcoBoss boss = (EcoBoss) o;
        return this.getName().equals(boss.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }

    @Override
    public String toString() {
        return "EcoBoss{"
                + this.getName()
                + "}";
    }
}
