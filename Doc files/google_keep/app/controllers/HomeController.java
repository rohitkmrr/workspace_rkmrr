package controllers;



import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import jdk.nashorn.internal.scripts.JS;

import akka.actor.ActorRef;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.crypto.MACSigner;


import models.AppUsers;
import models.Posts;
import play.data.DynamicForm;
import play.data.Form;

import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.RequestBody;
import scala.App;
import scala.concurrent.java8.FuturesConvertersImpl.P;
import sun.security.provider.SecureRandom;

import views.html.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */


public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render());
    }
    public Result welcome(){
    	response().setHeader("Cache-Control", "no-cache");
    	response().setHeader("Cache-Control", "no-store");
    	if(session("username")!=null)
    		return redirect("main");
    	return ok(welcome.render("ZeMoSo Notes",""));
    }
    
    public Result addUser(){
    	JsonNode jsonNode=request().body().asJson();
    	String username=jsonNode.path("username").asText();
    	String password=jsonNode.path("password").asText();
    	String email=jsonNode.path("email").asText();
    	String hash=BCrypt.hashpw(password, BCrypt.gensalt());
    	String token=UUID.randomUUID().toString();
    	AppUsers users=new AppUsers(username, hash, email,token);
    	
    	try{
    		Ebean.beginTransaction();
    		Ebean.save(users);	
    	}
    	catch(Exception e){
    		Ebean.rollbackTransaction();
    		if(e.toString().contains("PRIMARY")){
    			return badRequest("Username taken");
    			}
    		else if(e.toString().contains("email")){
    			return badRequest("Email id already mapped");
    		}
    		else{
    			return internalServerError("Something went wrong");
    		}
    	}
    	Ebean.commitTransaction();
    	return ok("Successfully registered");

    }
    
    public Result login(){
    	AppUsers appUsers=Form.form(AppUsers.class).bindFromRequest().get();
    	String username=appUsers.getUsername();
    	String password=appUsers.getPassword();
    	try{
	    	AppUsers users=Ebean.find(AppUsers.class).where().eq("username", username).findUnique();
	    	String db_password=users.getPassword();
	    	if(BCrypt.checkpw(password, db_password)){
	    		session("username",username);
	    		return redirect("main");
	    	}
    	}
    	catch(Exception e){
    		
    	}
    	return ok(welcome.render("ZeMoSo Notes, login attempt failed","x"));
    
    		

    	
    }
    public Result newRegister(){
    	return ok(newRegister.render("new users"));
    }
    
    public  Result main(){
    	response().setHeader("Cache-Control", "no-cache");
    	response().setHeader("Cache-Control", "no-store");
    	if(session("username")==null)
    		return redirect("welcome");
    	AppUsers user=Ebean.find(AppUsers.class).where().eq("username", session("username")).findUnique();
    	
    	return ok(main.render(session("username"),user.getToken()));
    }
    
    
    public Result trial(){
    	
    	return ok();
    }
    public Result jwt(){
    	JsonNode jsonNode=request().body().asJson();
    	String text=jsonNode.path("arg").asText();
    	
    	return ok("".toString());
    	
    }
    public Result addPost(){
    	String token=request().getHeader("Authorization");
    	AppUsers appUsers=Ebean.find(AppUsers.class).where().eq("token", token).findUnique();
    	if(appUsers==null)
    		return badRequest("");
    	try{
    		
    		JsonNode jsonNode=request().body().asJson();
	    	String title=jsonNode.path("title").asText();
	    	String content=jsonNode.path("content").asText();
	    	String reminder=jsonNode.path("reminder").asText();
	    	Integer isArchive=jsonNode.path("isArchive").asInt();
	    	Integer isReminderActive=jsonNode.path("isReminderActive").asInt();
	    	Posts posts=new Posts(appUsers, title, content, reminder,isArchive,isReminderActive);
	    	Ebean.beginTransaction();
	    	Ebean.save(posts);
	    	Ebean.commitTransaction();
	    	return ok("");
    	}
    	catch(Exception e){
    		Ebean.rollbackTransaction();
    		return internalServerError(e.toString());
    	}
    }
    public Result getAllPosts(){
    	String token=request().getHeader("Authorization");
    	int isArchive=Integer.parseInt(request().getQueryString("isArchive"));
    	AppUsers appUsers=Ebean.find(AppUsers.class).where().eq("token", token).findUnique();
    	if(appUsers==null)
    		return badRequest("");
    	try{
    		List<Posts> list=Ebean.find(Posts.class).where().eq("appUsers", appUsers).eq("isArchive",isArchive).orderBy("postId desc").findList();
    		return ok(Json.toJson(list));
    	}
    	catch(Exception e){
    		return internalServerError("");
    	}
    	
    }
    public Result getReminders(){
    	String token=request().getHeader("Authorization");
    	
    	AppUsers appUsers=Ebean.find(AppUsers.class).where().eq("token", token).findUnique();
    	if(appUsers==null)
    		return badRequest("");
    	try{
    		List<Posts> list=Ebean.find(Posts.class)
    				.where()
    					.conjunction()
	    					.eq("appUsers", appUsers)
	    					.eq("isReminderActive", 0)
	    					.ne("reminder","")
	    				.endJunction()
    					.orderBy("postId desc")
    					.findList();
    		return ok(Json.toJson(list));
    	}
    	catch(Exception e){
    		return internalServerError("");
    	}
    }
    public Result getPost(){
    	String token=request().getHeader("Authorization");
    	String id=request().getQueryString("id");
    	AppUsers appUsers=Ebean.find(AppUsers.class).where().eq("token", token).findUnique();
    	if(appUsers==null)
    		return badRequest("");
    	try{
    		List<Posts> list=Ebean.find(Posts.class).where().conjunction().eq("appUsers", appUsers).eq("postId",id).findList();
    		
    		return ok(Json.toJson(list));
    	}
    	catch(Exception e){
    		return internalServerError("");
    	}
    }
    public Result archivePostById(){
    	String token=request().getHeader("Authorization");
    	JsonNode jsonNode=request().body().asJson();
    	String postId=jsonNode.path("postId").asText();
    	int flag=jsonNode.path("flag").asInt();
    	try{
    		
	    	Posts posts=Ebean.find(Posts.class)
	    			.where()
	    			.conjunction()
		    			.eq("appUsers", Ebean.find(AppUsers.class).where().eq("token", token).findUnique())
		    			.eq("postId", postId)
	    			.endJunction()
	    			.findUnique();
	    	Ebean.beginTransaction();
	    		posts.setIsArchive(flag);
	    		Ebean.update(posts);
	    	Ebean.commitTransaction();
	    	return ok();
    	}
    	catch(Exception e){
    		Ebean.rollbackTransaction();
    	}
    	return badRequest();
    }
    public Result homePage(){
    	if(session("username")!=null)
    		return ok(home.render());
    	return redirect("welcome");
    }
    public Result archivePage(){
    	if(session("username")!=null)
    		return ok(archive.render());
    	return redirect("welcome");
    }
    
    public Result setReminder(){
    	String postId=request().getQueryString("postId");
    	Integer isReminderActive=Integer.parseInt(request().getQueryString("isReminderActive"));
    	String token=request().getHeader("Authorization");
    	try{
    		
	    	Posts posts=Ebean.find(Posts.class)
	    			.where()
	    			.conjunction()
		    			.eq("appUsers", Ebean.find(AppUsers.class).where().eq("token", token).findUnique())
		    			.eq("postId", postId)
	    			.endJunction()
	    			.findUnique();
	    	Ebean.beginTransaction();
	    		posts.setIsReminderActive(isReminderActive);
	    		Ebean.update(posts);
	    	Ebean.commitTransaction();
	    	return ok("1");
    	}
    	catch(Exception e){
    		Ebean.rollbackTransaction();
    		
    	}
    	return badRequest();
    }
    public Result updateReminder(){
    	String token=request().getHeader("Authorization");
    	JsonNode jsonNode=request().body().asJson();
    	String postId=jsonNode.path("postId").asText();
    	String reminder=jsonNode.path("reminder").asText();
		try{
    		Posts posts=Ebean.find(Posts.class)
	    			.where()
	    			.conjunction()
		    			.eq("appUsers", Ebean.find(AppUsers.class).where().eq("token", token).findUnique())
		    			.eq("postId", postId)
	    			.endJunction()
	    			.findUnique();
    		
	    	Ebean.beginTransaction();
	    		posts.setIsReminderActive(0);
	    		posts.setReminder(reminder);
	    		Ebean.update(posts);
	    	Ebean.commitTransaction();
	    	
	    	return ok("1");
    	}
    	catch(Exception e){
    		Ebean.rollbackTransaction();
    		
    	}
    	return badRequest();
    }
    public Result updatePost(){
    	String token=request().getHeader("Authorization");
    	AppUsers appUsers=Ebean.find(AppUsers.class).where().eq("token", token).findUnique();
    	if(appUsers==null)
    		return badRequest("");
    	try{
    		
    		JsonNode jsonNode=request().body().asJson();
    		String postId=jsonNode.path("postId").asText();
	    	String title=jsonNode.path("title").asText();
	    	String content=jsonNode.path("content").asText();
	    	String reminder=jsonNode.path("reminder").asText();
	    	Integer isArchive=jsonNode.path("isArchive").asInt();
	    	Integer isReminderActive=jsonNode.path("isReminderActive").asInt();
	    	Posts posts=Ebean.find(Posts.class).where()
	    		.conjunction()
	    			.eq("postId", postId)
	    			.eq("appUsers", Ebean.find(AppUsers.class).where().eq("token", token).findUnique())
	    		.endJunction()
	    	.findUnique();
	    	
	    		posts.setTitle(title);
	    		posts.setContent(content);
	    		posts.setIsArchive(isArchive);
	    		posts.setReminder(reminder);
	    		posts.setIsReminderActive(isReminderActive);
	    	Ebean.save(posts);
	    	
	    	return ok("");
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		//Ebean.rollbackTransaction();
    		return internalServerError(e.toString());
    	}
    }
    public Result removeReminder(){
    	String token=request().getHeader("Authorization");
    	JsonNode jsonNode=request().body().asJson();
    	String postId=jsonNode.path("postId").asText();
    	try{
    		Posts posts=Ebean.find(Posts.class)
    			.where()
    				.conjunction()
    					.eq("postId", postId)
    					.eq("appUsers", Ebean.find(AppUsers.class).where().eq("token", token).findUnique())
    				.endJunction()
    				.findUnique();
    		posts.setReminder("");
    		posts.setIsReminderActive(1);
    		Ebean.save(posts);
    		return ok("");
    	}
    	catch(Exception e){
    		return badRequest("");
    	}
    }
    public Result logout(){
    	if(session("username")!=null){
    		session().clear();
    	}
    	return redirect("welcome");
    }
}
