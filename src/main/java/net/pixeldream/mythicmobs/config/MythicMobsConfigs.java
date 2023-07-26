package net.pixeldream.mythicmobs.config;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.Arrays;
import java.util.List;

public class MythicMobsConfigs extends MidnightConfig {
    @Comment(centered = true)
    public static Comment mobSpawnRates;
    @Entry(isSlider = true, min = 0, max = 100)
    public static int koboldSpawnWeight = 10;
    @Entry(isSlider = true, min = 0, max = 100)
    public static int koboldWarriorSpawnWeight = 8;
    @Entry(isSlider = true, min = 0, max = 100)
    public static int chupacabraSpawnWeight = 20;
    @Entry(isSlider = true, min = 0, max = 100)
    public static int drakeSpawnWeight = 5;
    @Entry(isSlider = true, min = 0, max = 100)
    public static int mushroomSpawnWeight = 20;
    @Comment(centered = true)
    public static Comment mobAttributes;
    @Entry
    public static double automatonHealth = 100.0;
    @Entry
    public static double automatonAttackDamage = 16.0;
    @Entry
    public static double chupacabraHealth = 16.0;
    @Entry
    public static double chupacabraAttackDamage = 6.0;
    @Entry
    public static double drakeHealth = 30.0;
    @Entry
    public static double drakeAttackDamage = 5.0;
    @Entry
    public static double koboldHealth = 20.0;
    @Entry
    public static double koboldAttackDamage = 2.5;
    @Entry
    public static double koboldWarriorHealth = 20.0;
    @Entry
    public static double koboldWarriorArmor = 6.0;
    @Entry
    public static double koboldWarriorAttackDamage = 5.5;
    @Entry
    public static double mushroomHealth = 6.0;
//    @MidnightConfig.Entry
    public static double wendigoHealth = 250.0;
//    @MidnightConfig.Entry
    public static double wendigoArmor = 26.0;
//    @MidnightConfig.Entry
    public static double wendigoAttackDamage = 8.0;

    @Comment(centered = true)
    public static Comment miscellaneous;
    @Entry
    public static boolean replaceIronGolems = true;
    @Entry
    public static List<String> redMushroomLines = Arrays.asList(
            "playerGreeting",
            "Seen any smurfs lately?",
            "I hate Gargamel...",
            "Mondays, huh?",
            "You're a big fellow!",
            "Please don't eat me!",
            "Why did the Creeper hang out with me? Because I'm such a fun-gi to be around!",
            "They say I'm a fun-gi, but I think I'm just a cap-tivating conversationalist!",
            "Looking for a spore-tacular time? You've found the right mushroom!",
            "Did you hear about the fungi who threw a party? It was a real spore-gy!",
            "I'm not edibles!",
            "My cousin Brownie is an alchemist.",
            "Do you know of a bard named Smash Mouth? I hear he's an All Star.",
            "I'm not a regular mushroom; I'm a spore-tacular mushroom!",
            "Some people blame me for the apocalypse...",
            "Γειά σου Ελλάδα!"
    );
    @Entry
    public static List<String> brownMushroomLines = Arrays.asList(
            "Why do I feel like a fun-guy in a no-fun zone?",
            "You want a piece of my spore attitude? Take a hike!",
            "Life's a spore-t, and then you decompose.",
            "Step back, or I'll give you a spore-tacular scowl!",
            "Why do players always pick on mushrooms? Can't we just have a cap-py existence?",
            "I'd rather be left alone in my dark corner. No mushroom for company!",
            "I don't have mushroom for joy, just a whole lot of fung-titude!",
            "Have a fung-tastic day...",
            "I am responsible for the apocalypse.",
            "I'm not grumpy, I'm just a fungi with a perpetual frown.",
            "Why are you bothering me? Can't you see I'm having a spore day?",
            "Why do players keep trying to cheer me up? Do I look like a sunflower to you?",
            "I'm like a mushroom, growing in the darkness of my grumpy thoughts.",
            "Step lightly, or you might awaken the wrath of the grumpy mushroom!",
            "Why be a fun-guy when you can be a grump-guy?",
            "I've mastered the art of being perpetually annoyed. It's my spore-cialty!",
            "Let's get blazed! Go bring the blaze rod.",
            "Is it 4:20 already?"
    );
}
