package webserver;

import com.google.gson.Gson;
import controller.Controller;
import core.item.assets.Trade;
import utils.StrJSonFine;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("apitrade")
@Produces(MediaType.APPLICATION_JSON)
public class API_Trade {

	@Context
	HttpServletRequest request;

	@GET
	public Response Default() {
		Map<String, String> help = new LinkedHashMap<>();

		help.put("apitrade/get?have={have}&want={want}&timestamp={timestamp}&limit={limit}",
				"Get data by trade. address is account, recipient is account two, timestamp is value time, "
						+ "limit is count record. The number of transactions is limited by input param.");

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
	}

	/**
	 * Get trades by timestamp. The number of transactions is limited by input
	 * param.
	 * 
	 * @author Ruslan
	 * @param have
	 *            is account
	 * @param want
	 *            is account two
	 * @param timestamp
	 *            value time
	 * @param limit
	 *            count out record
	 * @return record trades
	 */
	
	@GET
	@Path("get")
	// apitrade/get?have=1&want=2&timestamp=3&limit=4
	public Response getTradeByAccount(@QueryParam("have") String have, @QueryParam("want") String want,
			@QueryParam("timestamp") String timestamp, @QueryParam("limit") String limit)  {

		List<Trade> listRusult = Controller.getInstance().getTradeByTimestmp(Long.parseLong(have), Long.parseLong(want),
				Long.parseLong(timestamp));
		
		List<Trade> tradeList = listRusult.subList(0,Integer.parseInt(limit));

		Gson gs= new Gson();
		String result= gs.toJson(tradeList);
				 
		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(result).build();
	}
}