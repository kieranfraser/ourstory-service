package ie.fraser.findings.models;

import java.util.ArrayList;

/**
 * Created by Kieran on 28/07/2017.
 */

public class StoryInterceptedNotification {

    private String id; // the post time of the notification (uniquely identifies the notification)


    // From statusBarNotification

    private int notifyId; // the id supplied to notify(id, notification)
    private String key; // unique instance key for this notification record (android)

    private String appPackage; // package of app that posted the notification
    private long postTime; // the time the notification was posted (more accurate than "when")
    private String tag; // the tag that was supplied to notify(id, notification), may be null
    private boolean isClearable; // Method to check if the notification flags (ongoing, no-clear)
    private boolean isOngoing; // Check for ongoing event

    // From notification in getNotification from statusBarNotification

    private String groupId; // key used to group notification into cluster
    private String sortKey; // key that orders notifications among others from same package

    // Extras to be extracted from listener service

    private String bigText; // longer text shown in the big form as supplied to bigText(CharSequence)
    private ArrayList<Integer> compactActions; // indices of actions to be shown in compact view setShowActionsInCompactView(int)
    private String infoText; // small piece of additional text supplied to setContentInfo(CharSequence)
    //private String[] people; // string array containing people associated with notification
    private String picture; // bitmap to be shown in expanded notifications bigPicture(bitmap)
    private boolean showChronometer; // whether when should be count-up timer
    private boolean showWhen; // whether when should be shown
    private String smallIcon; // deprecated api 26..
    private String subText; // third line of text
    private String summaryText; // summary info shown alongside expanded notifications.
    private String text; // main text payload
    private String textLines; // array of charsequences in expanded notification.
    private String title; // the title of the notification
    private String titleBig; // the of the notification in expanded form

    // public fields

    //private String[] actions; // actions containing pending intents.. may be necessary
    private String category; // category from predefined list
    private int color; // argb integer
    private String contentIntent; // may be needed for next action
    private String deleteIntent; // may be needed for next action
    private int iconLevel; // if has more than 1
    private int ledARGB; // deprecated in 26
    private int ledOffMS; // deprecated in 26
    private int ledOnMS; // deprecated in 26
    private int priority; // deprecated in 26
    //private long[] vibrate; // deprecated in 26 - vibration pattern
    private int visibility; // affects when and how system ui reveals the notification
    public long whenTimestamp; // in milliseconds since the epoch
    private String tickerText;

    // Custom fields
    private long notificationRemoved;   // calculated in onNotificationRemoved
    private boolean notificationClicked;    // calculated using usage stats
    private ArrayList<Long> updateTimeHistory;  // history of times the notification is updated (starts with original postTime)
    private long appLastUsed;
    private boolean needClarification;

    public boolean isNeedClarification() {
        return needClarification;
    }

    public void setNeedClarification(boolean needClarification) {
        this.needClarification = needClarification;
    }

    public long getAppLastUsed() {
        return appLastUsed;
    }

    public void setAppLastUsed(long appLastUsed) {
        this.appLastUsed = appLastUsed;
    }

    public ArrayList<Long> getUpdateTimeHistory() {
        return updateTimeHistory;
    }

    public void setUpdateTimeHistory(ArrayList<Long> updateTimeHistory) {
        this.updateTimeHistory = updateTimeHistory;
    }

    public long getNotificationRemoved() {
        return notificationRemoved;
    }

    public void setNotificationRemoved(long notificationRemoved) {
        this.notificationRemoved = notificationRemoved;
    }

    public boolean isNotificationClicked() {
        return notificationClicked;
    }

    public void setNotificationClicked(boolean notificationClicked) {
        this.notificationClicked = notificationClicked;
    }


    /*public String[] getActions() {
        return actions;
    }

    public void setActions(String[] actions) {
        this.actions = actions;
    }*/

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getContentIntent() {
        return contentIntent;
    }

    public void setContentIntent(String contentIntent) {
        this.contentIntent = contentIntent;
    }

    public String getDeleteIntent() {
        return deleteIntent;
    }

    public void setDeleteIntent(String deleteIntent) {
        this.deleteIntent = deleteIntent;
    }

    public int getIconLevel() {
        return iconLevel;
    }

    public void setIconLevel(int iconLevel) {
        this.iconLevel = iconLevel;
    }

    public int getLedARGB() {
        return ledARGB;
    }

    public void setLedARGB(int ledARGB) {
        this.ledARGB = ledARGB;
    }

    public int getLedOffMS() {
        return ledOffMS;
    }

    public void setLedOffMS(int ledOffMS) {
        this.ledOffMS = ledOffMS;
    }

    public int getLedOnMS() {
        return ledOnMS;
    }

    public void setLedOnMS(int ledOnMS) {
        this.ledOnMS = ledOnMS;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /*public long[] getVibrate() {
        return vibrate;
    }

    public void setVibrate(long[] vibrate) {
        this.vibrate = vibrate;
    }*/

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public long getWhenTimestamp() {
        return whenTimestamp;
    }

    public void setWhenTimestamp(long whenTimestamp) {
        this.whenTimestamp = whenTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getTickerText() {
        return tickerText;
    }

    public void setTickerText(String tickerText) {
        this.tickerText = tickerText;
    }

    @Override
    public String toString() {
        return "App Package: "+this.appPackage+", Ticker: "+this.tickerText;
    }

    public int getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(int notifyId) {
        this.notifyId = notifyId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isClearable() {
        return isClearable;
    }

    public void setClearable(boolean clearable) {
        isClearable = clearable;
    }

    public boolean isOngoing() {
        return isOngoing;
    }

    public void setOngoing(boolean ongoing) {
        isOngoing = ongoing;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getBigText() {
        return bigText;
    }

    public void setBigText(String bigText) {
        this.bigText = bigText;
    }

    public ArrayList<Integer> getCompactActions() {
        return compactActions;
    }

    public void setCompactActions(ArrayList<Integer> compactActions) {
        this.compactActions = compactActions;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    /*public String[] getPeople() {
        return people;
    }

    public void setPeople(String[] people) {
        this.people = people;
    }*/

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isShowChronometer() {
        return showChronometer;
    }

    public void setShowChronometer(boolean showChronometer) {
        this.showChronometer = showChronometer;
    }

    public boolean isShowWhen() {
        return showWhen;
    }

    public void setShowWhen(boolean showWhen) {
        this.showWhen = showWhen;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextLines() {
        return textLines;
    }

    public void setTextLines(String textLines) {
        this.textLines = textLines;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleBig() {
        return titleBig;
    }

    public void setTitleBig(String titleBig) {
        this.titleBig = titleBig;
    }
}