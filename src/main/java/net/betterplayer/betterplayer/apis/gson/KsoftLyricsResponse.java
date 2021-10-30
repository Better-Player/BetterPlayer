package net.betterplayer.betterplayer.apis.gson;

import com.google.gson.annotations.SerializedName;

public class KsoftLyricsResponse {

	private long total;
	private int took;
	private Data[] data;
	
	public class Data {
		private long popularity;
		private String artist, album, name, lyrics;
		
		@SerializedName("album_ids")
		private String albumIds;
		
		@SerializedName("album_year")
		private String albumYear;

		@SerializedName("search_str")
		private String searchString;
		
		@SerializedName("album_art")
		private String albumArt;

		public long getPopularity() {
			return popularity;
		}

		public String getArtist() {
			return artist;
		}

		public String getAlbum() {
			return album;
		}

		public String getName() {
			return name;
		}

		public String getLyrics() {
			return lyrics;
		}

		public String getAlbumIds() {
			return albumIds;
		}

		public String getAlbumYear() {
			return albumYear;
		}

		public String getSearchString() {
			return searchString;
		}

		public String getAlbumArt() {
			return albumArt;
		}
	}


	public Data[] getData() {
		return data;
	}

	public int getTook() {
		return took;
	}

	public long getTotal() {
		return total;
	}
}
