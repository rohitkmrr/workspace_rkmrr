package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model;

@Entity
public class Posts extends Model{
	@Id
	private int postId;
	
	@ManyToOne
	private AppUsers appUsers;
	
	@Column(columnDefinition="TEXT")
	private String content;
	
	@Column(columnDefinition="TEXT")
	private String title;
	
	@Column(name="timestamp",columnDefinition="timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private String date;
	
	private String reminder;
	
	@Column(columnDefinition="int default 0")
	private Integer isReminderActive;
	
	@Column(columnDefinition="int default 0")
	private Integer isArchive; 
	
	
	public Posts(AppUsers appUsers,String title,String content,String reminder,Integer isArchive,Integer isReminderActive) {
		this.setAppUsers(appUsers);
		this.setTitle(title);
		this.setContent(content);
		this.setReminder(reminder);
		this.setIsArchive(isArchive);
		this.setIsReminderActive(isReminderActive);
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}

	public Integer getIsArchive() {
		return isArchive;
	}

	public void setIsArchive(Integer isArchive) {
		this.isArchive = isArchive;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/*public AppUsers getAppUsers() {
		return appUsers;
	}*/

	public void setAppUsers(AppUsers appUsers) {
		this.appUsers = appUsers;
	}

	public Integer getIsReminderActive() {
		return isReminderActive;
	}

	public void setIsReminderActive(Integer isReminderActive) {
		this.isReminderActive = isReminderActive;
	}
	
	//public static Finder<Long, Posts> find=new Finder<Long, Posts>(Posts.class);
	
}