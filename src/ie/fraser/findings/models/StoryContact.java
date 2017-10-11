package ie.fraser.findings.models;

import java.io.Serializable;

/**
 * Created by Kieran on 26/06/2017.
 */

public class StoryContact implements Serializable{

    public enum App{
        PHONE, FACEBOOK, TWITTER,;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private String id;
    private String name;
    private App associateApp;
    private String photoRef;
    private int totalStories;
    private String lastCommunication;

    public StoryContact(){};

    public StoryContact(String id, String name, App associateApp){
        this.id = id;
        this.name = name;
        this.associateApp = associateApp;
        this.totalStories = 0;
        this.lastCommunication = "";
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public App getAssociateApp() {
        return associateApp;
    }

    public void setAssociateApp(App associateApp) {
        this.associateApp = associateApp;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public void setPhotoRef(String photoRef) {
        this.photoRef = photoRef;
    }

    public int getTotalStories() {
        return totalStories;
    }

    public void setTotalStories(int totalStories) {
        this.totalStories = totalStories;
    }

    public String getLastCommunication() {
        return lastCommunication;
    }

    public void setLastCommunication(String lastCommunication) {
        this.lastCommunication = lastCommunication;
    }

    /*public void addStory(Story story){
        if(this.stories!=null){
            this.stories.add(story);
        }
        else{
            this.stories = new ArrayList<>();
            this.stories.add(story);
        }
    }

    public List<String> getExploreStoryIcons(){
        List<String> storyIcons = new ArrayList<>();
        if(this.stories!=null){
            for(Story story: this.stories){
                String icon = story.getRandomIconForStory();
                if(icon!=null)
                    storyIcons.add(icon);
            }
        }
        return storyIcons;
    }*/
}
