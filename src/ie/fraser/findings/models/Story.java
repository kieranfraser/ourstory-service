package ie.fraser.findings.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kieran on 29/07/2017.
 */

public class Story implements Serializable{

    private String id;
    private StoryContact.App app;
    private String summary;
    private ArrayList<String> topics;
    private ArrayList<KeywordVisual> keywordVisuals;

    public Story(){};

    public Story(String id, StoryContact.App app, String summary){
        this.id = id;
        this.app = app;
        this.summary = summary;
        this.topics = new ArrayList<String>();
        this.keywordVisuals = new ArrayList<KeywordVisual>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StoryContact.App getApp() {
        return app;
    }

    public void setApp(StoryContact.App app) {
        this.app = app;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<String> topics) {
        this.topics = topics;
    }

    public ArrayList<KeywordVisual> getKeywordVisuals() {
        return keywordVisuals;
    }

    public void setKeywordVisuals(ArrayList<KeywordVisual> keywordVisuals) {
        this.keywordVisuals = keywordVisuals;
    }

    public String getRandomIconForStory(){
        List<String> icons = new ArrayList<String>();
        if(this.keywordVisuals!=null){
            for(KeywordVisual visual: this.keywordVisuals){
                icons.add(visual.getRandomIcon());
            }
        }
        if(icons.size()>0)
            return icons.get(new Random().nextInt(icons.size()));
        else
            return null;
    }
}
