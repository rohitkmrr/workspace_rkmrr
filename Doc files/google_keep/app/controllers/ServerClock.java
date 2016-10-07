package controllers;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import scala.concurrent.duration.Duration;
public class ServerClock extends UntypedActor {
	
	public static ActorRef serverClock=Akka.system().actorOf(Props.create(ServerClock.class));
	static{
	Akka.system().scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.MINUTES),	serverClock, "varun",Akka.system().dispatcher(),null);
	}
	@Override
	public void onReceive(Object arg0) throws Exception {
		if(arg0.equals("varun")){
			System.out.println(new Date());
			
		}
		else{
			System.out.print("No");
		}
	}

	
}
