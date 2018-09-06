package com.cwsni.world.client.desktop.game.infopanels;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.game.events.GameEventHandler;
import com.cwsni.world.model.engine.Event;
import com.cwsni.world.model.engine.Province;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GsProvEventsInfoPane extends InternalInfoPane {

	@Autowired
	private GameEventHandler gameEventHandler;

	private GameScene gameScene;
	private Pane pane;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		pane = createUI();
		init(getMessage("info.pane.prov.events.title"), pane);
	}

	private Pane createUI() {
		VBox box = new VBox();
		return box;
	}

	public void refreshInfo() {
		pane.getChildren().clear();
		if (!hasDataForUser()) {
			return;
		}
		Province prov = gameScene.getSelectedProvince();
		Set<Object> eventsIds = prov.getModifiers().getAllEvents();
		Map<Event, List<String>> descriptions = gameEventHandler
				.getTitleAndShortDescriptionsForEvents(gameScene.getGame(), eventsIds, prov);
		for (Entry<Event, List<String>> entry : descriptions.entrySet()) {
			String title = entry.getValue().get(0);
			String shortDescription = entry.getValue().get(1);
			pane.getChildren().add(createWithAlignment(title, shortDescription));
		}
	}

	private Pane createWithAlignment(String title, String shortDescription) {
		BorderPane pane = new BorderPane();
		Label lblMsg = new Label(title);
		pane.setLeft(lblMsg);
		lblMsg.setTooltip(new Tooltip(shortDescription));
		return pane;
	}

	@Override
	protected boolean hasDataForUser() {
		Province prov = gameScene.getSelectedProvince();
		return prov != null && !prov.getModifiers().isEmpty();
	}
}
