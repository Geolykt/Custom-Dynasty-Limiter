# Custom Dynasty Limiter

Small mod that allows to change the maximum amount of persons in a dynasty via SLAPI's modconf system (Found in "Mods" > "Mod Settings" in the game's settings menu).
With releases of SLAPI before the 19th of November 2023, this mod will not be able to use IntegerOption#addValueChangeListenerI and as such will be unable to detect
changes in the configuration: changes will as such only apply after a restart.

## Dependencies

 - SLAPI (1.3.X-2.X)
 - SLL (4.X)
 - Java (8-21)
