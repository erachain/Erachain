package api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONValue;

import controller.Controller;
import core.blockexplorer.BlockExplorer;
import core.transaction.Transaction;
import database.DBSet;

@Path("assets")
@Produces(MediaType.APPLICATION_JSON)
public class AssetsResource 
{
	@GET
	public String getAseetsLite()
	{
		return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAssetsLite());
	}
	
	@GET
	@Path("/full")	
	public String getAssetsFull()
	{
		return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAssets());
	}	

	@GET
	@Path("/{key}")	
	public String getAssetLite(@PathParam("key") String key)
	{
		Long assetAsLong = null;
		
		// HAS ASSET NUMBERFORMAT
		try {
			assetAsLong = Long.valueOf(key);

		} catch (NumberFormatException e) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ASSET_DOES_NOT_EXIST);

		}

		// DOES ASSETID EXIST
		if (!DBSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ASSET_DOES_NOT_EXIST);

		}
		
		return Controller.getInstance().getAsset(assetAsLong).toJson().toJSONString();
	}
	
	@GET
	@Path("/{key}/full")	
	public String getAsset(@PathParam("key") String key)
	{
		Long assetAsLong = null;
		
		// HAS ASSET NUMBERFORMAT
		try {
			assetAsLong = Long.valueOf(key);

		} catch (NumberFormatException e) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ASSET_DOES_NOT_EXIST);

		}

		// DOES ASSETID EXIST
		if (!DBSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ASSET_DOES_NOT_EXIST);
		}
		
		return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAsset(assetAsLong));
	}	
}
