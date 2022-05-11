.PHONY: eden rosetta surface checkpoint flock surface2

eden:
	edex buildfiles/eden.edex
rosetta:
	edex buildfiles/rosetta.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Rosetta.jar ./lib/Rosetta.jar
	cp ./lib/Rosetta.jar ./internalModules
flock:
	edex buildfiles/flock.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Flock.jar ./lib/Flock.jar
	cp ./lib/Flock.jar ./internalModules
surface:
	edex buildfiles/surface.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Surface.jar ./lib/Surface.jar
	cp ./lib/Surface.jar ./internalModules
surface2:
	edex buildfiles/surface2.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Surface2.jar ./lib/Surface2.jar
	cp ./lib/Surface2.jar ./internalModules
checkpoint:
	edex buildfiles/checkpoint.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Checkpoint.jar ./lib/Checkpoint.jar
	cp ./lib/Checkpoint.jar ./internalModules
libs:
	edex buildfiles/eden.edex
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden-* ./lib

	edex buildfiles/surface2.edex -noheader
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Surface2.jar ./lib/Surface2.jar
	cp ./lib/Surface2.jar ./internalModules
	
	edex buildfiles/checkpoint.edex -noheader
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Checkpoint.jar ./lib/Checkpoint.jar
	cp ./lib/Checkpoint.jar ./internalModules

	edex buildfiles/flock.edex -noheader
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Flock.jar ./lib/Flock.jar
	cp ./lib/Flock.jar ./internalModules
	
	edex buildfiles/rosetta.edex -noheader
	cp ~/Servers/Minecraft/Minecraft\ 1.18/plugins/Eden/mods/Rosetta.jar ./lib/Rosetta.jar
	cp ./lib/Rosetta.jar ./internalModules