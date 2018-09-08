package com.cwsni.world.client.desktop.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

@Component
@Scope("prototype")
public class SearchOnMapWindow extends Dialog<ButtonType> {

	public enum SearchResultType {
		PROVINCE, COUNTRY
	}

	public class SearchResult {
		SearchResultType type;
		Integer id;
		String name;

		public SearchResult(SearchResultType type, Integer id, String name) {
			this.type = type;
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			switch (type) {
			case PROVINCE:
				sb.append(messageSource.getMessage("window.search-on-map.province"));
				break;
			case COUNTRY:
				sb.append(messageSource.getMessage("window.search-on-map.country"));
				break;
			}
			sb.append(": ");
			sb.append(name);
			sb.append(" | ");
			sb.append(id);
			return sb.toString();
		}
	}

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;
	private ComboBox<SearchResult> searchField;
	private Button goButton;

	private ObservableList<SearchResult> searchResults;
	private Map<String, SearchResult> strToSearchResult;
	private Set<Object> foundObjects;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		strToSearchResult = new HashMap<>();
		foundObjects = new HashSet<>();
		searchResults = FXCollections.observableArrayList();

		setTitle(getMessage("window.search-on-map.title"));
		ButtonType okButtonType = new ButtonType(getMessage("window.search-on-map.button.ok"), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(okButtonType);
		ButtonType cancelButtonType = new ButtonType(getMessage("window.search-on-map.button.cancel"),
				ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(cancelButtonType);
		goButton = (Button) getDialogPane().lookupButton(okButtonType);

		searchField = new ComboBox<SearchResult>(searchResults);
		searchField.setEditable(true);
		searchField.setConverter(new StringConverter<SearchOnMapWindow.SearchResult>() {
			@Override
			public String toString(SearchResult object) {
				if (object != null) {
					return object.toString();
				} else {
					return null;
				}
			}

			@Override
			public SearchResult fromString(String string) {
				return strToSearchResult.get(string);
			}
		});
		searchField.getEditor().setOnKeyReleased(e -> {
			processSearchAndFillResult(searchField.getEditor().getText());
			searchField.hide();
			searchField.setVisibleRowCount(searchResults.size());
			searchField.show();
		});
		searchField.setOnAction(e -> Platform.runLater(() -> {
			if (searchField.getValue() != null) {
				goButton.fire();
			}
		}));

		Node pane = new VBox(searchField);
		getDialogPane().setContent(pane);
		this.setOnShown(e -> Platform.runLater(() -> searchField.requestFocus()));
	}

	private void processSearchAndFillResult(String searchText) {
		int maxResults = 20;
		searchResults.clear();
		strToSearchResult.clear();
		foundObjects.clear();
		if (searchText == null || searchText.isEmpty()) {
			return;
		}
		Game game = gameScene.getGame();
		try {
			Integer id = Integer.valueOf(searchText);
			Country country = game.findCountryById(id);
			if (country != null) {
				addCountryToSearchResults(country);
			}
			Province prov = game.getMap().findProvinceById(id);
			if (prov != null) {
				addProvinceToSearchResults(prov);
			}
		} catch (NumberFormatException e) {
			// ignore
		}
		searchText = searchText.toLowerCase();
		int found = 0;
		for (Country o : game.getCountries()) {
			if (searchResults.size() > maxResults || found > maxResults / 2) {
				break;
			}
			if (o.getName().toLowerCase().startsWith(searchText)) {
				addCountryToSearchResults(o);
				found++;
			}
		}
		found = 0;
		for (Province o : game.getMap().getProvinces()) {
			if (searchResults.size() > maxResults || found > maxResults / 2) {
				break;
			}
			if (o.getName().toLowerCase().startsWith(searchText)) {
				addProvinceToSearchResults(o);
				found++;
			}
		}
	}

	private void addProvinceToSearchResults(Province o) {
		if (!foundObjects.contains(o)) {
			SearchResult sr = new SearchResult(SearchResultType.PROVINCE, o.getId(), o.getName());
			searchResults.add(sr);
			strToSearchResult.put(sr.toString(), sr);
			foundObjects.add(o);
		}
	}

	private void addCountryToSearchResults(Country o) {
		if (!foundObjects.contains(o)) {
			SearchResult sr = new SearchResult(SearchResultType.COUNTRY, o.getId(), o.getName());
			searchResults.add(sr);
			strToSearchResult.put(sr.toString(), sr);
			foundObjects.add(o);
		}
	}

	public SearchResult getSelectedSearchResult() {
		return searchField.getValue();
	}

}
