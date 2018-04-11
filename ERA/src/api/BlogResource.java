package api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.Controller;
import core.crypto.Base58;
import core.naming.Name;
import core.transaction.Transaction;
import core.web.Profile;
import core.web.blog.BlogEntry;
import datachain.DCSet;
import datachain.NameMap;
import utils.BlogUtils;
import webserver.WebResource;

@Path("blog")
@Produces(MediaType.APPLICATION_JSON)
public class BlogResource {
	
	@GET
	public String getBlogList() {
		return getBlogList("Erachain.org");
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/posts/{blogname}")
	public String getBlogList(@PathParam("blogname") String blogname) {

		if(blogname.equals("Erachain.org"))
		{
			blogname = null;
		}
		else
		{
			NameMap nameMap = DCSet.getInstance().getNameMap();
			Name name = nameMap.get(blogname);
			
			if(name == null){
				throw ApiErrorFactory.getInstance().createError(
						Transaction.NAME_DOES_NOT_EXIST);
			}
			
			Profile profile = Profile.getProfileOpt(name);

			if (profile == null || !profile.isProfileEnabled()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BLOG_DISABLED);
			}
		}
		
		List<byte[]> txlist = DCSet.getInstance().getBlogPostMap()
				.get(blogname == null ? "Erachain.org" : blogname);

		JSONArray outputJSON = new JSONArray();
		
		for (byte[] sign : txlist) {
			outputJSON.add(Base58.encode(sign));
		}
		
		return outputJSON.toJSONString();
	}
	
	@GET
	@Path("/post/{signature}")
	public String getBlogPost(@PathParam("signature") String signature) {
		
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
		}
		
		//GET TRANSACTION
		Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
		
		//CHECK IF TRANSACTION EXISTS
		if(transaction == null)
		{
			throw ApiErrorFactory.getInstance().createError(Transaction.TRANSACTION_DOES_NOT_EXIST);
		}
		
		BlogEntry blogEntry = BlogUtils.getBlogEntryOpt(signatureBytes);
	
		if (blogEntry == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_ENTRY_NO_EXISTS);
		}
		
		WebResource.addSharingAndLiking(blogEntry, blogEntry.getSignature());
		
		return blogEntry.toJson().toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/entries/{blogname}/limit/{limit}")
	public String getBlogEntry(@PathParam("blogname") String blogname, @PathParam("limit") int limit) {
	
		JSONObject outputJSON = new JSONObject();
		
		if(blogname.equals("Erachain.org"))
		{
			blogname = null;
		}
		else
		{
			NameMap nameMap = DCSet.getInstance().getNameMap();
			Name name = nameMap.get(blogname);
			
			if(name == null){
				throw ApiErrorFactory.getInstance().createError(
						Transaction.NAME_DOES_NOT_EXIST);
			}
			
			Profile profile = Profile.getProfileOpt(name);

			if (profile == null || !profile.isProfileEnabled()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BLOG_DISABLED);
			}
		}
		
		List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(blogname, limit);
		
		int i = 1;
		
		for (BlogEntry blogEntry : blogPosts) {
			
			WebResource.addSharingAndLiking(blogEntry, blogEntry.getSignature());
			
			outputJSON.put(i, blogEntry.toJson());
			
			i++;
		}
		
		return outputJSON.toJSONString();
	}
	
	@GET
	@Path("/entries")
	public String getBlogEntry() {
		return getBlogEntry("Erachain.org", -1);
	}
	
	@GET
	@Path("/entries/{blogname}")
	public String getBlogEntry(@PathParam("blogname") String blogname) {
		return getBlogEntry(blogname, -1);
	}

	@GET
	@Path("/lastentry")
	public String getLastEntry() {
		return getLastEntry("Erachain.org");
	}
	
	@GET
	@Path("/lastentry/{blogname}")
	public String getLastEntry(@PathParam("blogname") String blogname) {
		if(blogname.equals("Erachain.org"))
		{
			blogname = null;
		}
		else
		{
			NameMap nameMap = DCSet.getInstance().getNameMap();
			Name name = nameMap.get(blogname);
			
			if(name == null){
				throw ApiErrorFactory.getInstance().createError(
						Transaction.NAME_DOES_NOT_EXIST);
			}
			
			Profile profile = Profile.getProfileOpt(name);

			if (profile == null || !profile.isProfileEnabled()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BLOG_DISABLED);
			}
		}
		
		List<byte[]> txlist = DCSet.getInstance().getBlogPostMap()
				.get(blogname == null ? "Erachain.org" : blogname);
		
		if(txlist.isEmpty())
		{
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_EMPTY);
		}
		
		BlogEntry blogEntry = BlogUtils.getBlogEntryOpt(txlist.get(txlist.size()-1));
		
		if (blogEntry == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_ENTRY_NO_EXISTS);
		}
		
		return blogEntry.toJson().toJSONString();
	}
}
