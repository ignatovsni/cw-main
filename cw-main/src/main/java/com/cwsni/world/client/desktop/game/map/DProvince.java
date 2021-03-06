package com.cwsni.world.client.desktop.game.map;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.Point;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Culture;
import com.cwsni.world.model.engine.GameTransientStats;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.State;
import com.cwsni.world.model.engine.relationships.RTribute;
import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;
import com.cwsni.world.util.ComparisonTool;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/**
 * Hexagon province
 * 
 * @author sni
 *
 */
class DProvince extends Group {
	// https://www.redblobgames.com/grids/hexagons/ - how to calculate hex

	private static final Color STROKE_SELECTED_1_COLOR = new Color(1, 0, 0, 0.8);
	private static final Color STROKE_SELECTED_2_COLOR = new Color(1, 1, 0, 0.8);
	private static final Color STROKE_DEFAULT_COLOR = Color.GRAY;

	private static final Color COLOR_NONE = new Color(0, 0, 0, 1);
	private static final Color COLOR_NO_COUNTRY_BUT_POPS = new Color(0.3, 0.3, 0.3, 1);

	private static final Color BORDER_COLOR = Color.BLACK;

	public static final int SIDES = 6;

	private Point2D[] points = new Point2D[SIDES];
	private Point2D center;
	private double radius;
	private Timeline timeline;
	private final DWorldMap map;
	private final Province province;

	private Polygon polygon;
	private Shape capitalPolygon;
	private Shape armyPolygon;

	private MapMode prevMode;

	private DProvince(DWorldMap map, Province province, double provinceRadius) {
		this.map = map;
		this.province = province;
		this.center = new Point2D(province.getCenter().getX(), province.getCenter().getY());
		this.radius = provinceRadius;
		updatePoints(points, radius);
		createVisualElements();
	}

	private void createVisualElements() {
		polygon = new Polygon();
		Stream.of(points).forEach(p -> polygon.getPoints().addAll(p.getX(), p.getY()));
		polygon.setStroke(STROKE_DEFAULT_COLOR);
		polygon.setStrokeWidth(1);
		getChildren().add(polygon);
		polygon.setOnMouseClicked(e -> map.mouseClickOnProvince(this, e));
	}

	public void draw() {
		MapMode mapMode = map.getMapMode();
		if (!mapMode.isEventType()) {
			drawProvinceByDefault(mapMode);
		} else {
			drawProvinceByEvent(mapMode);
		}
		drawCapital();
		drawArmy();
		prevMode = mapMode;
	}

	private void drawProvinceByEvent(MapMode mapMode) {
		List<Double> colorList = map.getGameEventHandler().getColorForProvince(map.getGame(), province, mapMode);
		if (colorList != null) {
			Color color = new Color(colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3));
			fillPolygon(polygon, color);
		} else {
			drawProvinceByDefault(MapMode.DEFAULT_MODE);
		}
	}

	private void drawProvinceByDefault(MapMode mapMode) {
		if (getProvince().getTerrainType().isMountain()
				|| getProvince().getTerrainType().isWater() && !MapModeEnum.REACHABLE_LANDS.equals(mapMode.getMode())) {
			if (!mapMode.equals(prevMode)) {
				drawGeoMode(polygon);
			}
		} else {
			switch (mapMode.getMode()) {
			case POPULATION:
				drawPopulationMode(polygon);
				break;
			case STATES:
				drawStatesMode(polygon);
				break;
			case WEALTH:
				drawWealthMode(polygon);
				break;
			case POLITICAL:
				drawPoliticalMode(polygon);
				break;
			case DIPLOMACY:
				drawDiplomacyMode(polygon);
				break;
			case REACHABLE_LANDS:
				drawDiplomacyReachableLandsMode(polygon);
				break;
			case GOVERNMENT_INFLUENCE:
				drawGovInfluenceMode(polygon);
				break;
			case LOYALTY:
				drawLoyaltyMode(polygon);
				break;
			case LOYALTY_DANGEROUS:
				drawLoyaltyDangerousMode(polygon);
				break;
			case CULTURE:
				drawCultureMode(polygon);
				break;
			case INFRASTRUCTURE:
				drawInfrastructureMode(polygon);
				break;
			case SOIL_AREA:
				drawSoilAreMode(polygon);
				break;
			case SOIL_FERTILITY:
				drawSoilFertilityMode(polygon);
				break;
			case SOIL_NATURAL_FERTILITY:
				drawSoilNaturalFertilityMode(polygon);
				break;
			case SCIENCE_AGRICULTURE:
				drawScienceAgricultureMode(polygon);
				break;
			case SCIENCE_MEDICINE:
				drawScienceMedicineMode(polygon);
				break;
			case SCIENCE_ADMINISTRATION:
				drawScienceAdministrationMode(polygon);
				break;
			case GEO:
				if (!mapMode.equals(prevMode)) {
					drawGeoMode(polygon);
				}
				break;
			default:
				break;
			}
		}
	}

	private void drawCapital() {
		boolean isCapital = province.getCountryId() != null && province.getCountry().getCapital() != null
				&& ComparisonTool.isEqual(province.getCountry().getCapital().getId(), province.getId());
		if (isCapital) {
			if (capitalPolygon == null) {
				Point2D[] points = new Point2D[SIDES];
				updatePoints(points, radius * 0.6);
				Polygon pol = new Polygon();				
				Stream.of(points).forEach(p -> pol.getPoints().addAll(p.getX(), p.getY()));
				pol.setStrokeWidth(radius / 10);
				pol.setStroke(Color.BLACK);
				pol.setFill(province.getCountry().getColor().getJavaFxColor());
				capitalPolygon = pol;
				getChildren().add(capitalPolygon);
				capitalPolygon.setOnMouseClicked(e -> {
					map.mouseClickOnProvince(this, e);
				});
			}
			capitalPolygon.toFront();
		} else if (capitalPolygon != null) {
			getChildren().remove(capitalPolygon);
			capitalPolygon = null;
		}
	}

	private void drawArmy() {
		boolean isArmy = !province.getArmies().isEmpty();
		if (isArmy) {
			if (armyPolygon == null) {
				Double[] armyPoints = new Double[] { 0.0, 0.0, 10.0, 0.0, 10.0, 10.0, 5.0, 13.0, 0.0, 10.0 };
				for (int i = 0; i < armyPoints.length - 1; i = i + 2) {
					armyPoints[i] = armyPoints[i] * radius * 0.05 + province.getCenter().getX() - radius * 0.25;
					armyPoints[i + 1] = armyPoints[i + 1] * radius * 0.05 + province.getCenter().getY() - radius * 0.25;
				}
				Polygon p = new Polygon();
				p.getPoints().addAll(armyPoints);
				p.setStrokeWidth(radius / 15 );
				p.setStroke(Color.BLACK);
				armyPolygon = p;
				getChildren().add(armyPolygon);
				armyPolygon.setOnMouseClicked(e -> {
					map.mouseClickOnProvince(this, e);
				});
			}
			if (province.getArmies().size() == 1) {
				armyPolygon.setFill(province.getArmies().get(0).getCountry().getColor().getJavaFxColor());
			} else {
				armyPolygon.setFill(Color.BLACK);
			}
			armyPolygon.toFront();
		} else if (armyPolygon != null) {
			getChildren().remove(armyPolygon);
			armyPolygon = null;
		}
	}

	private void drawPoliticalMode(Polygon polygon) {
		Country country = province.getCountry();
		Color color;
		if (country != null) {
			color = country.getColor().getJavaFxColor();
		} else {
			if (province.getPopulationAmount() == 0) {
				color = COLOR_NONE;
			} else {
				color = COLOR_NO_COUNTRY_BUT_POPS;
			}
		}
		fillPolygon(polygon, color);
	}

	private void drawStatesMode(Polygon polygon) {
		State state = province.getState();
		Color color;
		if (state != null) {
			color = state.getColor().getJavaFxColor();
		} else {
			color = COLOR_NONE;
		}
		fillPolygon(polygon, color);
	}

	private void drawDiplomacyMode(Polygon polygon) {
		if (province.getCountry() == null) {
			Color color;
			if (province.getPopulationAmount() == 0) {
				color = COLOR_NONE;
			} else {
				color = COLOR_NO_COUNTRY_BUT_POPS;
			}
			fillPolygon(polygon, color);
			return;
		}
		Province selectedProvince = map.getSelectedProvince();
		if (selectedProvince != null && selectedProvince.getCountry() != null) {
			drawDiplomacyModeForSelectedCountry(polygon, selectedProvince.getCountry());
		} else {
			drawDiplomacyModeForWorld(polygon);
		}
	}

	private void drawDiplomacyModeForWorld(Polygon polygon) {
		Integer countryId = province.getCountryId();
		Collection<RWar> wars = map.getGame().getRelationships().getWars();
		Optional<RWar> first = wars.stream().filter(war -> ComparisonTool.isEqual(war.getMasterId(), countryId)
				|| ComparisonTool.isEqual(war.getSlaveId(), countryId)).findFirst();
		if (first.isPresent()) {
			fillPolygon(polygon, Color.RED);
		} else {
			fillPolygon(polygon, Color.GREY);
		}
	}

	private void drawDiplomacyModeForSelectedCountry(Polygon polygon, Country selectedCountry) {
		Country country = province.getCountry();
		Color color;
		if (country.equals(selectedCountry)) {
			color = Color.GREEN;
		} else {
			RelationshipsCollection relationships = map.getGame().getRelationships();
			if (relationships.getCountriesWithWar(selectedCountry.getId()).containsKey(country.getId())) {
				color = Color.RED;
			} else {
				RTribute tribute = relationships.getCountriesWithTribute(selectedCountry.getId()).get(country.getId());
				if (tribute != null) {
					if (ComparisonTool.isEqual(selectedCountry.getId(), tribute.getMasterId())) {
						color = Color.BLUE;
					} else {
						color = Color.AQUAMARINE;
					}
				} else if (relationships.getCountriesWithTruce(selectedCountry.getId()).containsKey(country.getId())) {
					color = Color.YELLOW;
				} else {
					color = Color.GREY;
				}
			}
		}
		fillPolygon(polygon, color);
	}

	private void drawDiplomacyReachableLandsMode(Polygon polygon) {
		Province selectedProvince = map.getSelectedProvince();
		if (selectedProvince == null || selectedProvince.getCountry() == null) {
			drawGeoMode(polygon);
			return;
		}
		Country selectedCountry = selectedProvince.getCountry();
		Province thisProvince = getProvince();
		if (selectedCountry.getCapital() != null) {
			double distance = map.getGame().getMap()
					.findDistanceApproximateCountOfProvinces(selectedCountry.getCapital(), getProvince());
			double maxDistance = selectedCountry.getMaxReachableDistance();
			if (distance > maxDistance) {
				drawGeoMode(polygon);
				return;
			}
		}
		if (thisProvince.getTerrainType().isWater()) {
			if (selectedCountry.getReachableWaterProvinces().contains(thisProvince)) {
				fillPolygon(polygon, Color.AQUA);
			} else {
				drawGeoMode(polygon);
			}
		} else if (selectedCountry.equals(thisProvince.getCountry())) {
			drawDiplomacyMode(polygon);
		} else if (selectedCountry.getReachableLandBorderAlienProvs().contains(thisProvince)
				|| selectedCountry.getReachableLandProvincesThroughWater().contains(thisProvince)) {
			if (map.getGame().getRelationships().hasAgreement(selectedCountry.getId(), thisProvince.getCountryId())) {
				drawDiplomacyMode(polygon);
			} else {
				// selected country does not have agreements, so probably does not have the
				// right to cross lands
				fillPolygon(polygon, Color.ANTIQUEWHITE);
			}
		} else {
			drawGeoMode(polygon);
		}
	}

	private void drawCultureMode(Polygon polygon) {
		Culture cult = province.getCulture();
		Color color;
		if (cult != null) {
			color = new Color(cult.getRed() / 255.0, cult.getGreen() / 255.0, cult.getBlue() / 255.0, 1);
		} else {
			color = COLOR_NONE;
		}
		fillPolygon(polygon, color);
	}

	private void drawLoyaltyMode(Polygon polygon) {
		drawGradientMode(polygon, DataPopulation.LOYALTY_MAX, province.getLoyaltyToCountry(), false);
	}

	private void drawLoyaltyDangerousMode(Polygon polygon) {
		double danger = Math.min(Math.max(province.getLoyaltyToState() - province.getLoyaltyToCountry(), 0),
				DataPopulation.LOYALTY_MAX);
		drawGradientMode(polygon, DataPopulation.LOYALTY_MAX, danger, false);
	}

	private void drawInfrastructureMode(Polygon polygon) {
		double infrastructureMaxInProvince = map.getGame().getGameTransientStats().getInfrastructureMaxInProvince();
		double infrastructureAvgInProvince = map.getGame().getGameTransientStats().getInfrastructureAvgInProvince();
		double infrastructureMedianInProvince = map.getGame().getGameTransientStats()
				.getInfrastructureMedianInProvince();
		double infrastructure = province.getInfrastructurePercent();
		int populationAmount = province.getPopulationAmount();
		int populationMedianInProvince = map.getGame().getGameTransientStats().getPopulationMedianInProvince();
		if (populationAmount > 0 && populationAmount < populationMedianInProvince
				&& infrastructure > infrastructureMedianInProvince) {
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

	private void drawScienceAdministrationMode(Polygon polygon) {
		drawGradientModeForMedian(polygon,
				map.getGame().getGameTransientStats().getScienceAdministrationMaxInProvince(),
				map.getGame().getGameTransientStats().getScienceAdministrationAvgInProvince(),
				map.getGame().getGameTransientStats().getScienceAdministrationMedianInProvince(),
				province.getScienceAdministration());
	}

	private void drawGeoMode(Polygon polygon) {
		String terrainType = getProvince().getTerrainType().toString().toLowerCase();
		ImagePattern image = map.getTextures().get(terrainType);
		if (image == null) {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream("desktop/map/" + terrainType + ".png");
			Image texture = new Image(is);
			image = new ImagePattern(texture, 0, 0, 1, 1, true);
			map.getTextures().put(terrainType, image);
		}
		polygon.setFill(image);
		this.prevColor = null;
	}

	private void drawPopulationMode(Polygon polygon) {
		drawGradientModeForMedian(polygon, map.getGame().getGameTransientStats().getPopulationMaxInProvince(),
				map.getGame().getGameTransientStats().getPopulationAvgInProvince(),
				map.getGame().getGameTransientStats().getPopulationMedianInProvince(), province.getPopulationAmount());
	}

	private void drawGovInfluenceMode(Polygon polygon) {
		drawGradientMode(polygon, 1.0, province.getGovernmentInfluence(), false);

		// drawGradientModeForMedian(polygon,
		// map.getGame().getGameTransientStats().getGovInfluenceMaxInProvince(),
		// map.getGame().getGameTransientStats().getGovInfluenceAvgInProvince(),
		// map.getGame().getGameTransientStats().getGovInfluenceMedianInProvince(),
		// province.getGovernmentInfluence());
	}

	private void drawWealthMode(Polygon polygon) {
		drawGradientMode(polygon, 1.0, province.getWealthLevel(), false);
	}

	private void drawSoilFertilityMode(Polygon polygon) {
		GameTransientStats stats = map.getGame().getGameTransientStats();
		drawGradientModeForMedian(polygon, stats.getSoilFertilityMax(), stats.getSoilFertilityAvg(),
				stats.getSoilFertilityMedian(), province.getSoilFertility());
	}

	private void drawSoilNaturalFertilityMode(Polygon polygon) {
		GameTransientStats stats = map.getGame().getGameTransientStats();
		drawGradientModeForMedian(polygon, stats.getSoilNaturalFertilityMax(), stats.getSoilNaturalFertilityAvg(),
				stats.getSoilNaturalFertilityMedian(), province.getSoilNaturalFertility());
	}

	private void drawSoilAreMode(Polygon polygon) {
		GameTransientStats stats = map.getGame().getGameTransientStats();
		drawGradientModeForMedian(polygon, stats.getSoilAreaMax(), stats.getSoilAreaAvg(), stats.getSoilAreaMedian(),
				province.getSoilArea());
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
			// fraction = fraction * fraction;
			// fraction = Math.sqrt(fraction);
			pValue = new Color(1, Math.min(1 - fraction, 1), 0, 1);
		}
		fillPolygon(polygon, pValue);
	}

	private void updatePoints(Point2D[] points, double radius) {
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
			polygon.toFront();
			if (capitalPolygon != null) {
				capitalPolygon.toFront();
			}
			if (armyPolygon != null) {
				armyPolygon.toFront();
			}
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
		if (prevColor != color
				&& (prevColor == null || color.getBlue() != prevColor.getBlue() || color.getRed() != prevColor.getRed()
						|| color.getGreen() != prevColor.getGreen() || color.getOpacity() != prevColor.getOpacity())) {
			polygon.setFill(color);
		}
		prevColor = color;
	}

	protected Line createBorderLine(Province neighbor) {
		int neighborPosition = whatNeighborPosition(neighbor);
		int borderFromIdx = neighborPosition;
		int borderToIdx = (neighborPosition + 1) % SIDES;
		Line line = new Line(points[borderFromIdx].getX(), points[borderFromIdx].getY(), points[borderToIdx].getX(),
				points[borderToIdx].getY());
		line.setStroke(BORDER_COLOR);
		line.getStrokeDashArray().addAll(2d);
		return line;
	}

	private int whatNeighborPosition(Province neighbor) {
		Point nc = neighbor.getCenter();
		if (nc.getX() > center.getX()) {
			if ((nc.getY() - center.getY()) > 1) {
				return 0;
			} else if ((nc.getY() - center.getY()) < -1) {
				return 4;
			} else {
				return 5;
			}
		} else {
			if ((nc.getY() - center.getY()) > 1) {
				return 1;
			} else if ((nc.getY() - center.getY()) < -1) {
				return 3;
			} else {
				return 2;
			}
		}
	}

}
