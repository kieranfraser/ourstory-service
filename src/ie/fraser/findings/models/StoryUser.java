package ie.fraser.findings.models;

import java.util.ArrayList;

/**
 * Created by Kieran on 26/06/2017.
 */

public class StoryUser {

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    private String id;
    private String email;
    private Gender gender;
    private int age;
    private String name;
    private StoryOptions options;
    private ArrayList<StoryContact> phoneContacts;
    private ArrayList<StoryContact> facebookContacts;
    private ArrayList<StoryContact> twitterContacts;

    public StoryUser(){}
    
    public StoryUser(String id, String email, Gender gender, int age, String name,  StoryOptions options){
        this.id = id;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.name = name;
        this.options = options;

        this.phoneContacts = new ArrayList<StoryContact>();
        this.facebookContacts = new ArrayList<StoryContact>();
        this.twitterContacts = new ArrayList<StoryContact>();
    }

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

    public StoryOptions getOptions() {
        return options;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public ArrayList<StoryContact> getPhoneContacts() {
        return phoneContacts;
    }

    public ArrayList<StoryContact> getFacebookContacts() {
        return facebookContacts;
    }

    public void setFacebookContacts(ArrayList<StoryContact> facebookContacts) {
        this.facebookContacts = facebookContacts;
    }

    public ArrayList<StoryContact> getTwitterContacts() {
        return twitterContacts;
    }

    public void setTwitterContacts(ArrayList<StoryContact> twitterContacts) {
        this.twitterContacts = twitterContacts;
    }

    public void updatePhoneContact(StoryContact contact){
        for(StoryContact c: this.phoneContacts){
            if(c.getId().trim()==contact.getId().trim()){
                c = contact;
            }
        }
    }

    public void updateFacebookContact(StoryContact contact){
        for(StoryContact c: this.facebookContacts){
            if(c.getId().trim()==contact.getId().trim()){
                c = contact;
            }
        }
    }

    public void updateTwitterContact(StoryContact contact){
        for(StoryContact c: this.twitterContacts){
            if(c.getId().trim()==contact.getId().trim()){
                c = contact;
            }
        }
    }

    public StoryContact searchForContactGivenId(String id){
        for(StoryContact c: this.phoneContacts){
            if(c.getId().trim().equals(id.trim()))
                return c;
        }
        for(StoryContact c: this.facebookContacts){
            if(c.getId().trim().equals(id.trim()))
                return c;
        }
        for(StoryContact c: this.twitterContacts){
            if(c.getId().trim().equals(id.trim()))
                return c;
        }
        return null;
    }

    public void updateConsent(boolean isConsentGiven){
        this.options.setConsent(isConsentGiven);
    }

}