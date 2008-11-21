package org.makumba.demo;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.catalina.Request;
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
            newTopic.put("author", a.getAttribute("author"));
            topic = db.insert("forum.Topic", newTopic);

        }
        //Let's insert the new post
        newPost.put("forum", a.getAttribute("forum"));
        newPost.put("topic", topic);
        newPost.put("author", a.getAttribute("author"));
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
        
        increaseField(topic, "postCount", db);
        increaseField(forum, "postCount", db);
        
        a.setAttribute("tPtr", topic.toExternalForm());
        
    }

}
  