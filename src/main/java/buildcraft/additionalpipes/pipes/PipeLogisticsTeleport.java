/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.additionalpipes.pipes;

import java.util.List;

import logisticspipes.api.ILPPipeTile;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.additionalpipes.api.PipeType;
import buildcraft.additionalpipes.utils.Log;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeLogisticsTeleport extends PipeTeleport<PipeTransportItemsLogistics>  {
	private static final int ICON = 0;

	public PipeLogisticsTeleport(Item items) {
		super(new PipeTransportItemsLogistics(), items, PipeType.ITEMS);
	}
	
	public void eventHandler(PipeEventItem.Entered event)
	{
		if(getWorld().isRemote) 
		{
			return;
		}
		
		PipeLogisticsTeleport otherPipe = getConnectedPipe();
		
		// cannot teleport, use default
		if(otherPipe == null || !canSend()) {
			return;
		}

		Position insertPoint = otherPipe.getPosition();
		insertPoint.x += 0.5;
		insertPoint.y += TransportUtils.getPipeFloorOf(event.item.getItemStack());
		insertPoint.z += 0.5;
		insertPoint.moveForwards(0.5);
		event.item.setPosition(insertPoint.x, insertPoint.y, insertPoint.z);
		
		ForgeDirection newOrientation = otherPipe.getOpenOrientation().getOpposite();
		otherPipe.transport.injectItem(event.item, newOrientation);
		
//		ForgeDirection newOrientation = otherPipe.getOpenOrientation().getOpposite();
//		TileEntity destinationGeneric = otherPipe.container.getTile(newOrientation);
//
//		if(destinationGeneric instanceof IPipeTile)
//		{
//			TileGenericPipe destination = (TileGenericPipe)destinationGeneric;
//			
//			Position insertPoint = new Position(destination.xCoord + 0.5, destination.yCoord + TransportUtils.getPipeFloorOf(event.item.getItemStack()), destination.zCoord + 0.5, newOrientation.getOpposite());
//			insertPoint.moveForwards(0.5);
//			event.item.setPosition(insertPoint.x, insertPoint.y, insertPoint.z);
//			
//			((PipeTransportItems) destination.pipe.transport).injectItem(event.item, newOrientation);
//			
//		}
//		else if(destinationGeneric instanceof ILPPipeTile)
//		{
//			
//		}
//		else
//		{
//			return;
//		}
			
		Log.debug(event.item + " from " + getPosition() + " to " + otherPipe.getPosition() + " " + newOrientation);
		event.cancelled = true;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return ICON;
	}

	@SuppressWarnings("unchecked")
	public PipeLogisticsTeleport getConnectedPipe()
	{
		List<PipeLogisticsTeleport> connectedPipes = (List<PipeLogisticsTeleport>)((List<?>)TeleportManager.instance.getConnectedPipes(this, true, true));
		if(connectedPipes.size() == 0)
		{
			return null;
		}
		else if(connectedPipes.size() > 1)
		{
			Log.unexpected("This Logistics Teleport Pipe has more than one other pipe on its channel.  Somewhere, somebody messed up!");
			return null;
		}
		
		return connectedPipes.get(0);
	}

	
	/**
	 * Recursive function for checking valid pipelines
	 * @param start
	 * @param sideConnectedToPrevPipe
	 * @return
	 */
	public boolean pipelineEndsinLogisticsPipe(TileEntity start, ForgeDirection sideConnectedToPrevPipe)
	{
		//found one!
		if(start instanceof ILPPipeTile)
		{
			return true;
		}
		
		//another BC pipe
		else if(start instanceof IPipeTile)
		{
			Pipe<?> pipe = (Pipe<?>) ((IPipeTile) start).getPipe();
			if(!BlockGenericPipe.isValid(pipe) || !(pipe.transport instanceof PipeTransportItems))
			{
				//or not?
				return false;
			}
			
			//if it branches off, then this pipeline isn't valid
			if(PipeTransportItemsLogistics.getNumConnectedPipes(pipe.container) != 1)
			{
				return false;
			}
			
			//move to next pipe
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (pipe.container.isPipeConnected(o) || o != sideConnectedToPrevPipe) {
					
					return pipelineEndsinLogisticsPipe(((IPipeTile) start).getNeighborTile(o), o.getOpposite());
				}
			}
		}
		//trying to connect to anything besides a BC or LP pipe
		return false;
	}
}