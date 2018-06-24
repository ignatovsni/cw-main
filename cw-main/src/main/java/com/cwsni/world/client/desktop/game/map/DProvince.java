package com.cwsni.world.client.desktop.game.map;

import java.io.InputStream;
import java.util.stream.Stream;

import com.cwsni.world.model.Culture;
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

	private MapMode prevMode;

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
		MapMode mapMode = map.getMapMode();
		if (getProvince().getTerrainType() == TerrainType.OCEAN) {
			if (mapMode != prevMode) {
				drawGeoMode(polygon);
			}
			return;
		}
		switch (mapMode) {
		case POPULATION:
		case POPULATION_2:
			drawPopulationMode(polygon);
			break;
		case CULTURE:
			drawCultureMode(polygon);
			break;
		case INFRASTRUCTURE:
			drawInfrastructureMode(polygon);
			break;
		case SOIL:
			drawSoilFertilityMode(polygon);
			break;
		case SOIL_2:
			drawSoilQualityMode(polygon);
			break;
		case SCIENCE_AGRICULTURE:
			drawScienceAgricultureMode(polygon);
			break;
		case SCIENCE_MEDICINE:
			drawScienceMedicineMode(polygon);
			break;
		case DISEASE:
			drawDiseaseMode(polygon);
			break;
		case GEO:
			if (mapMode != prevMode) {
				drawGeoMode(polygon);
			}
			break;
		default:
			break;
		}
		prevMode = mapMode;
	}

	private void drawCultureMode(Polygon polygon) {
		Culture cult = province.getCulture();
		Color color;
		if (cult != null) {
			color = new Color(cult.getRed() / 255.0, cult.getGreen() / 255.0, cult.getBlue() / 255.0, 1);
		} else {
			color = new Color(0, 0, 0, 1);
		}
		fillPolygon(polygon, color);
	}

	private void drawInfrastructureMode(Polygon polygon) {
		double infrastructureMaxInProvince = map.getGame().getGameTransientStats().getInfrastructureMaxInProvince();
		double infrastructureAvgInProvince = map.getGame().getGameTransientStats().getInfrastructureAvgInProvince();
		double infrastructureMedianInProvince = map.getGame().getGameTransientStats()
				.getInfrastructureMedianInProvince();
		double infrastructure = province.getInfrastructure();
		int populationAmount = province.getPopulationAmount();
		if (infrastructure >= map.getGame().getGameParams().getInfrastructureMaxValue()
				|| (populationAmount < 1000 && populationAmount > 0)) {
			// too avoid showing as maximized that provinces where population fall very
			// quickly
			infrastructure = infrastructureMedianInProvince;
		}
		drawGradientModeForMedian(polygon, infrastructureMaxInProvince, infrastructureAvgInProvince,
				infrastructureMedianInProvince, infrastructure);
	}

	private void drawScienceAgricultureMode(Polygon polygon) {
		drawGradientModeForMedian(polygon, map.getGame().getGameTransientStats().getScienceAgricultureMaxInProvince(),
				map.getGame().getGameTransientStats().getScienceAgricultureAvgInProvince(),
				map.getGame().getGameTransientStats().getScienceAgricultureMedianInProvince(),
				province.getScienceAgriculture());
	}

	private void drawScienceMedicineMode(Polygon polygon) {
		drawGradientModeForMedian(polygon, map.getGame().getGameTransientStats().getScienceMedicineMaxInProvince(),
				map.getGame().getGameTransientStats().getScienceMedicineAvgInProvince(),
				map.getGame().getGameTransientStats().getScienceMedicineMedianInProvince(),
				province.getScienceMedicine());
	}

	private void drawGeoMode(Polygon polygon) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader
				.getResourceAsStream("desktop/map/" + getProvince().getTerrainType().toString().toLowerCase() + ".png");
		Image texture = new Image(is);
		polygon.setFill(new ImagePattern(texture, 0, 0, 1, 1, true));
		this.prevColor = null;
	}

	private void drawPopulationMode(Polygon polygon) {
		if (MapMode.POPULATION_2.equals(map.getMapMode())) {
			drawGradientMode(polygon, map.getGame().getGameTransientStats().getPopulationMaxInProvince(),
					province.getPopulationAmount(), false);
		} else {
			drawGradientModeForMedian(polygon, map.getGame().getGameTransientStats().getPopulationMaxInProvince(),
					map.getGame().getGameTransientStats().getPopulationAvgInProvince(),
					map.getGame().getGameTransientStats().getPopulationMedianInProvince(),
					province.getPopulationAmount());
		}
	}

	private void drawSoilQualityMode(Polygon polygon) {
		drawGradientMode(polygon, map.getGame().getGameTransientStats().getSoilQualityMax(), province.getSoilQuality(),
				false);
	}

	private void drawSoilFertilityMode(Polygon polygon) {
		// TODO blue color if fertility < 1
		GameTransientStats stats = map.getGame().getGameTransientStats();
		drawGradientModeForMedian(polygon, stats.getSoilFertilityMax(), stats.getSoilFertilityAvg(),
				stats.getSoilFertilityMedian(), province.getSoilFertility());
	}

	private void drawGradientMode(Polygon polygon, double maxValue, double provinceValue, boolean mode2) {
		double fraction = maxValue != 0 ? provinceValue / maxValue : 1;
		fraction = Math.min(fraction, 1); // sometimes data can be old
		Color pValue;
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
		fillPolygon(polygon, pValue);
	}

	private void drawGradientModeForMedian(Polygon polygon, double maxValue, double avgValue, double medianValue,
			double provinceValue) {
		// double baseValue = Math.max(medianValue, avgValue);
		double baseValue = medianValue;
		provinceValue = Math.min(provinceValue, maxValue); // sometimes provinceValue can be more than maxValue
		Color pValue;
		if (provinceValue <= baseValue) {
			double fraction = baseValue != 0 ? provinceValue / baseValue : 0;
			// fraction = Math.sqrt(fraction);
			fraction = fraction * fraction;
			pValue = new Color(Math.min(fraction, 1), Math.min(fraction, 1), 0, 1);
		} else {
			provinceValue -= baseValue;
			maxValue -= baseValue;
			double fraction = maxValue != 0 ? provinceValue / maxValue : 0;
			fraction = fraction * fraction;
			pValue = new Color(1, Math.min(1 - fraction, 1), 0, 1);
		}
		fillPolygon(polygon, pValue);
	}

	private void drawDiseaseMode(Polygon polygon) {
		if (getProvince().getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			fillPolygon(polygon, Color.RED);
		} else if (getProvince().getEvents().hasEventWithType(Event.EVENT_EPIDEMIC_PROTECTED)) {
			fillPolygon(polygon, Color.GREEN);
		} else {
			fillPolygon(polygon, Color.GREY);
			fillPolygon(polygon, Color.GREY);
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

	private Color prevColor;

	private void fillPolygon(Polygon polygon, Color color) {
		if (prevColor == null || color.getBlue() != prevColor.getBlue() || color.getRed() != prevColor.getRed()
				|| color.getGreen() != prevColor.getGreen() || color.getOpacity() != prevColor.getOpacity()) {
			polygon.setFill(color);
		}
		prevColor = color;
	}

}
