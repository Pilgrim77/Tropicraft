package net.tropicraft.core.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.tropicraft.core.common.enums.TropicraftTallPlants;

public abstract class BlockTallPlant extends BlockBush implements ITropicraftBlock {

	public static enum PlantHalf implements IStringSerializable {
		LOWER, UPPER;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		@Override
		public String toString() {
			return this.getName();
		}
	};
	
	protected static final AxisAlignedBB FULL_HEIGHT_AABB = BUSH_AABB.setMaxY(1);

	public static final PropertyEnum<TropicraftTallPlants> VARIANT = PropertyEnum.create("variant", TropicraftTallPlants.class);
	public static final PropertyEnum<PlantHalf> HALF = PropertyEnum.create("half", PlantHalf.class);

	public BlockTallPlant() {
		super(Material.PLANTS);
		this.setHardness(0.0F);
		this.setSoundType(SoundType.GROUND);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, TropicraftTallPlants.PINEAPPLE).withProperty(HALF, PlantHalf.LOWER));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT, HALF);
	}

	@Override
	public String getStateName(IBlockState state) {
		return ((TropicraftTallPlants) state.getValue(VARIANT)).getName();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HALF, PlantHalf.values()[meta >> 3]).withProperty(VARIANT, TropicraftTallPlants.byMetadata(meta & 7));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((PlantHalf) state.getValue(HALF)).ordinal() * 8 + ((TropicraftTallPlants) state.getValue(VARIANT)).ordinal();
	}

	public BlockPos getLowerPos(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {return pos;}
		return world.getBlockState(pos).getValue(HALF) == PlantHalf.UPPER ? pos.down() : pos;       
	}

	public BlockPos getUpperPos(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {return pos.up();}
		return world.getBlockState(pos).getValue(HALF) == PlantHalf.UPPER ? pos : pos.up();       
	}

	public IBlockState getStateLower(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(getLowerPos(world, pos));
	}

	public IBlockState getStateUpper(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(getUpperPos(world, pos));
	}

	// Called by ItemBlock before the block is placed - the placed block must always be Half.LOWER
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getStateFromMeta(meta).withProperty(HALF, PlantHalf.LOWER);
	}
	
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    	return state.getValue(HALF) == PlantHalf.LOWER ? FULL_HEIGHT_AABB : BUSH_AABB;
    }
    
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == PlantHalf.UPPER ? super.getItemDropped(state, rand, fortune) : null;
    }
}
