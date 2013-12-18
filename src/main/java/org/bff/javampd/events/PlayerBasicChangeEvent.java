/*
 * MusicPlayerStatusChangedEvent.java
 *
 * Created on June 22, 2005, 12:32 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.bff.javampd.events;

/**
 * Represents a change in the status of a music player.
 * <p>
 * @author Bill Findeisen
 */
public class PlayerBasicChangeEvent
        extends java.util.EventObject {

    private int id;
    private String msg;

    /**
     * the player was stopped
     */
    public static final int PLAYER_STOPPED = 0;
    /**
     * the player was started
     */
    public static final int PLAYER_STARTED = 1;
    /**
     * the player was paused
     */
    public static final int PLAYER_PAUSED = 2;
    /**
     * the player was taken off pause
     */
    public static final int PLAYER_UNPAUSED = 3;
    /**
     * the players instantaneous bitrate changed
     */
    public static final int PLAYER_BITRATE_CHANGE = 4;

    /**
     * The random playback flag changed (off)
     */
    public static final int PLAYER_RANDOM_OFF = 5;

    /**
     * The random playback flag changed (on)
     */
    public static final int PLAYER_RANDOM_ON = 6;

    /**
     * The repeat flag changed (off)
     */
    public static final int PLAYER_REPEAT_OFF = 7;

    /**
     * The repeat flag changed (on)
     */
    public static final int PLAYER_REPEAT_ON = 8;

    /**
     * The consume mode flag changed (off)
     */
    public static final int PLAYER_CONSUME_OFF = 9;

    /**
     * The consume mode flag changed (on)
     */
    public static final int PLAYER_CONSUME_ON = 10;

    /**
     * The single playback flag changed (off)
     */
    public static final int PLAYER_SINGLE_OFF = 11;

    /**
     * The single playback flag changed (on)
     */
    public static final int PLAYER_SINGLE_ON = 12;

    /**
     * Creates a new instance of PlayerBasicChangeEvent
     * <p>
     * @param source the object on which the Event initially occurred
     * @param id     the specific event that occurred
     */
    public PlayerBasicChangeEvent(Object source, int id) {
        this(source, id, null);
    }

    /**
     * Creates a new instance of PlayerBasicChangeEvent
     * <p>
     * @param source the object on which the Event initially occurred
     * @param id     the specific event that occurred
     * @param msg    an optional message
     */
    public PlayerBasicChangeEvent(Object source, int id, String msg) {
        super(source);
        this.id = id;
        this.msg = msg;
    }

    /**
     * Returns specific id of the event that occurred. The ids are public static
     * fields in the class.
     * <p>
     * @return the specific id
     */
    public int getId() {
        return (id);
    }

    /**
     * Returns the message attached to this event. If there is no message null
     * is returned.
     * <p>
     * @return the optional message
     */
    public String getMsg() {
        return msg;
    }
}
