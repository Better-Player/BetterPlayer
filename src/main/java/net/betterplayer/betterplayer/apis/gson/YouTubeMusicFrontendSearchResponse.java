package net.betterplayer.betterplayer.apis.gson;

/**
 * Class describing JSON response included when doing a search via the YouTube music frontend. It's ugly, I know
 * 
 * @author Tobias de Bruijn
 *
 */
public class YouTubeMusicFrontendSearchResponse {
	private Content contents;
	
	/**
	 * Get the video ID returned for the first Track in the search response
	 * @return Returns the video ID
	 */
	public String getVideoId() {
		FlexColumn[] fcs = this.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content.sectionListRenderer.contents[1].musicShelfRenderer.contents[0].musicResponsiveListItemRenderer.flexColumns;
		for(FlexColumn fc : fcs) {
			Run[] rs = fc.musicResponsiveListItemFlexColumnRenderer.text.runs;
			for(Run r : rs) {
				if(r.navigationEndpoint != null) {
					if(r.navigationEndpoint.watchEndpoint != null) {
						return r.navigationEndpoint.watchEndpoint.videoId;
					}
				}
			}
		}
		
		return null;
	}
	
	private class Content {
		private TabbedSearchResultsRenderer tabbedSearchResultsRenderer;
	}
	
	private class TabbedSearchResultsRenderer {
		private Tab[] tabs;
	}
	
	private class Tab {
		private TabRenderer tabRenderer;
	}
	
	private class TabRenderer {
		private Content1 content;
	}
	
	private class Content1 {
		private SectionListRenderer sectionListRenderer;
	}
	
	private class SectionListRenderer {
		private Content2[] contents;
	}
	
	private class Content2 {
		private MusicShelfRenderer musicShelfRenderer;
	}
	
	private class MusicShelfRenderer {
		private Content3[] contents;
	}
	
	private class Content3 {
		private MusicResponsiveListItemRenderer musicResponsiveListItemRenderer;
	}
	
	private class MusicResponsiveListItemRenderer {
		private FlexColumn[] flexColumns;
	}
	
	private class FlexColumn {
		private MusicResponsiveListItemFlexColumnRenderer musicResponsiveListItemFlexColumnRenderer;
	}
	
	private class MusicResponsiveListItemFlexColumnRenderer {
		private Text text;
	}
	
	private class Text {
		private Run[] runs; 
	}
	
	private class Run {
		private NavigationEndpoint navigationEndpoint;
	}
	
	private class NavigationEndpoint {
		private WatchEndpoint watchEndpoint;
	}
	
	private class WatchEndpoint {
		private String videoId;
	}
}
