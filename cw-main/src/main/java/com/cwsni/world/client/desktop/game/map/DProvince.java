package com.cwsni.world.client.desktop.game.map;

import java.io.InputStream;
import java.util.stream.Stream;

import com.cwsni.world.model.GameTransientStats;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.events.Event;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * Hexagon province
 * 
 * @author sni
 *
 */
class DProvince extends Group {
	// https://www.redblobgames.com/grids/hexagons/ - how to calculate hex

	private static final Color STROKE_SELECTED_2_COLOR = Color.YELLOW;
	private static final Color STROKE_SELECTED_1_COLOR = Color.RED;
	private static final Color STROKE_DEFAULT_COLOR = Color.GRAY;

	public static final int SIDES = 6;

	private Point2D[] points = new Point2D[SIDES];
	private Point2D center;
	private double radius;
	private Polygon polygon;
	private Timeline timeline;

	private final DWorldMap map;
	private final Province province;

	private DProvince(DWorldMap map, Province province, double provinceRadius) {
		this.map = map;
		this.province = province;
		this.center = new Point2D(province.getCenter().getX(), province.getCenter().getY());
		this.radius = provinceRadius;
		updatePoints();
		createVisualElements();
	}

	private void createVisualElements() {
		polygon = new Polygon();
		Stream.of(points).forEach(p -> polygon.getPoints().addAll(p.getX(), p.getY()));
		polygon.setStroke(STROKE_DEFAULT_COLOR);
		polygon.setStrokeWidth(2);
		reDraw();
		getChildren().add(polygon);
		polygon.setOnMouseClicked(e -> map.mouseClickOnProvince(this, e));
	}

	public void reDraw() {
		if (getProvince().getTerrainType() == TerrainType.OCEAN) {
			drawGeoMode(polygon);
			return;
		}
		MapMode mapMode = map.getMapMode();
		switch (mapMode) {
		case POPULATION:
		case POPULATION_2:
			drawPopulationMode(polygon);
			break;
		case SOIL:
			drawSoilMode(polygon);
			break;
		case SOIL_2:
			drawSoilMode2(polygon);
			break;
		case SCIENCE_AGRICULTURE:
			drawScienceMode(polygon);
			break;
		case DISEASE:
			drawDesiaseMode(polygon);
			break;
		case GEO:
			drawGeoMode(polygon);
			break;
		default:
			break;
		}
	}

	private void drawScienceMode(Polygon polygon2) {
		drawGradientMode(polygon, map.getGame().getGameTransientStats().getMaxScienceAgricultureInProvince(),
				province.getScienceAgriculture(), false);
	}

	private void drawGeoMode(Polygon polygon) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader
				.getResourceAsStream("desktop/map/" + getProvince().getTerrainType().toString().toLowerCase() + ".png");
		Image texture = new Image(is);
		polygon.setFill(new ImagePattern(texture, 0, 0, 1, 1, true));
	}

	private void drawPopulationMode(Polygon polygon) {
		// TODO use red color while over population
		drawGradientMode(polygon, map.getGame().getGameTransientStats().getMaxPopulationInProvince(),
				province.getPopulationAmount(), MapMode.POPULATION_2.equals(map.getMapMode()));
	}

	private void drawSoilMode(Polygon polygon) {
		drawGradientMode(polygon, map.getGame().getGameTransientStats().getMaxSoilQuality(), province.getSoilQuality(),
				false);
	}

	private void drawSoilMode2(Polygon polygon) {
		// TODO blue color if fertility < 1
		GameTransientStats stats = map.getGame().getGameTransientStats();
		double minF = stats.getMinSoilFertility();
		drawGradientMode(polygon, stats.getMaxSoilFertility() - minF, province.getSoilFertility() - minF, true);
	}

	private void drawGradientMode(Polygon polygon, double maxValue, double provinceValue, boolean mode2) {
		double fraction = maxValue != 0 ? provinceValue / maxValue : 1;
		Paint pValue;
		if (fraction < 0.5) {
			if (mode2) {
				fraction = Math.sqrt(fraction);
			}
			pValue = new Color(Math.min(fraction * 2, 1), Math.min(fraction * 2, 1), 0, 1);
		} else {
			if (mode2) {
				fraction = fraction * fraction;
			}
			pValue = new Color(1, Math.min(1 - (fraction - 0.5) * 2, 1), 0, 1);
		}
		polygon.setFill(pValue);
	}

	private void drawDesiaseMode(Polygon polygon) {
		if (getProvince().getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			polygon.setFill(Color.RED);
		} else if (getProvince().getEvents().hasEventWithType(Event.EVENT_EPIDEMIC_PROTECTED)) {
			polygon.setFill(Color.GREEN);
		} else {
			polygon.setFill(Color.GREY);
		}
	}

	private void updatePoints() {
		for (int i = 0; i < SIDES; i++) {
			int angle_deg = 60 * i + 30;
			double angle_rad = Math.PI / 180 * angle_deg;
			points[i] = new Point2D(center.getX() + radius * Math.cos(angle_rad),
					center.getY() + radius * Math.sin(angle_rad));
		}
	}

	public static DProvince createDProvince(DWorldMap map, Province p, double provinceRadius) {
		return new DProvince(map, p, provinceRadius);
	}

	public void selectProvince(boolean isSelected) {
		if (isSelected) {
			DProvince.this.toFront();
			polygon.setStroke(STROKE_SELECTED_2_COLOR);
			if (timeline == null) {
				timeline = new Timeline(
						new KeyFrame(Duration.seconds(0.5), evt -> polygon.setStroke(STROKE_SELECTED_1_COLOR)),
						new KeyFrame(Duration.seconds(1), evt -> polygon.setStroke(STROKE_SELECTED_2_COLOR)));
				timeline.setCycleCount(Animation.INDEFINITE);
			}
			timeline.play();
		} else {
			if (timeline != null) {
				timeline.stop();
			}
			polygon.setStroke(STROKE_DEFAULT_COLOR);
		}
	}

	public Province getProvince() {
		return province;
	}

}
