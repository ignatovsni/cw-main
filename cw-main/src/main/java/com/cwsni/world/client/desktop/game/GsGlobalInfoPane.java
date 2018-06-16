package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.InternalPane;
import com.cwsni.world.client.desktop.util.InternalTableView;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsGlobalInfoPane extends InternalPane {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;
	private InternalTableView table;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("global.info.pane.title"), pane);
	}

	private Pane createUI() {
		table = new InternalTableView();
		table.setPrefHeight(50);
		return new HBox(table);
	}

	public void refreshInfo() {
		table.clearAllRows();
		table.setData("Provinces", gameScene.getGame().getMap().getProvinces().size());
	}

}
