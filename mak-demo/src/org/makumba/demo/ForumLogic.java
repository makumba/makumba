package org.makumba.demo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.Pointer;

/**
 * Makumba Java Business Logic
 * @author Jasper van Bourgognie
 *
 */
public class ForumLogic extends Logic {
    public void insertNewForumPost(Dictionary d, Attributes a, Database db) throws LogicException {
        //Check if the user is not flooding the forum
        Integer timeSeparation = 10; //Minimum time between 2 postst by the same user in seconds
        Date now = new Date();
        now.setTime(now.getTime() - timeSeparation*1000);
        Vector results = db.executeQuery("SELECT p FROM forum.Post p WHERE p.TS_create > $1 AND p.author = $2", new Object[] {new Timestamp(now.getTime()), a.getAttribute("loggedInUser")});
        if(results.size()>0) {
            throw new LogicException ("You can not post more than 1 message per "+ timeSeparation +" seconds");
        }
        else if (a.hasAttribute("loggedInUser") && a.getAttribute("loggedInUser") != null) {
    
            //Do we have a parent post?
            Pointer forum = new Pointer("forum.Forum", (String) a.getAttribute("forum"));
            Pointer topic;
            Pointer post;
            Dictionary newPost = new Hashtable();
            Dictionary newTopic = new Hashtable();
            
            if (a.hasAttribute("parent")) {
                //If yes, find out what its topic pointer is
                Vector result = db.executeQuery("SELECT p as parent, p.topic as topic FROM forum.Post p WHERE p=$1", a.getAttribute("parent"));
                if (result.size()==1) {
                    Dictionary parentPost = (Dictionary) result.firstElement();
                    topic = (Pointer) parentPost.get("topic");
                    newPost.put("parent", parentPost.get("parent"));
                } else {
                    throw new LogicException("Couldn't find parent post, so cannot insert this as a reply. Looking for post with PTR: " + a.getAttribute("parent"));
                }
            } else {
                increaseField(forum, "threadCount", db);
                newTopic.put("forum", a.getAttribute("forum"));
                newTopic.put("title", a.getAttribute("title"));
                newTopic.put("sticky", 0); //not sticky by default
                newTopic.put("postCount", 0);
                newTopic.put("author", a.getAttribute("loggedInUser"));
                topic = db.insert("forum.Topic", newTopic);
    
            }
            //Let's insert the new post
            newPost.put("forum", a.getAttribute("forum"));
            newPost.put("topic", topic);
            newPost.put("author", a.getAttribute("loggedInUser"));
            newPost.put("contents", a.getAttribute("contents"));
            newPost.put("hidden", 0); //not sticky by default
            post = db.insert("forum.Post", newPost);
            //Update the topic with the new post
            newTopic = new Hashtable();
            //If it was a new post, tell this to the topic
            if (!a.hasAttribute("parent")) {
                newTopic.put("topPost", post);
            }
            newTopic.put("lastPost", post);
            newTopic.put("lastPostDate", new java.util.Date());
            db.update(topic, newTopic);
            db.update("forum.Forum f", "f.lastPost=$1", "f=$2", new Object[] {post, a.getAttribute("forum")});
            increaseField(topic, "postCount", db);
            increaseField(forum, "postCount", db);
            
        }
        else {
            throw new LogicException("Please log in before making a post.");
        }
    }
    public void on_addForumForumAdmins(Pointer forum, Dictionary d, Attributes a, Database db) throws LogicException {
        System.out.println("kak");
    }

}
  