package buildcraft.additionalpipes.pipes;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.additionalpipes.APConfiguration;
import buildcraft.transport.PipeTransportFluids;

public class PipeLiquidsWaterPump extends APPipe<PipeTransportFluids> {
	private static final int ICON = 24;
	private static final Block water = Blocks.water;

	public PipeLiquidsWaterPump(Item item)
	{
		super(new PipeTransportFluids(), item);
		
		//load the fluid capacities set in mod init
		transport.initFromPipe(getClass());
	}

	@Override
	public void updateEntity() 
	{
		super.updateEntity();
		if(getWorld().getBlock(container.xCoord, container.yCoord - 1, container.zCoord) == water)
		{
			transport.fill(ForgeDirection.DOWN, new FluidStack(FluidRegistry.WATER, APConfiguration.waterPumpWaterPerTick), true);
		}
	}

	@Override
	public int getIconIndex(ForgeDirection direction)
	{
		return ICON;
	}
	
	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side)
	{
		return side != ForgeDirection.DOWN && super.canPipeConnect(tile, side);
	}

}
