/*
 * MPDStandAloneMonitor.java
 *
 * Created on October 18, 2005, 10:17 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.bff.javampd.monitor;

import org.bff.javampd.MPD;
import org.bff.javampd.MPD.StatusList;
import org.bff.javampd.MPDOutput;
import org.bff.javampd.events.*;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDException;
import org.bff.javampd.exception.MPDResponseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MPDStandAloneMonitor monitors a MPD connection by querying the status and
 * statistics of the MPD server at given delay intervals. As statistics change
 * appropriate events are fired indicating these changes. If more detailed
 * events are desired attach listeners to the different controllers of a
 * connection or use the {@link MPDEventRelayer} class.
 * <p>
 * @author Bill Findeisen
 * @version 1.0
 */
public class MPDStandAloneMonitor
        extends MPDEventMonitor
        implements Runnable {

    private final MPD mpd;
    private final int delay;
    private int newVolume;
    private int oldVolume;
    private int newPlaylistVersion;
    private int oldPlaylistVersion;
    private int newPlaylistLength;
    private int oldPlaylistLength;
    private int oldSong;
    private int newSong;
    private int oldSongId;
    private int newSongId;
    private int oldBitrate;
    private int newBitrate;
    private long elapsedTime;
    private String state;
    private String error;
    private boolean stopped;
    private int oldRepeat;
    private int oldRandom;
    private int oldSingleMode;
    private int oldConsume;
    private HashMap<Integer, MPDOutput> outputMap;

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
    private static final int DEFAULT_DELAY = 1000;
    private static final String RESPONSE_PLAY = "play";
    private static final String RESPONSE_STOP = "stop";
    private static final String RESPONSE_PAUSE = "pause";
    private List<PlayerBasicChangeListener> playerListeners;
    private List<PlaylistBasicChangeListener> playlistListeners;
    private List<VolumeChangeListener> volListeners;
    private List<MPDErrorListener> errorListeners;
    private List<OutputChangeListener> outputListeners;

    /**
     * Creates a new instance of MPDStandAloneMonitor using the default delay of
     * 1 second.
     * <p>
     * @param mpd a connection to a MPD server
     */
    public MPDStandAloneMonitor(MPD mpd) {
        this(mpd, DEFAULT_DELAY);
    }

    /**
     * Creates a new instance of MPDStandAloneMonitor using the given delay
     * interval for queries.
     * <p>
     * @param mpd   a connection to a MPD server
     * @param delay the delay interval
     */
    public MPDStandAloneMonitor(MPD mpd, int delay) {
        super(mpd);
        this.mpd = mpd;
        this.delay = delay;
        this.playerListeners = new ArrayList<PlayerBasicChangeListener>();
        this.playlistListeners = new ArrayList<PlaylistBasicChangeListener>();
        this.volListeners = new ArrayList<VolumeChangeListener>();
        this.errorListeners = new ArrayList<MPDErrorListener>();
        this.outputListeners = new ArrayList<OutputChangeListener>();
        this.outputMap = new HashMap<Integer, MPDOutput>();
        try {
            //initial load so no events fired
            processResponse(mpd.getStatus());
            loadOutputs(mpd.getMPDAdmin().getOutputs());
        } catch (MPDException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds a {@link PlayerBasicChangeListener} to this object to receive
     * {@link PlayerChangeEvent}s.
     * <p>
     * @param pcl the PlayerBasicChangeListener to add
     */
    public synchronized void addPlayerChangeListener(PlayerBasicChangeListener pcl) {
        playerListeners.add(pcl);
    }

    /**
     * Removes a {@link PlayerBasicChangeListener} from this object.
     * <p>
     * @param pcl the PlayerBasicChangeListener to remove
     */
    public synchronized void removePlayerChangeListener(PlayerBasicChangeListener pcl) {
        playerListeners.remove(pcl);
    }

    /**
     * Sends the appropriate {@link PlayerBasicChangeEvent} to all registered
     * {@link PlayerBasicChangeListener}s.
     * <p>
     * @param id the event id to send
     */
    protected synchronized void firePlayerChangeEvent(int id) {
        PlayerBasicChangeEvent pce = new PlayerBasicChangeEvent(this, id);

        for (PlayerBasicChangeListener pcl : playerListeners) {
            pcl.playerBasicChange(pce);
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
     * Adds a {@link OutputChangeListener} to this object to receive
     * {@link OutputChangeEvent}s.
     * <p>
     * @param vcl the OutputChangeListener to add
     */
    public synchronized void addOutputChangeListener(OutputChangeListener vcl) {
        outputListeners.add(vcl);
    }

    /**
     * Removes a {@link OutputChangeListener} from this object.
     * <p>
     * @param vcl the OutputChangeListener to remove
     */
    public synchronized void removeOutputChangedListener(OutputChangeListener vcl) {
        outputListeners.remove(vcl);
    }

    /**
     * Sends the appropriate {@link OutputChangeEvent} to all registered
     * {@link OutputChangeListener}s.
     * <p>
     * @param event the event id to send
     */
    protected synchronized void fireOutputChangeEvent(OutputChangeEvent event) {
        for (OutputChangeListener ocl : outputListeners) {
            ocl.outputChanged(event);
        }
    }

    /**
     * Adds a {@link PlaylistBasicChangeListener} to this object to receive
     * {@link PlaylistChangeEvent}s.
     * <p>
     * @param pcl the PlaylistChangeListener to add
     */
    public synchronized void addPlaylistChangeListener(PlaylistBasicChangeListener pcl) {
        playlistListeners.add(pcl);
    }

    /**
     * Removes a {@link PlaylistBasicChangeListener} from this object.
     * <p>
     * @param pcl the PlaylistBasicChangeListener to remove
     */
    public synchronized void removePlaylistStatusChangedListener(PlaylistBasicChangeListener pcl) {
        playlistListeners.remove(pcl);
    }

    /**
     * Sends the appropriate {@link PlaylistChangeEvent} to all registered
     * {@link PlaylistChangeListener}.
     * <p>
     * @param id the event id to send
     */
    protected synchronized void firePlaylistChangeEvent(int id) {
        PlaylistBasicChangeEvent pce = new PlaylistBasicChangeEvent(this, id);

        for (PlaylistBasicChangeListener pcl : playlistListeners) {
            pcl.playlistBasicChange(pce);
        }
    }

    /**
     * Adds a {@link MPDErrorListener} to this object to receive
     * {@link MPDErrorEvent}s.
     * <p>
     * @param el the MPDErrorListener to add
     */
    public synchronized void addMPDErrorListener(MPDErrorListener el) {
        errorListeners.add(el);
    }

    /**
     * Removes a {@link MPDErrorListener} from this object.
     * <p>
     * @param el the MPDErrorListener to remove
     */
    public synchronized void removeMPDErrorListener(MPDErrorListener el) {
        errorListeners.remove(el);
    }

    /**
     * Sends the appropriate {@link MPDErrorListener} to all registered
     * {@link MPDErrorListener}s.
     * <p>
     * @param msg the event message
     */
    protected void fireMPDErrorEvent(String msg) {
        MPDErrorEvent ee = new MPDErrorEvent(this, msg);

        for (MPDErrorListener el : errorListeners) {
            el.errorEventReceived(ee);
        }
    }

    /**
     * Implements the Runnable run method to monitor the MPD connection.
     */
    @Override
    public void run() {
        Map<String, String> response;
        while (!isStopped()) {

            try {
                try {
                    synchronized (this) {
                        response = mpd.getStatus();
                        processResponse(response);

                        checkError();
                        checkPlayer();
                        checkPlaylist();
                        checkTrackPosition(elapsedTime);
                        checkVolume();
                        checkBitrate();
                        checkConnection();
                        checkOutputs();
                        this.wait(delay);
                    }
                } catch (InterruptedException ie) {
                    setStopped(true);
                }
            } catch (MPDException mce) {
                if (mce instanceof MPDConnectionException) {
                    fireConnectionChangeEvent(false, mce.getMessage());
                    boolean retry = true;

                    while (retry) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MPDStandAloneMonitor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        checkConnection();
                        if (isConnectedState()) {
                            retry = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Starts the monitor by creating and starting a thread using this instance
     * as the Runnable interface.
     */
    public void start() {
        Executors.newSingleThreadExecutor().execute(this);
    }

    /**
     * Stops the thread.
     */
    public void stop() {
        setStopped(true);
    }

    /**
     * Returns true if the monitor is stopped, false if the monitor is still
     * running.
     * <p>
     * @return true if monitor is running, false otherwise false
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Returns the current status of the player.
     * <p>
     * @return the status of the player
     */
    public PlayerStatus getStatus() {
        return status;
    }

    private void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private void checkError() {
        if (error != null) {
            fireMPDErrorEvent(error);
        }
    }

    private void checkPlayer() {
        PlayerStatus newStatus = PlayerStatus.STATUS_STOPPED;
        if (state.startsWith(RESPONSE_PLAY)) {
            newStatus = PlayerStatus.STATUS_PLAYING;
        } else if (state.startsWith(RESPONSE_PAUSE)) {
            newStatus = PlayerStatus.STATUS_PAUSED;
        } else if (state.startsWith(RESPONSE_STOP)) {
            newStatus = PlayerStatus.STATUS_STOPPED;
        }

        if (status != newStatus) {
            switch (newStatus) {
                case STATUS_PLAYING:
                    switch (status) {
                        case STATUS_PAUSED:
                            firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_UNPAUSED);
                            break;
                        case STATUS_STOPPED:
                            firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_STARTED);
                            break;
                    }
                    break;
                case STATUS_STOPPED:
                    elapsedTime = 0; //when stopped no time in response reading 0
                    firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_STOPPED);
                    if (newSongId == -1) {
                        firePlaylistChangeEvent(PlaylistBasicChangeEvent.PLAYLIST_ENDED);
                    }

                    break;
                case STATUS_PAUSED:
                    switch (status) {
                        case STATUS_PAUSED:
                            firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_UNPAUSED);
                            break;
                        case STATUS_PLAYING:
                            firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_PAUSED);
                            break;
                    }
            }
            status = newStatus;
        }
    }

    private void checkBitrate() {
        if (playerListeners.isEmpty()) {
            return;
        }

        if (oldBitrate != newBitrate) {
            firePlayerChangeEvent(PlayerBasicChangeEvent.PLAYER_BITRATE_CHANGE);
            oldBitrate = newBitrate;
        }
    }

    /**
     * Checks the connection status of the MPD. Fires a
     * {@link ConnectionChangeEvent} if the connection status changes.
     * <p>
     * @throws org.bff.javampd.exception.MPDConnectionException if there is a
     *                                                          problem with the
     *                                                          connection
     * @throws org.bff.javampd.exception.MPDResponseException   if response is
     *                                                          an error
     */
    private void checkOutputs() throws MPDConnectionException, MPDResponseException {
        if (outputListeners.isEmpty()) {
            return;
        }

        List<MPDOutput> outputs = new ArrayList<MPDOutput>(mpd.getMPDAdmin().getOutputs());
        if (outputs.size() > outputMap.size()) {
            fireOutputChangeEvent(new OutputChangeEvent(this, OutputChangeEvent.OUTPUT_EVENT.OUTPUT_ADDED));
            loadOutputs(outputs);
        } else if (outputs.size() < outputMap.size()) {
            fireOutputChangeEvent(new OutputChangeEvent(this, OutputChangeEvent.OUTPUT_EVENT.OUTPUT_DELETED));
            loadOutputs(outputs);
        } else {
            for (MPDOutput out : outputs) {
                MPDOutput output = outputMap.get(out.getId());
                if (output == null) {
                    fireOutputChangeEvent(new OutputChangeEvent(out, OutputChangeEvent.OUTPUT_EVENT.OUTPUT_CHANGED));
                    loadOutputs(outputs);
                    return;
                } else {
                    if (output.isEnabled() != out.isEnabled()) {
                        fireOutputChangeEvent(new OutputChangeEvent(out, OutputChangeEvent.OUTPUT_EVENT.OUTPUT_CHANGED));
                        loadOutputs(outputs);
                        return;
                    }
                }

            }
        }
    }

    private void loadOutputs(Collection<MPDOutput> outputs) {
        outputMap.clear();
        for (MPDOutput output : outputs) {
            outputMap.put(output.getId(), output);
        }
    }

    private void checkPlaylist() {
        if (playlistListeners.isEmpty()) {
            return;
        }

        if (oldPlaylistVersion != newPlaylistVersion) {
            firePlaylistChangeEvent(PlaylistBasicChangeEvent.PLAYLIST_CHANGED);
            oldPlaylistVersion = newPlaylistVersion;
        }

        if (oldPlaylistLength != newPlaylistLength) {
            if (oldPlaylistLength < newPlaylistLength) {
                firePlaylistChangeEvent(PlaylistBasicChangeEvent.SONG_ADDED);
            } else if (oldPlaylistLength > newPlaylistLength) {
                firePlaylistChangeEvent(PlaylistBasicChangeEvent.SONG_DELETED);
            }

            oldPlaylistLength = newPlaylistLength;
        }

        if (status == PlayerStatus.STATUS_PLAYING) {
            if (oldSong != newSong) {
                firePlaylistChangeEvent(PlaylistBasicChangeEvent.SONG_CHANGED);
                oldSong = newSong;
            } else if (oldSongId != newSongId) {
                firePlaylistChangeEvent(PlaylistBasicChangeEvent.SONG_CHANGED);
                oldSongId = newSongId;
            }
        }
    }

    private void checkVolume() {
        if (volListeners.isEmpty()) {
            return;
        }

        if (oldVolume != newVolume) {
            fireVolumeChangeEvent(newVolume);
            oldVolume = newVolume;
        }
    }

    private void checkRepeat(int repeat) {
        if (playerListeners.isEmpty()) {
            return;
        }

        if (repeat != oldRepeat) {
            firePlayerChangeEvent(repeat == 1 ? PlayerBasicChangeEvent.PLAYER_REPEAT_ON : PlayerBasicChangeEvent.PLAYER_REPEAT_OFF);
            oldRepeat = repeat;
        }
    }

    private void checkRandom(int random) {
        if (playerListeners.isEmpty()) {
            return;
        }

        if (random != oldRandom) {
            firePlayerChangeEvent(random == 1 ? PlayerBasicChangeEvent.PLAYER_RANDOM_ON : PlayerBasicChangeEvent.PLAYER_RANDOM_OFF);
            oldRandom = random;
        }
    }

    private void checkSingleMode(int singleMode) {
        if (playerListeners.isEmpty()) {
            return;
        }

        if (singleMode != oldSingleMode) {
            firePlayerChangeEvent(singleMode == 1 ? PlayerBasicChangeEvent.PLAYER_SINGLE_ON : PlayerBasicChangeEvent.PLAYER_SINGLE_OFF);
            oldSingleMode = singleMode;
        }
    }

    private void checkConsume(int consuming) {
        if (playerListeners.isEmpty()) {
            return;
        }

        if (consuming != oldConsume) {
            firePlayerChangeEvent(consuming == 1 ? PlayerBasicChangeEvent.PLAYER_CONSUME_ON : PlayerBasicChangeEvent.PLAYER_CONSUME_OFF);
            oldConsume = consuming;
        }
    }

    private void processResponse(Map<String, String> response) {
        newSongId = -1;
        newSong = -1;
        error = null;

        for (Entry<String, String> pair : response.entrySet()) {
            String key = pair.getKey();
            String value = pair.getValue();

            if (StatusList.VOLUME.getStatusPrefix().equals(key)) {
                newVolume = Integer.parseInt(value);
            }
            if (StatusList.REPEAT.getStatusPrefix().equals(key)) {
                checkRepeat(Integer.parseInt(value));
            }
            if (StatusList.RANDOM.getStatusPrefix().equals(key)) {
                checkRandom(Integer.parseInt(value));
            }
            if (StatusList.CONSUME.getStatusPrefix().equals(key)) {
                checkConsume(Integer.parseInt(value));
            }
            if (StatusList.SINGLE.getStatusPrefix().equals(key)) {
                checkSingleMode(Integer.parseInt(value));
            }
            if (StatusList.PLAYLIST.getStatusPrefix().equals(key)) {
                newPlaylistVersion = Integer.parseInt(value);
            }
            if (StatusList.PLAYLISTLENGTH.getStatusPrefix().equals(key)) {
                newPlaylistLength = Integer.parseInt(value);
            }
            if (StatusList.STATE.getStatusPrefix().equals(key)) {
                state = value;
            }
            if (StatusList.CURRENTSONG.getStatusPrefix().equals(key)) {
                newSong = Integer.parseInt(value);
            }
            if (StatusList.CURRENTSONGID.getStatusPrefix().equals(key)) {
                newSongId = Integer.parseInt(value);
            }
            if (StatusList.TIME.getStatusPrefix().equals(key)) {
                elapsedTime = Long.parseLong(value.split(":")[0]);
            }
            if (StatusList.BITRATE.getStatusPrefix().equals(key)) {
                newBitrate = Integer.parseInt(value);
            }
            if (StatusList.ERROR.getStatusPrefix().equals(key)) {
                error = value;
            }
        }
    }
}
