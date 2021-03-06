	/**
	 * User access token generated by server and posted 
	 * on to client, used to get and post data of THE USER
	 */
	var token="@Token";
	/**
	 * Interval variable
	 */
	var inter=setInterval(compare,1000);
	/**
	 * Start interval
	 */
	inter;														
	var script_start_time=new Date().getTime();  	/**
													 * get time when script started execution
													 */
	var timings=[];									
	
	/**
	 * global reminder variable
	 */
	var global_reminder_update="";
	
	/**
	 * Get reminders from server and then run compare function
	 */
	function getReminders(){
		$.ajax({
			url:"/getReminders",
			type:"GET",
			headers:{
				"Authorization":token,
				"Content-Type":"application/json"
			},
			success:function(data){
				timings=data;
					
			}
		});	
	}
	
	/**
	 * 	update reminder trigger activity, in database
	 * 	when a reminder is triggered, then it's alerted,
	 *  then it should be marked as inactive in Databse
	 */
	function updateThisReminder(timings,isReminderActive){
		$.ajax({
			url:"/setReminder?postId="+timings['postId']+"&isReminderActive="+isReminderActive,
			type:"GET",
			headers:{
				"Authorization":token,
				"Content-Type":"application/json"
			},
			dataType:'json',
			success:function(data){
				if(isReminderActive==1){
					var x_1=Number.parseInt(timings['reminder']);
					var user_date=new Date(x_1);
					var print_out="Reminder: "+((user_date.getMonth()+1)+"/"+user_date.getDate()+"/"+(1900+user_date.getYear()))+" \n"+timings['title'];
					getReminders();
					if(timings['isArchive']==1){
						window.location.href="main#/archive"
					}
					else{
						window.location.href="main#/home"
					}
					$("#foot_of_card_"+timings['postId']).addClass("viewed");
					alert(print_out);
				}
				else{
					getReminders();
					$("#foot_of_card_"+timings['postId']).addClass("nviewed");
				}
				
			}
		});	
	}
	
	/**
	 * Compare function is used to compare present timestamp to 
	 * the reminder's date and time if times match trigger the event of alert
	 */
	function compare(){
		var current_time=(new Date().getTime());
		var len=timings.length;
		for(var i=0;i<len;i++){
			var x_1=Number.parseInt(timings[i]['reminder']);
			if(x_1>script_start_time&&x_1<=current_time){
				updateThisReminder(timings[i],1);
			}
			else if(x_1<=script_start_time){
				updateThisReminder(timings[i],-1);
			}
		}
	}
	
	
	
	
	
	

	
	/**
	 * Main app with ngRoute dependency
	 */
	var m_app=angular.module("user_main",['ngRoute']);
	/**
	 * routing angularJS application with two views 1)home page 2)archive page
	 * and their respective controllers
	 */
	m_app.config(function($routeProvider){
		$routeProvider
			.when('/home',{templateUrl:'home',controller:'homeController'})
			.when('/archive',{templateUrl:'archive',controller:'archiveController'})
			.otherwise({redirectTo:'/home'});
	});
	
	
	
	
	
	/**
	 * Services Common to all the controllers under main app
	 */
	m_app.service('servicesContainer',function($http){
		
		/**
		 * get notes of a particular user with respect to isArchive flag
		 * isArchive=1 means get archived data
		 * isArchive=0 means get un-archived data
		 */
		this.get_contents=function(isArchive){
			var output=$http.get(
				"@routes.HomeController.getAllPosts"+"?isArchive="+isArchive,
				{
					headers:{
						"Authorization":token,"Content-Type":"application/json"
					}
				}
			)
		//return the $http itself for success and error calls		
		return output;
		};
		
		
		/**
		 * Archive or un-archive a particular post with respect to it's id and flag
		 * id-> id of the post
		 * flag->{ 0: means un-archive, 1: archive }
		 */
		this.archiveThisPost=function(postId,flag){
			var output=$http.post("@routes.HomeController.archivePostById",
			{"postId":postId,"flag":flag},
			{
				headers:{
							"Authorization":token,
							"Content-Type":"application/json"
						}
			}
			);
			return output;
		};
	
	
	});
	
		/**
	 * Common controller to both sub controllers
	 */
	m_app.controller("mainController",function($scope,$interval){
		
		/**
		 * models from pop_up_modal
		 */
		$scope.user_date_g="";
		$scope.user_time_g=""
		$scope.generic_title="";
		$scope.generic_content="";
		global_reminder_update="";
		var i=0;
		/**
		 * Triggered by ng-click on Save reminder button of pop_up_modal
		 */
		$scope.popUpDiv=function(container){
				$("#genericModal").modal('show');
				$scope.generic_title=container['title'];
				$scope.generic_content=container['content'];
				$scope.createdDate="Last edited at "+container['date'];
				$scope.postId=container['postId'];
				global_reminder_update=container['reminder'];
				var user_date_c=Number.parseInt(container['reminder']);
				var date=new Date(user_date_c);
				$scope.user_date_g=(date.getMonth()+1)+"/"+(date.getDate())+"/"+(date.getYear()+1900);
				$scope.user_time_g=(date.getHours()+":"+date.getMinutes());
				$scope.prev_container=container;
			//If current container is equal to previous container -> give perms to update!
		}
		
		
	});
	
	
	
	/**
	 * controller for home page
	 */
	m_app.controller("homeController",function($scope,$http,servicesContainer,$interval){
		
		
		
		/**
		 * Setting anchor to active
		 */
		$("#home").addClass("active");
		$("#archive").removeClass("active");
		
		
		
		
		/**
		 * get posts which are un-archived
		 */
		
		$scope.get_contents=function(isArchive){
			var input=servicesContainer.get_contents(isArchive);
			input.success(function(data,status,headers,config){
				/**
				 * contents is a model in home page
				 * which contains particulars of user's post such as 
				 * 1) id
				 * 2) title of post
				 * 3) content of the post
				 * 4) date of last update
				 * 5) reminder
				 * 6) archive flag
				 */
				$scope.contents=data;	
				
			}).error(function(data,status,headers,config){
				console.log(status);
			});
			
			/**
			 * Call to fetch all active reminders
			 */
			getReminders();
			
		};
		//implicit call to get posts which are marked as un-archive
		$scope.get_contents(0);
		
		/**
		 * archives a particular post with flag 1
		 */
		$scope.archiveThisPost=function(postId,flag){
			if(postId==""){
				var isReminderActive=1;
				if(global_reminder_update!=""){
					isReminderActive=0;
				}
				$scope.post_contents(1,isReminderActive);
			}
			else{
				var input=servicesContainer.archiveThisPost(postId,flag);
				input
				.success(function(data,status,headers,config){
					closeAllDivs();
					//generate a noitification saying post is archived
					$.notify("Note archived","info");
					/**
					 * get the new list of notes that are un-archived since 
					 * the call itself archives a post with id postId
					 */
					$scope.get_contents(0);
				})
				.error(function(data,status,headers,config){
					console.log(status);	
				});	
			}
			
		}
		
		/**
		 * triggered by modal's submission
		 * submits either archived or un-archived post depends which depends on isArchive flag
		 */
		$scope.post_contents=function(isArchive,isReminderActive){
			if($scope.generic_title!=""|$scope.generic_content!=""|global_reminder_update!=""){
				$http.post(
					"@routes.HomeController.addPost",
					{
						"title":$scope.generic_title,"content":$scope.generic_content,"isArchive":isArchive,"reminder":global_reminder_update,"isReminderActive":isReminderActive
					},
					{
						headers:{
								"Authorization":token,"Content-Type":"application/json"
								}
					}
				).success(function(data, status, headers, config){
						if(isArchive==1){
							$.notify("Note archived","success");
							
						}
  						console.log(status);	
  						$scope.get_contents(0);
  					})
  				.error(function(data, status, headers, config){
  					console.log(status+" "+data);
  				});
				$scope.user_date_g="";
				$scope.user_time_g=""
				$scope.generic_title="";
				$scope.generic_content="";
				global_reminder_update="";
				closeAllDivs();
			}
			
		};
		
		
		
		
	
		
		
		$scope.popUpPost=function(){
			$("#genericModal").modal('show');
			$scope.generic_title="";
			$scope.generic_content="";
			$scope.createdDate="";
			$scope.postId="";
			global_reminder_update="";
			$scope.user_date_g="";
			$scope.user_time_g="";
			$scope.prev_container="postId";
		}
		
		
		/**
		 * updateReminder ng-click on a particular post,
		 * pops up a reminder and then updates the reminder if it's valid else nope!
		 */
		$scope.updateReminder=function(container){
			var x=container['postId'];
			var new_date=$("#rem_date_"+x).val();
			var new_time=$("#rem_time_"+x).val();
			var new_date_time=new Date(new_date+" "+new_time).getTime();
			if(new_date_time<=new Date().getTime()){
				$.notify("Oops, the time is gone, we cannot bring it back!","warn");
				return;
			}
			else{
				$http.post(
				"@routes.HomeController.updateReminder",
				{"postId":x,"reminder":new_date_time},
				{
					headers:{
						"Authorization":token,
						"Content-Type":"application/json"
					}
				}
				).success(function(data,status,headers,config){
					$scope.get_contents(0);
					$.notify("Reminder updated","success");
						
			}).error(function(data,status,headers,config){
				console.log(status);
			});
				
			}
		}
		
		
		$scope.save_reminder_local_generic=function(){
			
			var user_gen_date=new Date($scope.user_date_g+" "+$scope.user_time_g).getTime();
			if(user_gen_date<=new Date().getTime()){
				$.notify("Cannot save that reminder","warn");
				return;
			}
			else{
				global_reminder_update=user_gen_date;
				$.notify("Reminder saved","info");
			}
		}
		
		/***
		 * Saves or updates a post depending up on the value of argument "prev_container"
		 * prev_container->{"postId":"Post NEW Data"}
		 * prev_container->{"array of data with postId not null":"Update the content"}
		 */
		$scope.generic_click=function(prev_container){
			if(prev_container=="postId"){
				var title=$scope.generic_title;
				var content=$scope.generic_content;
				var reminder=global_reminder_update;
				var isReminderActive=1;
				if(!isNaN(global_reminder_update)&&global_reminder_update!="") 
					isReminderActive=0;
				$scope.post_contents(0,isReminderActive);
			}
			else{
				if($scope.generic_title!=prev_container['title']|
			$scope.generic_content!=prev_container['content']|
			prev_container['reminder']!=global_reminder_update
			){
				var isReminderActive=1;
				if(prev_container['reminder']!=global_reminder_update&&!isNaN(global_reminder_update)&&global_reminder_update!="") 
					isReminderActive=0;
				$http.post("@routes.HomeController.updatePost",
				{
					"postId":prev_container['postId'],			
					"title":$scope.generic_title,
					"content":$scope.generic_content,
					"reminder":global_reminder_update,
					"isArchive":0,
					"isReminderActive":isReminderActive
				},
				{
					headers:{
						"Authorization":token,
						"Content-Type":"application/json"
					},
				}
				).success(function(data,status,headers,config){
				$scope.get_contents(0);
				$.notify("Note updated","info");
				closeAllDivs();
			}).error(function(data,status,headers,config){
				console.log(status);
			});
			}
				
				
			}
			
			
			
		}
		
	});
	
		
	
	
	
	
	
	/**
	 * Controller of archive page
	 */
	m_app.controller("archiveController",function($scope,$http,servicesContainer){
		$("#home").removeClass("active");
		$("#archive").addClass("active");
		/**
		 * get archived contents of a user with respect to his/her token
		 */
		$scope.get_contents=function(isArchive){
			var input=servicesContainer.get_contents(isArchive);
			input.success(function(data,status,headers,config){
				/**
				 * contents is a model in archive page
				 * which contains particulars of user's post such as 
				 * 1) id
				 * 2) title of post
				 * 3) content of the post
				 * 4) date of last update
				 * 5) reminder
				 * 6) archive flag
				 */
				$scope.contents=data;
				
			}).error(function(data,status,headers,config){
				console.log(status);
			});
			getReminders();
		};
		/**
		 * implict call to get contents of user when the page is loaded
		 * a flag of "1" is specified mentioning "fetch only archived data"
		 */
		$scope.get_contents(1);
		
		/**
		 * un-archives a particular post, triggered by a post's un-archived button
		 *
		 */
		$scope.archiveThisPost=function(postId,flag){
			var input=servicesContainer.archiveThisPost(postId,flag);
			input
			.success(function(data,status,headers,config){
				closeAllDivs();
				//generate a noitification saying post is un-archived
				$.notify("Note un-archived","info");
				/**
				 * get the new list of notes that are archived since 
				 * the call itself un-archives a post with id postId
				 */
				$scope.get_contents(1);
			})
			.error(function(data,status,headers,config){
				console.log(status);	
			});
		}
		/**
		 * archives a particular post with flag 1
		 */
		$scope.archiveThisPost=function(postId,flag){
			
				var input=servicesContainer.archiveThisPost(postId,flag);
				input
				.success(function(data,status,headers,config){
					closeAllDivs();
					//generate a noitification saying post is archived
					$.notify("Note un-archived","info");
					/**
					 * get the new list of notes that are un-archived since 
					 * the call itself archives a post with id postId
					 */
					$scope.get_contents(1);
				})
				.error(function(data,status,headers,config){
					console.log(status);	
				});	
		}
		
		$scope.save_reminder_local_generic=function(){
			
			var user_gen_date=new Date($scope.user_date_g+" "+$scope.user_time_g).getTime();
			if(user_gen_date<=new Date().getTime()){
				$.notify("Cannot save that reminder","warn");
				return;
			}
			else{
				global_reminder_update=user_gen_date;
				$.notify("Reminder saved","info");
			}
		}
		
		$scope.generic_click=function(prev_container){
			if($scope.generic_title!=prev_container['title']|
			$scope.generic_content!=prev_container['content']|
			prev_container['reminder']!=global_reminder_update
			){
					var isReminderActive=1;
					if(prev_container['reminder']!=global_reminder_update&&!isNaN(global_reminder_update)&&global_reminder_update!="") 
						isReminderActive=0;
					$http.post("@routes.HomeController.updatePost",
					{
						"postId":prev_container['postId'],			
						"title":$scope.generic_title,
						"content":$scope.generic_content,
						"reminder":global_reminder_update,
						"isArchive":1,
						"isReminderActive":isReminderActive
					},
					{
						headers:{
							"Authorization":token,
							"Content-Type":"application/json"
						},
					}
					).success(function(data,status,headers,config){
					$scope.get_contents(1);
					$.notify("Note updated","info");
					closeAllDivs();
				}).error(function(data,status,headers,config){
					console.log(status);
				});
			}
		}
		/**
		 * updateReminder ng-click on a particular post,
		 * pops up a reminder and then updates the reminder if it's valid else nope!
		 */
		$scope.updateReminder=function(container){
			var x=container['postId'];
			var new_date=$("#rem_date_"+x).val();
			var new_time=$("#rem_time_"+x).val();
			var new_date_time=new Date(new_date+" "+new_time).getTime();
			if(new_date_time<=new Date().getTime()){
				$.notify("Oops, the time is gone, we cannot bring it back!","warn");
				return;
			}
			else{
				$http.post(
				"@routes.HomeController.updateReminder",
				{"postId":x,"reminder":new_date_time},
				{
					headers:{
						"Authorization":token,
						"Content-Type":"application/json"
					}
				}
				).success(function(data,status,headers,config){
					$scope.get_contents(1);
					$.notify("Reminder updated","success");
						
			}).error(function(data,status,headers,config){
				console.log(status);
			});
				
			}
		}
		
		
	});
	
	
	/**
	 * save_reminder_local_generic
	 */
	
	function closeAllDivs(){
		$("#genericModal").modal("hide");
		$("#formModal").modal("hide");
	}
	
	function addShadow(id){
	$("#"+id).removeClass("light-shadow");
	$("#"+id).addClass('dark-shadow');
	}	
	function undoShadow(id){
		$("#"+id).removeClass('dark-shadow');
		$("#"+id).addClass("light-shadow");
		
	}
	function reminderStateChanger(){
		if($("#reminderFooter_1").css("display")=="none"){
			$("#reminderFooter_1").css("display","block");
		}
		else{
			$("#reminderFooter_1").css("display","none");
		}
	}
	function popUpDate(id){
		$('#'+id).datepicker({minDate:new Date()});$('#'+id).datepicker('show')
	}
	function popUpTime(id){
		$("#"+id).timepicker();
	}
	
