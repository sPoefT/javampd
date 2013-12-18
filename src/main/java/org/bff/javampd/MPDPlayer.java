/*
 * MPDPlayer.java
 *
 * Created on September 29, 2005, 3:42 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.bff.javampd;

import org.bff.javampd.events.PlayerChangeEvent;
import org.bff.javampd.events.PlayerChangeListener;
import org.bff.javampd.events.VolumeChangeEvent;
import org.bff.javampd.events.VolumeChangeListener;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDPlayerException;
import org.bff.javampd.exception.MPDResponseException;
import org.bff.javampd.objects.MPDAudioInfo;
import org.bff.javampd.objects.MPDSong;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MPDPlayer represents a player controller to a MPD server. To obtain an
 * instance of the class you must use the <code>getMPDPlayer</code> method from
 * the {@link MPD} connection class. This class does not have a public
 * constructor (singleton model) so the object must be obtained from the
 * connection object.
 * <p>
 * @author Bill Findeisen
 * @version 1.0
 */
public class MPDPlayer {

    private MPD mpd;
    private Properties prop;
    private int oldVolume;
    private List<PlayerChangeListener> listeners = new ArrayList<PlayerChangeListener>();
    private List<VolumeChangeListener> volListeners = new ArrayList<VolumeChangeListener>();
    //properties value constants
    private static final String MPDPROPXFADE = "MPD_PLAYER_CROSSFADE";
    private static final String MPDPROPCURRSONG = "MPD_PLAYER_CURRENTSONG";
    private static final String MPDPROPNEXT = "MPD_PLAYER_NEXT";
    private static final String MPDPROPPAUSE = "MPD_PLAYER_PAUSE";
    private static final String MPDPROPPLAY = "MPD_PLAYER_PLAY";
    private static final String MPDPROPPLAYID = "MPD_PLAYER_PLAY_ID";
    private static final String MPDPROPPREV = "MPD_PLAYER_PREV";
    private static final String MPDPROPREPEAT = "MPD_PLAYER_REPEAT";
    private static final String MPDPROPRANDOM = "MPD_PLAYER_RANDOM";
    private static final String MPDPROPCONSUME = "MPD_PLAYER_CONSUME";
    private static final String MPDPROPSINGLE = "MPD_PLAYER_SINGLE";
    private static final String MPDPROPSEEK = "MPD_PLAYER_SEEK";
    private static final String MPDPROPSEEKID = "MPD_PLAYER_SEEK_ID";
    private static final String MPDPROPSTOP = "MPD_PLAYER_STOP";
    private static final String MPDPROPSETVOL = "MPD_PLAYER_SET_VOLUME";

    /**
     * The status of the player.
     */
    public enum PlayerStatus {

        /**
         * player stopped status
         */
        STATUS_STOPPED,
        /**
         * player playing status
         */
        STATUS_PLAYING,
        /**
         * player paused status
         */
        STATUS_PAUSED,
    }

    private PlayerStatus status = PlayerStatus.STATUS_STOPPED;

    /**
     * Creates a new instance of MPDPlayer
     * <p>
     * @param mpd the MPD connection
     */
    MPDPlayer(MPD mpd) {
        this.mpd = mpd;
        this.prop = mpd.getMPDProperties();
    }

    /**
     * Returns the current song either playing or queued for playing.
     * <p>
     * @return the current song
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public MPDSong getCurrentSong() throws MPDConnectionException, MPDPlayerException {
        MPDCommand command
                = new MPDCommand(prop.getProperty(MPDPROPCURRSONG));

        try {
            List<String> response
                    = new ArrayList<String>(mpd.sendMPDCommand(command));

            List<MPDSong> songList = new ArrayList<MPDSong>(mpd.convertResponseToSong(response));

            if (songList.size() == 0) {
                return (null);
            } else {
                return (songList.get(0));
            }
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        } catch (Exception e) {
            throw new MPDPlayerException(e);
        }
    }

    /**
     * Adds a {@link PlayerChangeListener} to this object to receive
     * {@link PlayerChangeEvent}s.
     * <p>
     * @param pcl the PlayerChangeListener to add
     */
    public synchronized void addPlayerChangeListener(PlayerChangeListener pcl) {
        listeners.add(pcl);
    }

    /**
     * Removes a {@link PlayerChangeListener} from this object.
     * <p>
     * @param pcl the PlayerChangeListener to remove
     */
    public synchronized void removePlayerChangedListener(PlayerChangeListener pcl) {
        listeners.remove(pcl);
    }

    /**
     * Sends the appropriate {@link PlayerChangeEvent} to all registered
     * {@link PlayerChangeListener}s.
     * <p>
     * @param id the event id to send
     */
    protected synchronized void firePlayerChangeEvent(int id) {
        PlayerChangeEvent pce = new PlayerChangeEvent(this, id);

        for (PlayerChangeListener pcl : listeners) {
            pcl.playerChanged(pce);
        }
    }

    /**
     * Adds a {@link VolumeChangeListener} to this object to receive
     * {@link VolumeChangeEvent}s.
     * <p>
     * @param vcl the VolumeChangeListener to add
     */
    public synchronized void addVolumeChangeListener(VolumeChangeListener vcl) {
        volListeners.add(vcl);
    }

    /**
     * Removes a {@link VolumeChangeListener} from this object.
     * <p>
     * @param vcl the VolumeChangeListener to remove
     */
    public synchronized void removeVolumeChangedListener(VolumeChangeListener vcl) {
        volListeners.remove(vcl);
    }

    /**
     * Sends the appropriate {@link VolumeChangeEvent} to all registered
     * {@link VolumeChangeListener}.
     * <p>
     * @param volume the new volume
     */
    protected synchronized void fireVolumeChangeEvent(int volume) {
        VolumeChangeEvent vce = new VolumeChangeEvent(this, volume);

        for (VolumeChangeListener vcl : volListeners) {
            vcl.volumeChanged(vce);
        }
    }

    /**
     * Starts the player.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void play() throws MPDConnectionException, MPDPlayerException {
        playId(null);
    }

    /**
     * Starts the player with the specified song.
     * <p>
     * @param song the song to start the player with
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void playId(MPDSong song) throws MPDConnectionException, MPDPlayerException {
        try {
            if (song == null) {
                mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPPLAY), null));
            } else {
                mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPPLAYID), new String[]{Integer.toString(song.getId())}));
            }
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        } catch (Exception e) {
            throw new MPDPlayerException(e);
        }

        if (status == PlayerStatus.STATUS_STOPPED || status == PlayerStatus.STATUS_PAUSED) {
            status = PlayerStatus.STATUS_PLAYING;
            firePlayerChangeEvent(PlayerChangeEvent.PLAYER_STARTED);
        } else {
            firePlayerChangeEvent(PlayerChangeEvent.PLAYER_SONG_SET);
        }
    }

    /**
     * Seeks to the desired location in the current song. If the location is
     * larger than the length of the song or is less than 0 then the parameter
     * is ignored.
     * <p>
     * @param secs the location to seek to
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void seek(long secs) throws MPDConnectionException, MPDPlayerException {
        seekId(null, secs);
    }

    /**
     * Seeks to the desired location in the specified song. If the location is
     * larger than the length of the song or is less than 0 then the parameter
     * is ignored.
     * <p>
     * @param song the song to seek in
     * @param secs the location to seek to
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void seekId(MPDSong song, long secs) throws MPDConnectionException, MPDPlayerException {
        List<String> response = null;
        String params[];

        if (song == null) {
            if (getCurrentSong() == null) {
                throw new MPDPlayerException("No song currently playing and no song specified", prop.getProperty(MPDPROPSEEKID), null);
            }

            if (getCurrentSong().getLength() > secs) {
                params = new String[]{
                    Integer.toString(getCurrentSong().getId()),
                    Long.toString(secs)
                };
                try {
                    response
                            = new ArrayList<String>(mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPSEEKID), params)));
                } catch (MPDResponseException re) {
                    throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
                }
            }
        } else {
            if (song.getLength() >= secs) {
                params = new String[]{
                    Integer.toString(song.getId()),
                    Long.toString(secs)
                };
                try {
                    response
                            = new ArrayList<String>(mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPSEEKID), params)));
                } catch (MPDResponseException re) {
                    throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
                }
            }
        }

        if (response != null) {
            firePlayerChangeEvent(PlayerChangeEvent.PLAYER_SEEKING);
        }
    }

    /**
     * Stops the player.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void stop() throws MPDConnectionException, MPDPlayerException {
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPSTOP), null));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        status = PlayerStatus.STATUS_STOPPED;
        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_STOPPED);
    }

    /**
     * Pauses the player.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void pause() throws MPDConnectionException, MPDPlayerException {
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPPAUSE), null));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        status = PlayerStatus.STATUS_PAUSED;
        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_PAUSED);

    }

    /**
     * Plays the next song in the playlist.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void playNext() throws MPDConnectionException, MPDPlayerException {
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPNEXT), null));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_NEXT);
    }

    /**
     * Plays the previous song in the playlist.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void playPrev() throws MPDConnectionException, MPDPlayerException {
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPPREV), null));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_PREVIOUS);

    }

    /**
     * Mutes the volume of the player.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void mute() throws MPDConnectionException, MPDPlayerException {
        oldVolume = getVolume();
        setVolume(0);
        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_MUTED);
    }

    /**
     * Unmutes the volume of the player.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void unMute() throws MPDConnectionException, MPDPlayerException {
        setVolume(oldVolume);
        firePlayerChangeEvent(PlayerChangeEvent.PLAYER_UNMUTED);
    }

    /**
     * Returns the instantaneous bitrate of the currently playing song or 0 if
     * not available (no song playing)
     * <p>
     * @return the instantaneous bitrate in kbps
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public int getBitrate() throws MPDConnectionException, MPDPlayerException {
        try {
            String bitrate = mpd.getStatus(MPD.StatusList.BITRATE);
            return (Integer.parseInt(bitrate));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        } catch (NullPointerException npe) {
            return (0);
        }
    }

    /**
     * Returns the current volume of the player.
     * <p>
     * @return the volume of the player (0-100)
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public int getVolume() throws MPDConnectionException, MPDPlayerException {
        try {
            return (Integer.parseInt(mpd.getStatus(MPD.StatusList.VOLUME)));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Sets the volume of the player. The volume is between 0 and 100, any
     * volume less that 0 results in a volume of 0 while any volume greater than
     * 100 results in a volume of 100.
     * <p>
     * @param volume the volume level (0-100)
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setVolume(int volume) throws MPDConnectionException, MPDPlayerException {
        if (volume > 100) {
            volume = 100;
        } else if (volume < 0) {
            volume = 0;
        }

        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPSETVOL), new String[]{Integer.toString(volume)}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        fireVolumeChangeEvent(volume);
    }

    /**
     * Returns if the player is repeating.
     * <p>
     * @return is the player repeating
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public boolean isRepeat() throws MPDConnectionException, MPDPlayerException {
        String repeat;
        try {
            repeat = mpd.getStatus(MPD.StatusList.REPEAT);
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        return "1".equals(repeat);
    }

    /**
     * Sets the repeating status of the player.
     * <p>
     * @param shouldRepeat should the player repeat the current song
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setRepeat(boolean shouldRepeat) throws MPDConnectionException, MPDPlayerException {
        String repeat;
        if (shouldRepeat) {
            repeat = "1";
        } else {
            repeat = "0";
        }
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPREPEAT), new String[]{repeat}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Returns if the player is in "single" mode.
     * <p>
     * @return the player is in "single" mode.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public boolean isSingleMode() throws MPDConnectionException, MPDPlayerException {
        try {
            String single = mpd.getStatus(MPD.StatusList.SINGLE);
            return "1".equals(single);
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Sets the single mode of the player.
     * <p>
     * @param singleMode should the player play only one song
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setSingleMode(boolean singleMode) throws MPDConnectionException, MPDPlayerException {
        String param;
        if (singleMode) {
            param = "1";
        } else {
            param = "0";
        }

        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPSINGLE), new String[]{param}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Returns if the player is in "consume" mode.
     * <p>
     * @return the player is in "consume" mode.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public boolean isConsuming() throws MPDConnectionException, MPDPlayerException {
        try {
            String consuming = mpd.getStatus(MPD.StatusList.CONSUME);
            return "1".equals(consuming);
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Enable/disable the consuming mode of the player.
     * <p>
     * @param consuming should the player remove the song from the playlist
     *                  after playing or not
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setConsuming(boolean consuming) throws MPDConnectionException, MPDPlayerException {
        String param;
        if (consuming) {
            param = "1";
        } else {
            param = "0";
        }

        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPCONSUME), new String[]{param}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Returns if the player is in random play mode.
     * <p>
     * @return true if the player is in random mode false otherwise
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public boolean isRandom() throws MPDConnectionException, MPDPlayerException {
        String random;
        try {
            random = mpd.getStatus(MPD.StatusList.RANDOM);
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        return "1".equals(random);
    }

    /**
     * Sets the random status of the player. So the songs will be played in
     * random order
     * <p>
     * @param shouldRandom should the player play in random mode
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setRandom(boolean shouldRandom) throws MPDConnectionException, MPDPlayerException {
        String random;
        if (shouldRandom) {
            random = "1";
        } else {
            random = "0";
        }
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPRANDOM), new String[]{random}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Plays the playlist in a random order.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void randomizePlay() throws MPDConnectionException, MPDPlayerException {
        setRandom(true);
    }

    /**
     * Plays the playlist in order.
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void unRandomizePlay() throws MPDConnectionException, MPDPlayerException {
        setRandom(false);
    }

    /**
     * Returns the cross fade of the player in seconds.
     * <p>
     * @return the cross fade of the player in seconds
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public int getXFade() throws MPDConnectionException, MPDPlayerException {
        try {
            return (Integer.parseInt(mpd.getStatus(MPD.StatusList.XFADE)));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Sets the cross fade of the player in seconds.
     * <p>
     * @param xFade the amount of cross fade to set in seconds
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public void setXFade(int xFade) throws MPDConnectionException, MPDPlayerException {
        try {
            mpd.sendMPDCommand(makeCommand(prop.getProperty(MPDPROPXFADE), new String[]{Integer.toString(xFade)}));
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }
    }

    /**
     * Returns the elapsed time of the current song in seconds.
     * <p>
     * @return the elapsed time of the song in seconds
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public long getElapsedTime() throws MPDConnectionException, MPDPlayerException {
        String time;

        try {
            time = mpd.getStatus(MPD.StatusList.TIME);
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        try {
            return (Integer.parseInt(time.trim().split(":")[0]));
        } catch (NullPointerException npe) {
            return (0);
        }

    }

    /**
     * Returns the {@link MPDAudioInfo} about the current status of the player.
     * If the status is unknown {@code null} will be returned. Any individual
     * parameter that is not known will be a -1
     * <p>
     * @return the sample rate
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public MPDAudioInfo getAudioDetails() throws MPDConnectionException, MPDPlayerException {
        MPDAudioInfo info = null;
        try {
            String response = mpd.getStatus(MPD.StatusList.AUDIO);
            if (response != null) {
                info = new MPDAudioInfo();
                String[] split = response.split(":");
                try {
                    info.setSampleRate(Integer.parseInt(split[0]));
                } catch (NumberFormatException nfe) {
                    info.setSampleRate(-1);
                }
                try {
                    info.setBits(Integer.parseInt(split[1]));
                } catch (NumberFormatException nfe) {
                    info.setBits(-1);
                }
                try {
                    info.setChannels(Integer.parseInt(split[2]));
                } catch (NumberFormatException nfe) {
                    info.setChannels(-1);
                }
            }
        } catch (MPDResponseException re) {
            throw new MPDPlayerException(re.getMessage(), re.getCommand(), re);
        }

        return info;
    }

    private MPDCommand makeCommand(String command, String[] params) {
        MPDCommand mpdCommand;

        if (params == null) {
            mpdCommand = new MPDCommand(command);
        } else {
            mpdCommand = new MPDCommand(command, params);
        }

        return (mpdCommand);
    }

    /**
     * Returns the current status of the player.
     * <p>
     * @return the status of the player
     * <p>
     * @throws org.bff.javampd.exception.MPDPlayerException     if the MPD
     *                                                          responded with
     *                                                          an error
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem sending
     *                                                          the command
     */
    public PlayerStatus getStatus() throws MPDResponseException, MPDConnectionException {
        String status = mpd.getStatus(MPD.StatusList.STATE);
        if (status.equalsIgnoreCase(MPD.STATUS_PLAYING)) {
            return (PlayerStatus.STATUS_PLAYING);
        } else if (status.equalsIgnoreCase(MPD.STATUS_PAUSED)) {
            return (PlayerStatus.STATUS_PAUSED);
        } else {
            return (PlayerStatus.STATUS_STOPPED);
        }
    }
}
