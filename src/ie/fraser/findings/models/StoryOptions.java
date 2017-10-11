package ie.fraser.findings.models;

/**
 * Created by Kieran on 28/06/2017.
 */

public class StoryOptions {

    private boolean intro;
    private boolean consent;
    private boolean optIn;
    private boolean facebookOn;
    private boolean twitterOn;

    public StoryOptions(){
        this.facebookOn = false;
        this.twitterOn = false;
        this.intro = true;
        this.consent = false;
        this.optIn = false;
    }

    public StoryOptions(boolean facebookOn, boolean twitterOn, boolean intro, boolean consent, boolean optIn){
        this.facebookOn = facebookOn;
        this.twitterOn = twitterOn;
        this.intro = intro;
        this.consent = consent;
        this.optIn = optIn;
    }

    public boolean isFacebookOn() {
        return facebookOn;
    }

    public void setFacebookOn(boolean facebookOn) {
        this.facebookOn = facebookOn;
    }

    public boolean isTwitterOn() {
        return twitterOn;
    }

    public void setTwitterOn(boolean twitterOn) {
        this.twitterOn = twitterOn;
    }

    public boolean isIntro() {
        return intro;
    }

    public void setIntro(boolean intro) {
        this.intro = intro;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    public boolean isOptIn() {
        return optIn;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }
}
