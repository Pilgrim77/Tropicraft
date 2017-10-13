package net.tropicraft.core.common.worldgen.village;

import build.world.BuildDirectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tropicraft.core.common.dimension.WorldProviderTropicraft;
import net.tropicraft.core.common.town.ManagedLocation;

import java.util.Iterator;

public class TownKoaVillageGenHelper {

	//relative from the scehamtic center, the entry stairs are in the -x, shaman hut in the +x, so 'left' (at stairs looking to center) is -z, and 'right' is +z
	//buildmod rotations do:
	//0: +x (assumed)
	//1: -z
	//2: -x
	//3: +z
	
	//from schematic
	public static int areaLength = 76; //Z for rot 0
	public static int areaWidth = 86; //X for rot 0
	public static int areaHeight = 16;
	
	
	
	/* Takes coords that are assumed to be a beach, scans to find the ocean side and checks if theres enough ocean space to gen */
	public static boolean hookTryGenVillage(BlockPos parCoords, World parWorld) {
		/*int x = MathHelper.floor_double(player.getX());
		int z = MathHelper.floor_double(player.getZ());
		int y = player.worldObj.getHeightValue(x, z);*/
		
		int directionTry = getBestGenDirection(parCoords, parWorld);
		
		if (directionTry != -1) {
			
			System.out.println("test success! dir: " + directionTry);
			
			BlockPos centerCoords = getCoordsFromAdjustedDirection(parCoords, directionTry);
			
			System.out.println("centerCoords: " + centerCoords);
			
			TownKoaVillage village = new TownKoaVillage();
			
			WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(parWorld);
			
			int minDistBetweenVillages = 128;
			
			Iterator it = wd.lookupTickingManagedLocations.values().iterator();
			while (it.hasNext()) {
				ManagedLocation town = (ManagedLocation) it.next();
				
				if (Math.sqrt(town.spawn.getDistanceSquaredToBlockPos(parCoords)) < minDistBetweenVillages) {
					return false;
				}
			}
			//questionable ID setting
			int newID = wd.lookupTickingManagedLocations.size();
			village.initData(newID, parWorld.provider.dimensionId, centerCoords);
			village.direction = directionTry;
			village.initFirstTime();
			wd.addTickingLocation(village);
			
			return true;
		} else {
			//System.out.println("test fail!");
		}
		
		return false;
	}
	
	/* 
	 * 
	 * tests from the beach position, looks for the most watery direction if any
	 * return value: -1 is fail, 0-3 is direction
	 * 
	 */
	public static int getBestGenDirection(BlockPos parCoords, World parWorld) {
		
		//doing very cheap water check
		//- check per direction
		//-- middle
		//-- end
		//scanning a square shaped area so width by width
		
		//this should possibly get the best angle instead of any working angle, so a circular beach uses the best opposing angle instead of lining up parallel to beach
		//- check corners! that sould solve that
		//- try front left and front right first
		//- trying front left in 4 pieces, same for right
		
		if (isClear(parCoords, parWorld, 1, 0)) return 0;
		if (isClear(parCoords, parWorld, 0, -1)) return 1;
		if (isClear(parCoords, parWorld, -1, 0)) return 2;
		if (isClear(parCoords, parWorld, 0, 1)) return 3;
		
		
		return -1;
	}
	
	public static boolean isClear(BlockPos parCoords, World parWorld, int scanX, int scanZ) {
		int sizeHorizMax = areaWidth;
		int sizeMiddle = areaWidth/2;
		
		int topYBeach = WorldProviderTropicraft.MID_HEIGHT - 1;//parWorld.getHeightValue(parCoords.getX(), parCoords.getZ()) - 1;
		
		//if (topYBeach < WorldProviderTropicraft.MID_HEIGHT) return false;//topYBeach = WorldProviderTropicraft.MID_HEIGHT+1;

		IBlockState state = parWorld.getBlockState(new BlockPos(parCoords.getX(), topYBeach, parCoords.getZ()));
		Block blockScanBeach = state.getBlock();
		
		
		if (blockScanBeach.getMaterial(state) == Material.SAND) {
			//tropiwater isnt counted in getHeightValue apparently so lets hardcode the observed water height val
			int topYMiddle = WorldProviderTropicraft.MID_HEIGHT-1;//parWorld.getHeightValue(parCoords.getX() + (sizeMiddle * scanX), parCoords.getZ() + (sizeMiddle * scanZ)) - 1;
			IBlockState state2 = parWorld.getBlockState(new BlockPos(parCoords.getX() + (sizeMiddle * scanX), topYMiddle, parCoords.getZ() + (sizeMiddle * scanZ)));
			Block blockScanMiddle = state2.getBlock();
			
			System.out.println("testing scanX: " + scanX + " scanZ: " + scanZ);
			
			//if middle of area is water, lets us also know we have our water level to check against for other areas
			if (blockScanMiddle.getMaterial(state2) == Material.WATER) {
				IBlockState state3 = parWorld.getBlockState(new BlockPos(parCoords.getX() + (sizeHorizMax * scanX), topYMiddle, parCoords.getZ() + (sizeHorizMax * scanZ)));
				Block blockScanEnd = state3.getBlock();
				System.out.println("testing blockScanEnd x: " + (parCoords.getX() + (sizeHorizMax * scanX)) + " z: " + (parCoords.getZ() + (sizeHorizMax * scanZ)));
				
				if (blockScanEnd.getMaterial(state3) == Material.WATER) {
					//purposely inverting scanX and scanZ here to make it check the perpendicular
					for (int i = 1; i <= 4; i++) {
						
						int sizeStep = sizeHorizMax / 4 * i;

						IBlockState state4 = parWorld.getBlockState(new BlockPos(parCoords.getX() + (sizeStep * scanZ), topYMiddle, parCoords.getZ() + (sizeStep * scanX)));
						IBlockState state5 = parWorld.getBlockState(new BlockPos(parCoords.getX() + (sizeStep * scanZ*-1), topYMiddle, parCoords.getZ() + (sizeStep * scanX*-1)));
						Block blockScanFrontLeft = state4.getBlock();
						Block blockScanFrontRight = state5.getBlock();
						
						System.out.println("testing blockScanFrontLeft x: " + (parCoords.getX() + (sizeStep * scanZ)) + " z: " + (parCoords.getZ() + (sizeStep * scanX)));
						System.out.println("testing blockScanFrontRight x: " + (parCoords.getX() + (sizeStep * scanZ*-1)) + " z: " + (parCoords.getZ() + (sizeStep * scanX*-1)));
						
						if (blockScanFrontLeft.getMaterial(state4) != Material.WATER || blockScanFrontRight.getMaterial(state5) != Material.WATER) return false;
					}
					
					return true;
				}
				
			}
		}
		
		//int topYEnd = parWorld.getHeightValue(parCoords.getX() + (sizeHorizMax * scanX), parCoords.getZ() + (sizeHorizMax * scanZ)) - 1;
		
		
		
		
		//if (blockScanMiddle.getMaterial() == Material.water && blockScanEnd.getMaterial() == Material.water) return true;
		
		return false;
	}
	
	public static BlockPos getCoordsFromAdjustedDirection(BlockPos parCoords, int parDirection) {
		return new BlockPos(parCoords.getX() + (areaWidth/2 * BuildDirectionHelper.getDirectionToCoords(parDirection).getX()), parCoords.getY(), parCoords.getZ() + (areaWidth/2 * BuildDirectionHelper.getDirectionToCoords(parDirection).getZ()));
	}
	
}
