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
import org.makumba.MakumbaSystem;
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
    public void on_deleteForumForum (Pointer forum, Attributes a, Database db) throws LogicException {
        //TODO: add some check if the person is really super admin or so
        //Delete all posts and topics from this forum
        Vector posts = db.executeQuery("SELECT p as post FROM forum.Post p WHERE p.forum=$1", forum);
        Vector topics = db.executeQuery("SELECT t as topic FROM forum.Topic t WHERE t.forum=$1", forum);
        for (Enumeration e=posts.elements();e.hasMoreElements();) {
            Dictionary f = (Dictionary) e.nextElement();
            db.delete((Pointer)f.get("post"));
        }
        for (Enumeration e=topics.elements();e.hasMoreElements();) {
            Dictionary f = (Dictionary) e.nextElement();
            db.delete((Pointer)f.get("topic"));
        }
        System.out.println("deleted " + topics.size() + " topics and " + posts.size() + " topics");
    }
    public void on_addForumForumAdmins(Pointer forum, Dictionary d, Attributes a, Database db) throws LogicException {
        Vector admins = db.executeQuery("SELECT adm.user as adm FROM forum.Forum f, f.admins adm WHERE f=$1 ", forum); 
        for (Enumeration e=admins.elements();e.hasMoreElements();) {
            Dictionary f = (Dictionary) e.nextElement();
            if (((Pointer)a.getAttribute("user")).equals((Pointer)f.get("adm"))) {
                throw new LogicException("Trying to add the same user twice as admin. No can do!");
            } 
        }
    }
    public void deleteTopic(Dictionary d, Attributes a, Database db) throws LogicException {
        Vector admins = db.executeQuery("SELECT adm.user as adm FROM forum.Forum f, f.admins adm WHERE f=$1 AND adm.user=$2 ", new Object[] {a.getAttribute("forum"),a.getAttribute("loggedInUser") });
        if(admins.size()==1) {
            Pointer forum = (Pointer) a.getAttribute("forum");
            Vector posts = db.executeQuery("SELECT p as post FROM forum.Post p WHERE p.topic=$1", a.getAttribute("topic"));
            addToField(forum, "postCount", db, posts.size()*-1);
            addToField(forum, "threadCount", db, -1);
            for (Enumeration e=posts.elements();e.hasMoreElements();) {
                Dictionary f = (Dictionary) e.nextElement();
                db.delete((Pointer)f.get("post"));
            }
            db.delete((Pointer)a.getAttribute("topic"));
        } else {
            throw new LogicException("Trying to delete a topic here, but you're not a forum admin!");
        }
    }
    public void on_editForumPost(Pointer post, Dictionary d, Attributes a, Database db) throws LogicException {
        
        if (a.hasAttribute("hidden")) {
            Vector admins = db.executeQuery("SELECT adm.user as adm FROM forum.Forum f, f.admins adm, forum.Post p WHERE p.forum=f AND p=$1 AND adm.user=$2 ", new Object[] {post,a.getAttribute("loggedInUser") });
            if(admins.size()<1) {
                throw new LogicException ("You're not an admin of this forum. you shouldn't be messing around with the forum too!");
            }
            else if(admins.size()>1) {
                throw new LogicException ("There's an inconsistency in the administrators of the forum you're trying to edit");
            } else {
                d.put("hidden", a.getAttribute("hidden"));
            }
        } else {
           throw new LogicException ("How on earth did this method get called? This won't do anything!");
        }
    }
    
    public void on_editForumTopic(Pointer topic, Dictionary d, Attributes a, Database db) throws LogicException {
        
        if (a.hasAttribute("sticky")) {
            Vector admins = db.executeQuery("SELECT adm.user as adm FROM forum.Forum f, f.admins adm, forum.Topic t WHERE t.forum=f AND t=$1 AND adm.user=$2 ", new Object[] {topic ,a.getAttribute("loggedInUser") });
            if(admins.size()<1) {
                throw new LogicException ("You're not an admin of this forum. you shouldn't be messing around with the forum too!");
            }
            else if(admins.size()>1) {
                throw new LogicException ("There's an inconsistency in the administrators of the forum you're trying to edit");
            } else {
                d.put("sticky", a.getAttribute("sticky"));
            }
        } else {
           throw new LogicException ("How on earth did this method get called? This won't do anything!");
        }
    }

}
  