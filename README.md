# WordBank

by ProgrammerDan for [DevotedMC](https://www.github.com/DevotedMC), also found on [reddit](https://www.reddit.com/r/Devoted) and for Minecraft gamers, join us at play.devotedmc.com

## What is it?

A tool for branding and unique imprinting.

The premise is simple. 

Using the anvil, name a tool, item, book, stack of potatos whatever. Give it a string of around 10 characters (configurable) as its name, like `1kZ9dlkPPe`. Then take it to an enchanting table, and right-click the 
table with the item. That seemingly random string of letters and numbers will be magically transformed into a
unique, colored name, at the small cost of 10XP (configurable!). It will have between one and three
(configurable!) words as a result. The item will also never be able to be renamed, ever again. 

That's about it! Enjoy your uniquely branded products, and the joy of discovering new names.

## Technical Details

To thwart easy "solving", this plugin uses several layers of obfuscation to achieve its goal of deriving a
final brand name from the "key", or input string. 

A word of warning, however. Changing any of the knobs post-deployment will definitely break prior discovered "brands". I strongly recommend you test your config extensively before deploying this, to be sure it does as you desire.

###That said, the knobs:

First, the actual expected # of characters, controlled by `activation_length` in the config, determines
the actual number of letters that are used in key computation.

Second, `activate_any_length` if on, uses any custom name (but not branded) and either truncates or
pads with `padding` to force the "used" length to equal `activation_length`.

Third, place a word list file in the plugin's folder, and supply the word list file name in `wordlist_file`. Users do not need to know the actual word list, although you can feel free to publish it if you want to "hype" what can be discovered.

Fourth, `word.max` determines the max number of words that can be applied as a brand.

Fifth, configure `word.count` and suboptions to decide how, from the input "key", the number of output words is decided.

Sixth, configure `word.N` where `N` is a number from `0` to `word.max - 1` and suboptions to decide how each word is decided. You can choose a unique function, and function parameters for each word.

Finally, configure `color` and suboptions to decide the color that is applied to the branded item.

### Configuring `color` and `word.*`

Each of the "key"-sensitive configurations take 3 suboptions.

`chars:` This is an integer array (typically use `[1, 4, 5]` yaml notation) indicating which characters from the "key" should be picked and passed to the function that converts those characters into byte data and from there into a number from [0.0 to 1.0). Note that these are _indexes_, so `0` is the first character and `N` is the last character, based on `activation_length - 1` as the max. You can "pick" the same character twice, use all or only some characters, your choice. If you want players to be able to more easily "solve" for color, for instance, you might use only `[0,2,1]` to determine the color.

`function:` This chooses the actual transform function to use. Currently distributed are:

  * `com.programmerdan.minecraft.wordbank.functions.LinearMap`

  * `com.programmerdan.minecraft.wordbank.functions.Modulus`

  * `com.programmerdan.minecraft.wordbank.functions.HashMap`

  * `com.programmerdan.minecraft.wordbank.functions.NormalDistribution`

  * `com.programmerdan.minecraft.wordbank.functions.TwoTailDistribution`

You could also write your own function; see the references for details.

`function_terms:` These are the parameters to the function. For a given function they must all be of the same type, e.g. all integers, all strings, etc. but otherwise there are no limits to how many, they must simply satisfy the function you picked in `function`.

### `function` quick reference

Choose wisely. No turning back once live.

####`com.programmerdan.minecraft.wordbank.functions.LinearMap`

Not even sure this is worth using, but it constructs a large bit sequence, and divides that into the maximum positive bitsequence of the same size, to produce a number between 0.0 and 1.0. It requires no `function_terms` at all.

####`com.programmerdan.minecraft.wordbank.functions.Modulus`

Slightly broken as demonstrated by my unit tests; it doesn't always return all possible values under the modulus transform. That said it uses an aggregation % modulo strategy to produce a small-ish number which gets divided into the `modulo - 1`. 

It requires a single `function_terms` indicating the modulo.

Example:

    color:
      chars: [0, 2, 3, 1]
      function: com.programmerdan.minecraft.wordbank.functions.Modulus
      function_terms:
       - 16 

####`com.programmerdan.minecraft.wordbank.functions.HashMap`

This is straightforward one-way cryptographic transform. It maps the selected byte data of the characters defined in `chars` to a byte array, which is passed through a `MessageDigest` hash function. The result should be a psuedo-uniform value between `0` and `1`.

It requires a single `function_terms` indicating the MessageDigest function to use. I've personally tested with `MD5`, `SHA-1`, `SHA-256`, and `SHA-512`. 

Example:

    color:
      chars: [0, 2, 3, 1, 4, 9, 5, 6, 7, 8]
      function: com.programmerdan.minecraft.wordbank.functions.HashMap
      function_terms:
       - SHA-1

####`com.programmerdan.minecraft.wordbank.functions.NormalDistribution`

This is a bit trickier to explain, but by some math magic, this transforms the _uniform_ distribution of `HashMap` into an approximately _normal_ or _Gaussian_ distribution, with the mean right around `0.5`. In other words, this function tends to generate values close to `0.5` and less often close to `0` or `1`.

It also requires a single `function_terms` indicating the MessageDigest function to use.

####`com.programmerdan.minecraft.wordbank.functions.TwoTailDistribution`

An accidental discovery, this produces a "two-tailed" distribution. Meaning, it prefers to generate values near `0` or `1` and tends not to produce values close to `0.5`. Mathematically, it's a kind of "opposite" of `NormalDistribution`.

It also requires a single `function_terms` indicating the MessageDigest function to use.

### Other options

`cost:` This is a Bukkit "ConfigurationSerializable" representation of an in-game item. An example:

    cost:
      ==: org.bukkit.inventory.ItemStack
      type: EXP_BOTTLE
      amount: 10

This means 10 "experience_bottles" are required to apply a brand to an item or stack of items.

`debug:` Turn this on to spam your console with debug messages

`makers_mark:` This is the value applied as lore to the branded items that "locks" them from being renamed in the future. It's also used in some messages sent to players so choose wisely.

`configuration_file_version:` Don't change this.

`db:` This option and its suboptions configure the database, which is used to keep track of who is using what brands.

An example:

    db:
      driver: mysql
      host: localhost
      name: wordbank
      port: 3306
      user: root
      password: ''

Under the covers this is using HikariCP, but I've taken are of the messy connection pool stuff for you. Currently only MySQL/MariaDB are supported.

# Commands

Use `/wordbank` to see a list of every "key" used so far. Page through using `/wordbank 2` etc.

Use `/wordbank [key]` to see how many times and by how many people a "key" has been used. Can also be paged through.

By default these commands are only available to OPs or via the `wordbank.godmode` permission.

# Permissions

`wordbank.godmode` gives access to `/wordbank` and `/wordbank [key]` so far.

`wordbank.default` does nothing yet, but may in the future be used to allow players to "look back" over their history of discovered "keys".

# Unit Tests and Dev goodies

For the bukkit developer, this project might interest you as I illustrate using two "advanced" technologies: Connection Pools (using HikariCP) and JUnit tests with Mockito. 

I also demonstrate a few single probability tests, although they are fairly naive in construction.

Feel free to ask me questions about these or other topics. 
