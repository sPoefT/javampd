/*
 * MPDSong.java
 *
 * Created on September 27, 2005, 10:39 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.bff.javampd.objects;

/**
 * MPDSong represents a song in the MPD database that can be inserted into
 * a playlist.
 *
 * @author Bill Findeisen
 * @version 1.0
 */
public class MPDSong extends MPDItem {

    private MPDArtist artist;
    private MPDAlbum album;
    private String file;
    private String genre;
    private String comment;
    private String year;
    private String discNumber;
    private int length;
    private int track;
    private int position = -1;
    private int id = -1;

    /**
     * Creates a new instance of MPDSong
     */
    public MPDSong() {
    }

    /**
     * Returns the name of the song.
     * Name and title are equal.
     *
     * @return the name of the song.
     */
    public String getTitle() {
        return getName();
    }

    /**
     * Sets the name of the song.
     * Name and title are equal.
     *
     * @param title the name of the song
     */
    public void setTitle(String title) {
        setName(title);
    }

    /**
     * Returns the name of the artist.
     *
     * @return the name of the artist
     */
    public MPDArtist getArtist() {
        return artist;
    }

    /**
     * Sets the name of the artist.
     *
     * @param artist the name of the artist
     */
    public void setArtist(MPDArtist artist) {
        this.artist = artist;
    }

    /**
     * Returns the name of the album.
     *
     * @return the name of the album
     */
    public MPDAlbum getAlbum() {
        return album;
    }

    /**
     * Sets the name of the album.
     *
     * @param album the name of the album
     */
    public void setAlbum(MPDAlbum album) {
        this.album = album;
    }

    /**
     * Returns the path of the song without a leading or trailing slash.
     *
     * @return the path of the song
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the path of the song.
     * Any leading or trailing slashes will be removed.
     *
     * @param path the path of the song
     */
    public void setFile(String path) {
        this.file = path;
    }

    /**
     * Returns the length of the song in seconds.
     *
     * @return the length of the song
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length of the song.
     *
     * @param length the length of the song in seconds
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Returns the track number of the song.
     *
     * @return the track number
     */
    public int getTrack() {
        return track;
    }

    /**
     * Sets the track number of the song
     *
     * @param track the track number of the song
     */
    public void setTrack(int track) {
        this.track = track;
    }

    /**
     * Returns the genre of the song.
     *
     * @return the genre of the song
     */
    public String getGenre() {
        return genre != null ? genre : "No Genre";
    }

    /**
     * Sets the genre of the song
     *
     * @param genre the genre of the song
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Returns the comment tag of the song.
     *
     * @return the comment tag of the song
     */
    public String getComment() {
        return comment != null ? comment : "";
    }

    /**
     * Sets the comment tag of the song
     *
     * @param comment the comment tag of the song
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the year of the song.
     *
     * @return the year of the song
     */
    public String getYear() {
        return year != null ? year : "No Year";
    }

    /**
     * Sets the year of the song
     *
     * @param year the year of the song
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Returns the string representation of this MPDSong.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File:").append(getFile()).append("\n");
        sb.append("Title:").append(getTitle()).append("\n");
        sb.append("Artist:").append(getArtist()).append("\n");
        sb.append("Album:").append(getAlbum()).append("\n");
        sb.append("Track:").append(getTrack()).append("\n");
        sb.append("Year:").append(getYear()).append("\n");
        sb.append("Genre:").append(getGenre()).append("\n");
        sb.append("Comment:").append(getComment()).append("\n");
        sb.append("Length:").append(getLength()).append("\n");
        sb.append("Pos:").append(getPosition()).append("\n");
        sb.append("SongId:").append(getId()).append("\n");

        return (sb.toString());
    }

    /**
     * Returns the position of the song in the playlist. Returns
     * a -1 if the song is not in the playlist.
     *
     * @return the position in the playlist
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the playlist song id for the song. Returns
     * a -1 if the song is not in the playlist.
     *
     * @return song id of the song
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the playlist position for a song.
     *
     * @param position the playlist position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Sets the playlist song id for this MPDSong.
     *
     * @param id the playlist song id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the discNumber
     */
    public String getDiscNumber() {
        return discNumber != null ? discNumber : "";
    }

    /**
     * @param discNumber the discNumber to set
     */
    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
    }

    @Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		
		MPDSong other = (MPDSong) obj;
		
		// Check same file first 
		// -> this is the best candidate for early exit
		if (file == null)
		{
			if (other.file != null)
				return false;
		}
		else if (!file.equals(other.file))
			return false;
		if (album == null)
		{
			if (other.album != null)
				return false;
		}
		else if (!album.equals(other.album))
			return false;
		if (artist == null)
		{
			if (other.artist != null)
				return false;
		}
		else if (!artist.equals(other.artist))
			return false;
		if (comment == null)
		{
			if (other.comment != null)
				return false;
		}
		else if (!comment.equals(other.comment))
			return false;
		if (discNumber == null)
		{
			if (other.discNumber != null)
				return false;
		}
		else if (!discNumber.equals(other.discNumber))
			return false;
		if (genre == null)
		{
			if (other.genre != null)
				return false;
		}
		else if (!genre.equals(other.genre))
			return false;
		if (id != other.id)
			return false;
		if (length != other.length)
			return false;
		if (position != other.position)
			return false;
		if (track != other.track)
			return false;
		if (year == null)
		{
			if (other.year != null)
				return false;
		}
		else if (!year.equals(other.year))
			return false;
		
		return true;
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + id;
		result = prime * result + position;
		
		// Assuming that the following entries don't contribute much
		// we skip them to save some computation time
		
//		result = prime * result + ((album == null) ? 0 : album.hashCode());
//		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
//		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
//		result = prime * result + ((discNumber == null) ? 0 : discNumber.hashCode());
//		result = prime * result + ((genre == null) ? 0 : genre.hashCode());
//		result = prime * result + length;
//		result = prime * result + track;
//		result = prime * result + ((year == null) ? 0 : year.hashCode());

		return result;
	}

    @Override
    public int compareTo(MPDItem item) {
        MPDSong song = (MPDSong) item;
        StringBuffer sb;

        sb = new StringBuffer();
        sb.append(getAlbum());
        sb.append(formatToComparableString(getTrack()));
        String thisSong = sb.toString();

        sb = new StringBuffer();
        sb.append(song.getAlbum());
        sb.append(formatToComparableString(song.getTrack()));
        String songToCompare = sb.toString();

        return thisSong.compareTo(songToCompare);
    }

    private String formatToComparableString(int i) {
        return String.format("%1$08d", i);
    }
}
