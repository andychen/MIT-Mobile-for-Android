package edu.mit.mitmobile2.news;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.SearchActivity;
import edu.mit.mitmobile2.objs.NewsItem;
import edu.mit.mitmobile2.objs.SearchResults;

public class NewsSearchActivity extends SearchActivity<NewsItem> {

	protected NewsModel mNewsModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mNewsModel = new NewsModel(this);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected ArrayAdapter<NewsItem> getListAdapter(final SearchResults<NewsItem> results) {
		List<NewsItem> newsItems = results.getResultsList();

		mSearchListView.setOnItemClickListener(
			new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
					Intent intent = new Intent(NewsSearchActivity.this, NewsDetailsActivity.class);
					intent.putExtra(NewsDetailsActivity.KEY_POSITION, position);
					intent.putExtra(NewsDetailsActivity.SEARCH_TERM_KEY, results.getSearchTerm());
					startActivity(intent);
				}
			}
		);
		
		return new NewsArrayAdapter(this, 0, newsItems, new NewsModel(this), mSearchListView);
	}

	@Override
	protected String getSuggestionsAuthority() {
		return NewsSearchSuggestionsProvider.AUTHORITY;
	}

	@Override
	protected void initiateSearch(String searchTerm, Handler uiHandler) {
		mNewsModel.executeSearch(searchTerm, uiHandler);
	}

	@Override
	protected String searchItemPlural() {
		return "stories";
	}

	@Override
	protected String searchItemSingular() {
		return "story";
	}

	@Override
	protected Module getModule() {
		return new NewsModule();
	}
}
