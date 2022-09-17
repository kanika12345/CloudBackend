package com.tweetapp.service;

import com.tweetapp.model.Reply;
import com.google.gson.Gson;

import com.tweetapp.model.Tweet;
import com.tweetapp.model.User;
import com.tweetapp.repository.ReplyRepo;
import com.tweetapp.repository.TweetRepo;
import com.tweetapp.repository.UserRepo;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

@Service
public class UserServiceImpl implements UserService{

	Logger logger=LoggerFactory.getLogger(UserServiceImpl.class);

   @Value("spring.data.mongodb.uri")
	String uri;
	@Value("spring.data.mongodb.database")
	String databasename;
    MongoClient mongoClient =  MongoClients.create(uri);
    MongoDatabase database = mongoClient.getDatabase(databasename);
    MongoCollection<Document> collection = database.getCollection("User");
    
    MongoCollection<Document> collectiontweet = database.getCollection("Tweet");
    MongoCollection<Document> collectionreply = database.getCollection("Reply");
	
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TweetRepo tweetRepo;

    @Autowired
    private ReplyRepo replyRepo;

    @Override
    public User registerUser(User user) {
    	logger.info("===Inside RegisterUser===");
        User newUser = new User();
        try{
            if(user!=null){
                newUser = userRepo.save(user);
                Document document= new Document();
                document.put("_id", user.get_id().toString());
                document.put("firstName", user.getFirstName());
                document.put("lastName", user.getLastName());
                document.put("email", user.getEmail());
                document.put("password", user.getPassword());
                document.put("userName", user.getUserName());
                document.put("gender", user.getGender());
                collection.insertOne(document);
                logger.info("Registration successfully...");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return newUser;
    }

    //all the registered user
    public List<User> listUsers(){
    	List<User> userlist=new ArrayList<User>();
    	Iterable<Document> iterable=collection.find();
    	Iterator<Document> cursor= iterable.iterator();
    	ObjectMapper mapper= new ObjectMapper();
    	 while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	User user;
			try {
				user = mapper.readValue(str, User.class);
				userlist.add(user);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	System.out.println(userlist);
       // return userRepo.findAll();
    	return userlist;
    }

    //username as input -> list of users with that username as output
    @Override
    public List<User> findByUsername(String username) {
        //List<User> users = userRepo.findAll();
    	ObjectMapper mapper= new ObjectMapper();
 
      Iterable<Document> iterable=collection.find(Filters.eq("userName",username));
      Iterator<Document> cursor= iterable.iterator();
        List<User> usersByUname = new ArrayList<>();
      
        while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	User user;
			try {
				user = mapper.readValue(str, User.class);
				usersByUname.add(user);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 
        }

       /* for (int i = 0; i < users.size(); i++) {
            if ((users.get(i).getUserName()).equals(username)) {
                User reqUser=users.get(i);
                usersByUname.add(reqUser);
                
            }
        }*/
        return usersByUname;
    }

    //username & password as input -> success if credentials are right
    @Override
    public User login(String username, String password) {
        User user;
        ObjectMapper mapper= new ObjectMapper();
        Iterable<Document> iterable=collection.find(Filters.eq("userName",username));
        Iterator<Document> cursor= iterable.iterator();
          while(cursor.hasNext()) {
          	 String str = cursor.next().toJson();
  			try {
  				user = mapper.readValue(str, User.class);
  				if(user.getPassword()==password)
  				return user;
  			} catch (JsonMappingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			} catch (JsonProcessingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
          }
  			return null;
        /*try{
            user = userRepo.findUserByUserNameAndPassword(username,password);
            System.out.println(user.getEmail());
            logger.info(username + "--Logged in Successfully..!");
            return user;
        } catch (Exception e){
            e.printStackTrace();
            logger.info("Login failed");
        }
        return null;
        */
    }

    //user id as input -> changes the password of that particular user
    @Override
    public String forgotPassword(String userid,String password) {
    	if(userid!=null && password!=null) {
    	ObjectMapper mapper= new ObjectMapper();
    	 
        Iterable<Document> iterable=collection.find(Filters.eq("_id",userid));
        Iterator<Document> cursor= iterable.iterator();
          while(cursor.hasNext()) {
          	 String str = cursor.next().toJson();
          	User user;
  			try {
  				user = mapper.readValue(str, User.class);
  				try {
  					Bson filter=Filters.eq("_id",userid);
  					Document replacement= new Document();
  					replacement.put("_id", user.get_id());
  					replacement.put("firstName", user.getFirstName());
  					replacement.put("lastName", user.getLastName());
  					replacement.put("email", user.getEmail());
  					replacement.put("password", password);
  					replacement.put("userName", user.getUserName());
  					replacement.put("gender", user.getGender());
  					collection.findOneAndReplace(filter, replacement);
  	                System.out.println(user.getUserName()+" "+user.getPassword());
  	                return "Password updated successfully";
  	            } catch (Exception e) {
  	                e.printStackTrace();
  	            }
  			} catch (JsonMappingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			} catch (JsonProcessingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
          	 
          }
    	}
    	return "insufficient data";

    }
        /*if(userid!=null && password!=null) {
            try {
                User user = userRepo.findUserByUserId(userid);
                user.setPassword(password);
                System.out.println(user.getUserName()+" "+user.getPassword());
                userRepo.save(user);
                return "Password updated successfully";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "insufficient data";
    }*/

    //any authorised user after login can post a tweet
    // tweet content and username as input -> the content is added to list of tweets
    @Override
    public Tweet postTweet(String tweet, String username) {
        Tweet newTweet = new Tweet();
        Document document= new Document();
       
        document.put("tweetContent", tweet);
        document.put("username", username);
        
        newTweet.setTweetContent(tweet);
        newTweet.setUsername(username);
        
      
        int max=10000;
        int min = 100;
        String tweetid = "T" + Integer.toString((int)(Math.random()*(max-min+1)+min));
        newTweet.set_id(tweetid);
        document.put("_id", tweetid.toString());
        LocalDateTime current =  LocalDateTime.now();
        newTweet.setPostTime(current.toString());;
        document.put("postTime", current.toString());
        List<String> likedUsers = new ArrayList<>();
        newTweet.setLikedUsers(likedUsers);
        document.put("likedUsers", likedUsers);
        tweetRepo.save(newTweet);
       InsertOneResult result= collectiontweet.insertOne(document);
        logger.info("Tweet posted by user:"+username);
        
        return newTweet;
    }

    //all tweets
    @Override
    public List<Tweet> getAllTweets() {
    	List<Tweet> tweetlist=new ArrayList<Tweet>();
    	Iterable<Document> iterable=collectiontweet.find();
    	Iterator<Document> cursor= iterable.iterator();
    	ObjectMapper mapper= new ObjectMapper();
    	 while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	Tweet tweet;
			try {
				tweet = mapper.readValue(str, Tweet.class);
				tweetlist.add(tweet);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	//System.out.println("tweet1"+tweetlist);
       // return userRepo.findAll();
    	return tweetlist;
    }
       /* return tweetRepo.findAll();
    }
*/
    // all tweets of particular user
    @Override
    public List<Tweet> getAllTweetsOfUser(String username) {
    	logger.info("Retriving tweets of user: " + username);
    	
    	ObjectMapper mapper= new ObjectMapper();
    	 
        Iterable<Document> iterable=collectiontweet.find(Filters.eq("username",username));
        Iterator<Document> cursor= iterable.iterator();
          List<Tweet> tweetsByUname = new ArrayList<>();
          System.out.println(username);
          while(cursor.hasNext()) {
          	 String str = cursor.next().toJson();
          	Tweet tweet;
  			try {
  				tweet = mapper.readValue(str, Tweet.class);
  				 System.out.println(tweet);
  				tweetsByUname.add(tweet);
  			} catch (JsonMappingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			} catch (JsonProcessingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
          	 
          }
          System.out.println(tweetsByUname);
          return tweetsByUname;
      }

    	/*
        List<Tweet> newTweet = tweetRepo.getTweetsByUsername(username);
        return newTweet;
    }*/

    //logged in user can update tweet by providing the content
    @Override
    public Tweet updateTweet(String id, String content) {
    	ObjectMapper mapper= new ObjectMapper(); 
    	Tweet twee=null;
    	String updatetweet;
        Iterable<Document> iterable=collectiontweet.find(Filters.eq("_id",id));
        Iterator<Document> cursor= iterable.iterator();
          while(cursor.hasNext()) {
          	 String str = cursor.next().toJson();
          	Tweet tweet;
  			try {
  				tweet = mapper.readValue(str, Tweet.class);
   			        System.out.println(content);
  					Bson filter=Filters.eq("_id",id);
  					Document replacement= new Document();
  					replacement.put("_id", tweet.get_id());
  					replacement.put("tweetContent", content);
  					replacement.put("postTime", tweet.getPostTime());
  					replacement.put("likedUsers", tweet.getLikedUsers());
  					replacement.put("username", tweet.getUsername());
  					
  					Document p=collectiontweet.findOneAndReplace(filter, replacement);
  				
  					 updatetweet= p.toJson();
  					 twee=new Gson().fromJson(updatetweet, Tweet.class);
  					//System.out.println("fff"+updatetweet);
  			} catch (JsonMappingException e) {
  				e.printStackTrace();
  			} catch (JsonProcessingException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
          	 
          }
          
    	// Tweet updateTweet = tweetRepo.findTweetById(id);
        // tweet1.setTweetContent(content);
         //tweetRepo.save(tweet1);
         //logger.info("Tweet Updated: ");
         return twee;
    }

    //delete a particular tweet
    @Override
    public String deleteTweet(String id) {
    	DeleteResult deletetweet =collectiontweet.deleteOne(Filters.eq("_id",id));
    	String ret_str;
    	if(deletetweet!=null) {
    		 ret_str = "deleted tweet with id "+id;
    		 logger.info("Deleted the tweet for the tweet id"+id);
    		 System.out.println("Deleted the tweet for the tweet id"+id);
    	}
    	else {
    		   ret_str = "no tweet found with id "+id;
    	       logger.info("No tweet for the tweet id"+id);
    	}
    	return ret_str;
    }
    	/*
        Tweet deleteTweet = tweetRepo.findTweetById(id);
        String ret_str;
        if(deleteTweet!=null){
            tweetRepo.deleteById(id);
            ret_str = "deleted tweet with id "+id;
            logger.info("Deleted the tweet for the tweet id"+id);
        }
        else
            ret_str = "no tweet found with id "+id;
        logger.info("No tweet for the tweet id"+id);
        return ret_str;
    }
    */

    	/*
        Tweet deleteTweet = tweetRepo.findTweetById(id);
        String ret_str;
        if(deleteTweet!=null){
            tweetRepo.deleteById(id);
            ret_str = "deleted tweet with id "+id;
            logger.info("Deleted the tweet for the tweet id"+id);
        }
        else
            ret_str = "no tweet found with id "+id;
        logger.info("No tweet for the tweet id"+id);
        return ret_str;
    }
    */

    // tweet id & username as input -> like status is updated
    @Override
    public List<String> likeTweet(String id, String username) {
    	List<String> likedUsers=new ArrayList<>();
    	 if(id!=null && username!=null){
       Iterable<Document> iterable=collectiontweet.find(Filters.eq("_id",id));
    	Iterator<Document> cursor= iterable.iterator();
    	ObjectMapper mapper= new ObjectMapper();
    	 while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	Tweet tweet;
			try {
				tweet = mapper.readValue(str, Tweet.class);
				 likedUsers =tweet.getLikedUsers();
				likedUsers.add(username);
				Bson filter=Filters.eq("_id",id);
					Document replacement= new Document();
					replacement.put("_id", tweet.get_id());
					replacement.put("tweetContent", tweet.getTweetContent());
					replacement.put("postTime", tweet.getPostTime());
					replacement.put("likedUsers", likedUsers);
					replacement.put("username", tweet.getUsername());
					collectiontweet.findOneAndReplace(filter, replacement);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}}
    	 return likedUsers;
    	 /*
        Tweet tweet = tweetRepo.findTweetById(id);
        List<String> likedUsers  = tweet.getLikedUsers();
        if(id!=null && username!=null){
            likedUsers.add(username);
            logger.info("Tweet liked by user"+username);
        }
        System.out.println(likedUsers);
        tweetRepo.save(tweet);
        return tweet.getLikedUsers();
        */
    }


    //reply to tweet
    @Override
    public Reply postTweetReply(String tweetid, String username, String tweetreply) {
    	
    	 Document document= new Document();
    	
    	 document.put("replyContent", tweetreply);    	
    	 document.put("_rid", tweetid);
    	 document.put("username",username);
    	 
        Reply newReply = new Reply();
        newReply.set_rid(tweetid);
        newReply.setUsername(username);
        newReply.setReplyContent(tweetreply);
        LocalDateTime current =  LocalDateTime.now();
        document.put("replyPostTime", current.toString());
        newReply.setReplyPostTime(current.toString());;
        int max=10000;
        int min = 100;
        String replyid = "Rep" + Integer.toString((int)(Math.random()*(max-min+1)+min));
        newReply.set_id(replyid);;
        document.put("_id", replyid);
        collectionreply.insertOne(document);
        replyRepo.save(newReply);
        return newReply;
    }


    // all replies
    @Override
    public List<Reply> getAllReplies() {
    	List<Reply> repo=new ArrayList<Reply>();
    	Iterable<Document> iterable=collectionreply.find();
    	Iterator<Document> cursor= iterable.iterator();
    	ObjectMapper mapper= new ObjectMapper();
    	 while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	Reply rep;
			try {
				rep = mapper.readValue(str, Reply.class);
				// System.out.println(rep);
				repo.add(rep);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	// System.out.println("tweet"+repo);
    	 return repo;
    	
        //return replyRepo.findAll();
    }

    // returns user specific to the id
    @Override
    public User findByUserId(String userId) {
    	System.out.println(userId);
    	
    	ObjectMapper mapper= new ObjectMapper();
   	
        Iterable<Document> iterable=collection.find(Filters.eq("_id",userId));
        Iterator<Document> cursor= iterable.iterator();
        List<User> usersByUname = new ArrayList<>();
        
        while(cursor.hasNext()) {
        	 String str = cursor.next().toJson();
        	User user;
			try {
				user = mapper.readValue(str, User.class);
				return user;
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 
        }
        return null;
    }
        /*User userById = new User();
        userById = userRepo.findUserByUserId(_id);
        System.out.println(userById);
        if(userById!=null)
            return userById;
        else return null;*/
    
}
