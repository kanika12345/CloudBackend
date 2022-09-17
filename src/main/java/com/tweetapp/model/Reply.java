package com.tweetapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Reply")
public class Reply {

    @Id
    private String _id;
    private String replyContent;
    private String replyPostTime;
    private String _rid;
    private String username;

    
    public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getReplyPostTime() {
		return replyPostTime;
	}

	public void setReplyPostTime(String replyPostTime) {
		this.replyPostTime = replyPostTime;
	}

	public String get_rid() {
		return _rid;
	}

	public void set_rid(String _rid) {
		this._rid = _rid;
	}

	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
