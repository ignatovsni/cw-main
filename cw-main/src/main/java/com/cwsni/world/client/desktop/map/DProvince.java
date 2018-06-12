package com.cwsni.world.client.desktop.map;

import java.io.InputStream;
import java.util.stream.Stream;

import com.cwsni.world.model.Province;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * Hexagon province
 * @author sni
 *
 */
public class DProvince extends Group {
	// https://www.redblobgames.com/grids/hexagons/  - how to calculate hex
	
    private static final Color STROKE_SELECTED_2_COLOR = Color.YELLOW;
	private static final Color STROKE_SELECTED_1_COLOR = Color.RED;
	private static final Color STROKE_DEFAULT_COLOR = Color.GRAY;

	public static final int SIDES = 6;

    private Point2D[] points = new Point2D[SIDES];
    private Point2D center;
    private double radius;
    private Polygon polygon;
    
    private final DWorldMap map;
    private final Province province;

    private boolean selected = false;
	
    
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
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("desktop/map/green.png");
		Image texture = new Image(is);

		polygon.setFill(new ImagePattern(texture, 0, 0, 1, 1, true));
		
		getChildren().add(polygon);		
		
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), evt -> polygon.setStroke(STROKE_SELECTED_1_COLOR)),
                new KeyFrame(Duration.seconds(1), evt -> polygon.setStroke(STROKE_SELECTED_2_COLOR)));
		timeline.setCycleCount(Animation.INDEFINITE);
		polygon.setOnMouseClicked(e -> {
			//e.consume();
			if (e.getButton() == MouseButton.PRIMARY) {
				DProvince.this.toFront();
				selected = !selected;
				if (selected) {
					timeline.play();
				} else {
					timeline.stop();
					polygon.setStroke(STROKE_DEFAULT_COLOR);				
				}
			}
		});
	}

    protected void updatePoints() {
        for (int i = 0; i < SIDES; i++) {
        	int angle_deg = 60 * i + 30;
        	double angle_rad = Math.PI / 180 * angle_deg;
        	points[i] = new Point2D(center.getX() + radius * Math.cos(angle_rad), center.getY() + radius * Math.sin(angle_rad));
        }
    }

	public static DProvince createDProvince(DWorldMap map, Province p, double provinceRadius) {
		return new DProvince(map, p, provinceRadius);
	}

}
