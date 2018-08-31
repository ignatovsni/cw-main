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

	public class SearchResult {
		Object object;
		Integer id;
		String name;

		public SearchResult(Object object, Integer id, String name) {
			this.object = object;
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (object instanceof Province) {
				sb.append(messageSource.getMessage("window.search-on-map.province"));
			} else if (object instanceof Country) {
				sb.append(messageSource.getMessage("window.search-on-map.country"));
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

	private ObservableList<SearchResult> searchResults;
	private Map<String, SearchResult> strToSearchResult;
	private Set<Object> foundObjects;

	private Button goButton;

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
		searchResults.clear();
		strToSearchResult.clear();
		foundObjects.clear();
		if (searchText == null || searchText.isEmpty()) {
			return;
		}
		Game game = gameScene.getGame();
		try {
			Integer id = Integer.valueOf(searchText);
			Province prov = game.getMap().findProvById(id);
			if (prov != null) {
				addProvinceToSearchResults(prov);
			}
			Country country = game.findCountryById(id);
			if (country != null) {
				addCountryToSearchResults(country);
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}

	private void addProvinceToSearchResults(Province o) {
		if (!foundObjects.contains(o)) {
			SearchResult sr = new SearchResult(o, o.getId(), o.getName());
			searchResults.add(sr);
			strToSearchResult.put(sr.toString(), sr);
			foundObjects.add(o);
		}
	}

	private void addCountryToSearchResults(Country o) {
		if (!foundObjects.contains(o)) {
			SearchResult sr = new SearchResult(o, o.getId(), o.getName());
			searchResults.add(sr);
			strToSearchResult.put(sr.toString(), sr);
			foundObjects.add(o);
		}
	}

	public SearchResult getSelectedSearchResult() {
		return searchField.getValue();
	}

}
