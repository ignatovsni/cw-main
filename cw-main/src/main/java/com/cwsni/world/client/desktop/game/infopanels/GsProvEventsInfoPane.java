package com.cwsni.world.client.desktop.game.infopanels;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.data.events.Event;
import com.cwsni.world.model.engine.Province;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GsProvEventsInfoPane extends InternalInfoPane {

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
		List<Event> events = new ArrayList<Event>(prov.getEvents().getEvents());
		events.forEach(e -> {
			pane.getChildren().add(createWithAlignment(e.getTitle(), true));
			pane.getChildren().add(createWithAlignment(e.getDescription(), false));
		});
	}

	private Pane createWithAlignment(String txt, boolean al) {
		BorderPane pane = new BorderPane();
		Label lblMsg = new Label(txt);
		if (al) {
			pane.setLeft(lblMsg);
		} else {
			pane.setRight(lblMsg);
		}
		return pane;
	}

	@Override
	protected boolean hasDataForUser() {
		Province prov = gameScene.getSelectedProvince();
		return (prov != null && !prov.getEvents().getEvents().isEmpty());
	}
}
