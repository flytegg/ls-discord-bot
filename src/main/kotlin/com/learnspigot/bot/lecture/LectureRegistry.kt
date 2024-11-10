package com.learnspigot.bot.lecture

import java.util.*

class LectureRegistry {

    private val lectures = mutableListOf<Lecture>()
    private val matcher: WordMatcher = WordMatcher()

    fun findLectures(query: String, amount: Int): MutableList<Lecture> {
        return matcher.getTopLectures(
            query.lowercase(Locale.getDefault())
                .replace("\n", " ") // Replace line breaks with spaces
                .replace("\\b(the|a|an)\\b".toRegex(), "") // Remove determiners
                .replace("[^\\w\\s]".toRegex(), "") // Only leave valid characters
                .replace("\\s+".toRegex(), " "), // Clean up stacked spaces
            lectures,
            amount).toMutableList()
    }

    init {
        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860442",
                    "Introduction",
                    "I'll explain all about the course, the content within it, my credibility, the support networks available and how to make the most of this resource!"
                ),
                Lecture(
                    "29860444",
                    "Installing IntelliJ (Windows)",
                    "You'll learn how to install IntelliJ, the software we'll be using throughout, using Windows OS."
                ),
                Lecture(
                    "29860448",
                    "Installing IntelliJ (Mac)",
                    "You'll learn how to install IntelliJ, the software we'll be using throughout, using Mac OS."
                ),
                Lecture(
                    "35191380",
                    "Installing IntelliJ (Linux)",
                    "You'll learn how to install IntelliJ, the software we'll be using throughout, using Linux-based Ubuntu (22.04)."
                ),
                Lecture(
                    "32006130",
                    "Optimizing IntelliJ",
                    "You'll learn the basics of how IntelliJ works, how to get my theme and some other top tips."
                ),
                Lecture(
                    "29860450",
                    "Creating Spigot Server (Windows)",
                    "You'll learn how to setup your very own local Spigot server using Windows OS."
                ),
                Lecture(
                    "29860452",
                    "Creating Spigot Server (Mac)",
                    "You'll learn how to setup your very own local Spigot server using Mac OS."
                ),
                Lecture(
                    "35191378",
                    "Creating Spigot Server (Linux)",
                    "You'll learn how to setup your very own local Spigot server using Linux-based Ubuntu (22.04)."
                ),
                Lecture(
                    "29860454",
                    "Discord",
                    "I'll introduce you to our exclusive student-only Discord community and support server! Come join us @ discord.gg/9WuhJJkCsa"
                ),
                Lecture(
                    "29860422",
                    "First Plugin",
                    "You'll make your very first plugin! We start basic, with a simple console message, after setting up the whole environment for future lectures."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860426",
                    "Events",
                    "You'll begin to understand what events are and how to listen to them - in this lecture we look at the PlayerMoveEvent and the PlayerEggThrowEvent!"
                ),
                Lecture(
                    "29860428",
                    "Commands",
                    "You'll learn how to create basic commands. We'll make a /heal command to start off!"
                ),
                Lecture(
                    "29860430",
                    "Command Arguments",
                    "You'll learn how to start expanding your command possibilities using optional arguments from the player."
                ),
                Lecture(
                    "29860434",
                    "Console Commands",
                    "You'll learn how to allow your console users to run your commands!"
                ),
                Lecture(
                    "29860438",
                    "Configuration File (config.yml)",
                    "You'll learn how to create a configuration file, read and set data."
                ),
                Lecture(
                    "29860440",
                    "Permissions",
                    "You'll learn how to incorporate permission check into your plugin (i.e. allowing actions based on whether user has X permission)."
                ),
                Lecture(
                    "29860590",
                    "Javadocs",
                    "You'll learn the importance of the Javadocs throughout the course and your development career, and understand how to read and search them."
                ),
                Lecture(
                    "29860592",
                    "Reading Errors & Debugging",
                    "You'll learn the essential skills of reading errors and debugging which will be used throughout the course!"
                ),
                Lecture(
                    "29860594",
                    "Entities",
                    "You'll learn some of the key methods and events relating to entities."
                ),
                Lecture(
                    "29860596",
                    "Blocks, Materials & ItemStacks",
                    "You'll learn the key methods related to block manipulation, ItemStacks and the Material's behind them."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "31034476",
                    "Additional ItemMeta (Dyed Leather Armor etc.)",
                    "You'll learn how to use other forms of ItemMeta such as LeatherArmorMeta to make dyed armor."
                ),
                Lecture(
                    "29860598",
                    "Action Bar, Titles & Tablist",
                    "You'll learn how to send custom action bars (1.9+), titles (1.9) and player list header and footers (1.13+). For lower versions, see packets section."
                ),
                Lecture(
                    "29860600",
                    "Boss Bars",
                    "You'll learn how to create boss bars with lots of customization, and also how to edit them in real time!"
                ),
                Lecture(
                    "29860604",
                    "Fireworks",
                    "You'll learn how to create and give extremely customizable fireworks to players."
                ),
                Lecture(
                    "29860606",
                    "Potion Effects",
                    "You'll learn the key methods regarding potion effects and how to apply them to players with different settings."
                ),
                Lecture(
                    "29860610",
                    "Worlds (Weather, Time etc.)",
                    "You'll learn all the key methods regarding worlds; including how to get them, the methods you can use on them and the key events."
                ),
                Lecture(
                    "29860612",
                    "1.16 Hex Color Codes",
                    "In version 1.16+, you'll learn how to send custom Hex and RGB colours. As well as a bonus gradient and translation tutorial!"
                ),
                Lecture(
                    "29860614",
                    "Sounds",
                    "You'll learn how to send sounds to single or all players and the key settings involved."
                ),
                Lecture(
                    "30742712",
                    "Note Block Sounds & Music Discs",
                    "You'll learn how to play every Note Block sound and music discs."
                ),
                Lecture(
                    "29860616",
                    "Projectiles",
                    "You'll learn the key events related to projectiles and also create a gun-like Egg-shooting Diamond Hoe!"
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture("30195558", "Particles", "You'll learn how to spawn particles to players & worlds."),
                Lecture(
                    "29860618",
                    "Toggling",
                    "You'll learn the importance of toggling logic and how it works in practise!"
                ),
                Lecture(
                    "29860622",
                    "Vanish",
                    "You'll learn how to vanish players and even incorporate it into a custom command!"
                ),
                Lecture(
                    "29860624",
                    "PROJECT: Guns",
                    "We'll bring together a load of content from the earlier videos and implement some guns into Minecraft!"
                ),
                Lecture(
                    "29860626",
                    "Custom Books",
                    "You'll learn how to create a fully custom book - including setting the title, author, pages and other settings."
                ),
                Lecture(
                    "29860628",
                    "Custom Banners",
                    "You'll learn how to create custom banners and give them to players."
                ),
                Lecture(
                    "29860630",
                    "Moderation Tools (Kick, Ban etc.)",
                    "You'll learn how to kick, ban and temporarily ban players using Bukkit's build-in punishment system!"
                ),
                Lecture(
                    "29860632",
                    "Setting Resource Packs",
                    "You'll learn the key events related to resource packs and also how to force them from the server."
                ),
                Lecture(
                    "29860634",
                    "Riding Entities",
                    "You'll learn the key methods that allows any entity to ride any entity (really!)."
                ),
                Lecture(
                    "31038296",
                    "Player Statistics",
                    "You'll learn how to retrieve all the default-stored Minecraft statistics and how to change them."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860636",
                    "Setting MOTD, Server Icon & Player Count",
                    "You'll learn the methods used in the ServerPingEvent to set the server MOTD, icon and max player count."
                ),
                Lecture(
                    "29860638",
                    "PROJECT: Private Message System",
                    "We'll bring together a load of content from this section and create a functioning message system between players."
                ),
                Lecture(
                    "29869446",
                    "Runnables",
                    "You'll learn all about runnables and their importance, aswell as the key ones we'll be using and how to use them!"
                ),
                Lecture(
                    "29869450",
                    "GUI Creation",
                    "You'll learn how to create your very first GUI by making a cool staff/moderation menu! It'll also teach how to be sustainable, scalable and how to make your own ideas come to life."
                ),
                Lecture(
                    "29869452",
                    "GUI Interaction",
                    "You'll learn how to make GUI's clickable and user friendly, while keeping safety measures in mind. We'll make our GUI from last lecture fully functional and responsive!"
                ),
                Lecture(
                    "29869456",
                    "Command Tab Complete",
                    "You'll learn how to add custom tab complete options to your commands!"
                ),
                Lecture(
                    "33779108",
                    "Attribute Modifiers (1.16+)",
                    "This lecture you'll learn how to modify attributes on entities and items."
                ),
                Lecture(
                    "29869458",
                    "Block Data (Doors, Signs etc.)",
                    "You'll learn how to set specifc block data about all supported blocks, using a Rail, Cake & Glass Panes as examples!"
                ),
                Lecture(
                    "29869462",
                    "Per-Player Blocks & Signs",
                    "You'll learn how to send block and sign information to just one player rather than the whole server!"
                ),
                Lecture(
                    "29869466",
                    "Custom Skulls (Players & Textures)",
                    "You'll learn how to create fully custom skulls - by (a) setting it to a player's head, or (b) setting it to a custom texture."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29869468",
                    "Custom YML Files",
                    "You'll learn how to generate custom files for data storage, extra configuration, languages etc."
                ),
                Lecture(
                    "30195612",
                    "Custom JSON Files",
                    "You'll learn how to generate files for data storage in the JSON format!"
                ),
                Lecture(
                    "29869470",
                    "Custom Maps (Text, Images etc.)",
                    "You'll learn how to draw coloured pixels & shapes, create custom text and add any image to maps!"
                ),
                Lecture(
                    "29869472",
                    "Custom Crafting Recipes",
                    "You'll learn how to create custom crafting recipes using three different examples!"
                ),
                Lecture(
                    "34165536",
                    "Persistent Data Containers",
                    "You'll learn how to use persistent data containers with entities, tile entities, itemstacks and chunks. This data persists over server restart."
                ),
                Lecture(
                    "29869474",
                    "Cooldowns",
                    "You'll learn about the logic behind cooldowns and how to make the most efficient and effective system."
                ),
                Lecture(
                    "29869476",
                    "Holograms",
                    "You'll learn how to create cool-looking single and multi-line holograms and how to make them clickable!"
                ),
                Lecture(
                    "37363462",
                    "Display Entities (1.19.4+)",
                    "You'll learn how to create amazing floating text, blocks and items without Texture Packs."
                ),
                Lecture(
                    "29869478",
                    "Setting Permissions",
                    "You'll learn the (annoyingly complicated) way of manually applying and removing specific permissions on players."
                ),
                Lecture(
                    "29869482",
                    "Scoreboard #1 - Static",
                    "You'll learn how to display information that is unlikely to change on a player's sidebar!"
                ),
                Lecture(
                    "29869486",
                    "Scoreboard #2 - Dynamic",
                    "You'll learn how to add smooth seamless data which changes in your sidebar, alongside the static lines!"
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29869488",
                    "Nametags",
                    "You'll learn how to set player prefixes and suffixes (i.e. nametags) which show up on the tablist and above the player."
                ),
                Lecture(
                    "29869490",
                    "PROJECT: Rank System",
                    "You'll learn how to make a fully custom rank system, with all of your own savable ranks that work with nametags, chat formats & setting permissions."
                ),
                Lecture(
                    "29961814",
                    "Clickable/Hoverable Chat",
                    "You'll learn how to make clickable and hoverable text show in chat!"
                ),
                Lecture(
                    "29961816",
                    "Clickable/Hoverable Books",
                    "You'll learn how to make optional clickable and hoverable text within custom books."
                ),
                Lecture(
                    "29961818",
                    "Forcing Custom Skins",
                    "You'll learn the (slightly-complex) way of forcing a player to have a custom skin."
                ),
                Lecture(
                    "29961822",
                    "Custom Events",
                    "You'll learn how to create a fully custom event, with optional cancellable implementation, and how to call it and listen to it."
                ),
                Lecture(
                    "29961824",
                    "Using Plugin APIs",
                    "You'll learn how to connect to other plugins using theirs APIs - namely WorldEdit in this example!"
                ),
                Lecture(
                    "29961826",
                    "Creating Custom API",
                    "You'll learn how to convert your plugin into an API usable by others, following the best conventions and practises."
                ),
                Lecture(
                    "30742714",
                    "Creating & Playing Note Block Music",
                    "You'll learn (a) how to create your very own Note Block music and (b)&nbsp;how to play this or any other public Note Block song to players!"
                ),
                Lecture(
                    "35190590",
                    "Anvil Text Input",
                    "You'll learn how to accept player text input through an anvil and how to do things with the input!"
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29961834",
                    "Regions",
                    "You'll learn how to create a custom 3d region between two bounds, allowing for mass block updates, player tracking etc."
                ),
                Lecture(
                    "32195856",
                    "Custom Model Data (w/ Resource Packs)",
                    "You'll learn how resource packs work and how to utilise custom model data in 1.14+ to add (near)&nbsp;unlimited models."
                ),
                Lecture(
                    "35258896",
                    "Custom Enchantments",
                    "You'll learn how to create your own unique enchantment, how to apply it to an item and set the impact of it."
                ),
                Lecture(
                    "35440392",
                    "AI Chat",
                    "You'll learn how to make it so you can have conversations with an AI bot and give it certain characters to act as."
                ),
                Lecture(
                    "29961836",
                    "GUI Pages",
                    "You'll learn how to make pages for GUIs which dynamically generate depending on contents!"
                ),
                Lecture(
                    "30742708",
                    "Discord/Minecraft Bridge",
                    "You'll learn how to create a link between your Minecraft and Discord bot to allow for limitless possibilities; from syncing data, to sending messages, to sharing events etc."
                ),
                Lecture(
                    "29961840",
                    "PROJECT: Command Manager (No Plugin.yml)",
                    "You'll learn how to bring together all the command and tab completion content to make an efficient, scalable command manager which requires no plugin.yml info!"
                ),
                Lecture(
                    "29860486",
                    "Creating & Building Database",
                    "You'll learn all about databases, their benefits and use-cases, aswell as creating and building your own."
                ),
                Lecture(
                    "29860488",
                    "Connecting to Database",
                    "You'll learn how to connect to your database - whether local or externally hosted - using the the correct settings."
                ),
                Lecture(
                    "29860490",
                    "Key SQL Commands (Querying, Updating etc.)",
                    "You'll learn the essential SQL commands, everything from setting, updating, deleting and retrieving information from your database!"
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860494",
                    "Player Profiles",
                    "You'll learn how to make a wrapper for each player which provides useful functionality to connect to the database in setting and retrieving information."
                ),
                Lecture(
                    "29860496",
                    "Using HikariCP",
                    "You'll learn how to use HikariCP - a connection pooling API - in order to speed up requests and make your life easier!"
                ),
                Lecture(
                    "34168474",
                    "Using MongoDB (Installation, Connecting, Using)",
                    "You'll learn how to use MongoDB as an alternative to an SQL database. You'll download the relevant software and learn how to push and pull data."
                ),
                Lecture(
                    "30007374",
                    "Mechanics #1",
                    "You'll learn about what we'll be making and creating the configuration file which will govern the rest of our minigame."
                ),
                Lecture(
                    "30007376",
                    "Mechanics #2",
                    "You'll create the arena manager and arena classes which control the games."
                ),
                Lecture(
                    "30007380",
                    "Mechanics #3",
                    "You'll create the countdown and game classes which compliment the arena class to provide the minigame experience."
                ),
                Lecture(
                    "30007384",
                    "Mechanics #4",
                    "You'll create the required listeners and an arena command to ensure the most user-friendly approach. Then we're done!"
                ),
                Lecture(
                    "30126494",
                    "PROJECT: Bedwars",
                    "You'll learn how to convert our simple minigame mechanics framework into a fully functional Bedwars game!"
                ),
                Lecture(
                    "30126466",
                    "Kits (w/ Selection GUI)",
                    "You'll learn how to implement kits and a selection GUI to the minigame."
                ),
                Lecture(
                    "30126470",
                    "Teams (w/ Selection GUI)",
                    "You'll learn how to implement teams and a selection GUI to the minigame."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "30126476",
                    "Resetting Maps",
                    "You'll learn how to add multiple maps and make them reset after each game."
                ),
                Lecture(
                    "30126478",
                    "Arena Signs",
                    "You'll learn how to create multiple scalable signs which allow users to join specific arenas."
                ),
                Lecture(
                    "30126480",
                    "NPC Join",
                    "You'll learn how to create multiple scalable NPCs which allow users to join specific arenas."
                ),
                Lecture(
                    "30126484",
                    "Customisable Messages File",
                    "You'll learn how to make all of the messages within the minigame fully configurable."
                ),
                Lecture(
                    "30126488",
                    "Network Compatability",
                    "You'll learn how to convert our setup to a BungeeCord network to have an unlimited server system."
                ),
                Lecture(
                    "30126490",
                    "Supporting Multiple Games (Converting to Engine)",
                    "You'll learn how to convert our setup to an engine which supports unlimited games which can be added extremely easily."
                ),
                Lecture(
                    "29979080",
                    "Creating Cosmetic Foundation",
                    "You'll learn about the nature of cosmetics and how we're going to proceed in this section! We make a base GUI, command and listener."
                ),
                Lecture(
                    "29979082",
                    "Cosmetic #1 - Hats",
                    "You'll learn how to add custom player skulls onto peoples heads - as well as creating a fully functioning toggling system and paving the way for future lectures."
                ),
                Lecture(
                    "30299092",
                    "Cosmetic #2 - Trails",
                    "You'll learn how to spawn cool particles behind players as they move!"
                ),
                Lecture(
                    "30276474",
                    "Saving Cosmetic Data (YML Files)",
                    "You'll learn how to save what cosmetics people own in local YML files."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860512",
                    "Creating BungeeCord Network (Windows)",
                    "You'll learn how to create your very own BungeeCord network using Windows OS, setting up two Spigot servers to connect to each other!"
                ),
                Lecture(
                    "29860514",
                    "Creating BungeeCord Network (Mac)",
                    "You'll learn to create your very own BungeeCord network using Mac OS!"
                ),
                Lecture(
                    "35192316",
                    "Creating BungeeCord Network (Linux)",
                    "You'll learn to create your very own BungeeCord network using Linux (Ubuntu 22.04)!"
                ),
                Lecture(
                    "29860516",
                    "First BungeeCord Plugin",
                    "You'll learn how to create your very first BungeeCord plugin using Maven & IntelliJ!"
                ),
                Lecture(
                    "29860518",
                    "Bungee Commands, Events & Schedulers",
                    "You'll learn how to make basic commands, listen to events and run schedulers using the BungeeCord API."
                ),
                Lecture(
                    "29860520",
                    "Bungee Command Tab Complete",
                    "You'll learn how to add custom tab complete options to your commands in BungeeCord!"
                ),
                Lecture(
                    "29860522",
                    "Bungee Setting MOTD, Network Icon & Player Count",
                    "You'll learn how to set a custom MOTD, server favicon, online player count, max player count and version information."
                ),
                Lecture(
                    "31079722",
                    "PROJECT: Network Private Messaging",
                    "You'll learn how to make a private messaging system across servers by bringing together a load of content from this section."
                ),
                Lecture(
                    "29961838",
                    "Plugin Messaging (Cross-Server Communication)",
                    "You'll learn how to use the BungeeCord Plugin Messaging Channel in order to communicate between Spigot servers."
                ),
                Lecture(
                    "33985306",
                    "UUID/Name Conversion (Mojang API)",
                    "This lecture you'll learn how to convert names and UUIDs to be used across the network."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "35042092",
                    "Understanding NMS",
                    "You'll learn everything about NMS. What it is, why it exists and how to use it. We'll use this info in the following videos to do some cool things!"
                ),
                Lecture(
                    "35416526",
                    "Sending Packets",
                    "You'll learn how to find, interpret and send packets to players."
                ),
                Lecture(
                    "35042646",
                    "PROJECT: Player NPCs",
                    "You'll learn how to spawn player NPCs with custom skins and locations, as well as giving their custom armor or items to hold."
                ),
                Lecture(
                    "35247942",
                    "Custom Packet Listener (Clickable NPCs)",
                    "You'll learn how to create a custom packet listener to read incoming and outgoing packets. The example in this lecture is making a Player NPC clickable."
                ),
                Lecture(
                    "32065494",
                    "Finding Ideas, Planning & Staying Motivated",
                    "You'll learn the best tips on finding ideas, how to setup the best planning environment and how to stay motivated on those long painful projects."
                ),
                Lecture(
                    "32129970",
                    "Writing & Keeping Code Clean",
                    "You'll learn all the best industry-standard practises on keeping your code clean and readable."
                ),
                Lecture(
                    "29860562",
                    "Optimised Start.bat Flags",
                    "You'll learn the most optimized and stable flags to incorporate in your start.bat instructions to help the server run smoother and with higher TPS!"
                ),
                Lecture(
                    "35189142",
                    "PlaceholderAPI (Using & Creating)",
                    "You'll learn how to hook into an extremely valuable API named PlaceholderAPI which is used by all the big plugins. You'll know how to use existing placeholders and even create your own for your own plugin!"
                ),
                Lecture(
                    "30196408",
                    "Creating Multi-Version Plugins",
                    "You'll learn the best advice for creating plugins which support multiple/different Minecraft versions."
                ),
                Lecture(
                    "31972738",
                    "Supporting Multiple Languages",
                    "You'll learn the best practises and methods of supporting multiple speaking languages to allow for an easier user experience."
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "33642386",
                    "Adding Plugin Metrics (bStats)",
                    "You'll learn how to start collecting information about where and how your plugin is used using popular software called bStats."
                ),
                Lecture(
                    "29860564",
                    "Considering Spigot Forks",
                    "You'll learn about alternatives to Spigot, such as Paper and Waterfall, and why you should and shouldn't use them."
                ),
                Lecture(
                    "35191976",
                    "Using Gradle",
                    "You'll learn how to generate Minecraft projects with Gradle on IntelliJ, how to use dependencies and how to build your projects."
                ),
                Lecture(
                    "29860566",
                    "Using GitHub",
                    "You'll learn how to push your projects to GitHub, manage them and take advantage of GitHub's features."
                ),
                Lecture(
                    "35179306",
                    "Publishing to Maven & Gradle",
                    "You'll learn how to make your plugin API accessable to other developers through Maven and Gradle build tools."
                ),
                Lecture(
                    "34395636",
                    "Student Discounts",
                    "You'll learn how to enjoy the awesome student discounts offered by GitHub."
                ),
                Lecture(
                    "29860568",
                    "Plugin Licensing",
                    "You'll learn how to add a mandatory license system to your plugin which helps protect it from piracy."
                ),
                Lecture(
                    "29860570",
                    "Maximizing Plugin Sales",
                    "You'll learn my tried and tested strategies on pricing, presenting and how to best market your Spigot plugin to make the most $$$. I use Spigot as the case study marketplace, but the tips & tricks remain the same."
                ),
                Lecture(
                    "29860572",
                    "Ending...",
                    "The final lecture... it's been a long ride! Let's talk about the future..."
                ),
                Lecture(
                    "29860548",
                    "Java Basics #1",
                    "You will learn:\n- Introduction to Java & OOP\n- Variables & Objects\n- Data Types (Primitive & Non Primitive)\n- Methods (including Parameters & Returning)"
                )
            )
        )

        lectures.addAll(
            mutableListOf(
                Lecture(
                    "29860550",
                    "Java Basics #2",
                    "You will learn:\n- If Statements & Operators\n- Null\n- Exceptions & Try/Catch\n- Classes, Instances & Constructors"
                ),
                Lecture(
                    "29860554",
                    "Java Basics #4",
                    "You will learn:\n- Inheritance (Superclass & Subclass)\n- Abstract Methods & Classes\n- Enumerators\n- Static Keyword"
                ),
                Lecture(
                    "29860556",
                    "Java Basics #5",
                    "You will learn:\n- Switch Statement\n- Date & Time\n- Randomness"
                ),
                Lecture(
                    "29860552",
                    "Java Basics #3",
                    "You will learn:\n- Arrays\n- List (& LinkedList)\n- HashMap (& LinkedHashMap)\n- Loops (For & While)"
                )
            )
        )
    }

}