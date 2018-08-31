package com.cwsni.world.client.desktop.util;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

// Thanks to
// https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position/44314455#44314455
public class ZoomableScrollPane extends ScrollPane {
	private double scaleValue = 1;
	private double zoomIntensity = 0.02;
	private Node target;
	private Node zoomNode;

	public ZoomableScrollPane() {
		super();
	}

	public ZoomableScrollPane(Node target) {
		super();
		setTarget(target);
	}

	public void setTarget(Node target) {
		this.target = target;
		this.zoomNode = new Group(target);
		setContent(outerNode(zoomNode));
		setPannable(true);
		updateScale();
	}

	private Node outerNode(Node node) {
		Node outerNode = centeredNode(node);
		outerNode.setOnScroll(e -> {
			if (e.isControlDown()) {
				e.consume();
				onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
			}
		});
		return outerNode;
	}

	private Node centeredNode(Node node) {
		VBox vBox = new VBox(node);
		vBox.setAlignment(Pos.CENTER);
		return vBox;
	}

	private void updateScale() {
		target.setScaleX(scaleValue);
		target.setScaleY(scaleValue);
	}

	private void onScroll(double wheelDelta, Point2D mousePoint) {
		double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

		Bounds innerBounds = zoomNode.getLayoutBounds();
		Bounds viewportBounds = getViewportBounds();

		zoomFactor = Math.max(zoomFactor, Math.min(viewportBounds.getWidth() / innerBounds.getWidth(),
				viewportBounds.getHeight() / innerBounds.getHeight()));

		// calculate pixel offsets from [0, 1] range
		double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
		double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

		// Sometimes scrollbar stucks with NaN values
		if (Double.isNaN(valX)) {
			valX = 0;
		}
		if (Double.isNaN(valY)) {
			valY = 0;
		}

		scaleValue = scaleValue * zoomFactor;

		updateScale();
		this.layout(); // refresh ScrollPane scroll positions & target bounds

		// convert target coordinates to zoomTarget coordinates
		Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

		// calculate adjustment of scroll position (pixels)
		Point2D adjustment = target.getLocalToParentTransform()
				.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

		// convert back to [0, 1] range
		// (too large/small values are automatically corrected by ScrollPane)
		Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
		this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
		this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
	}

	public void ensureVisible(Node node) {
		double width = getContent().getBoundsInLocal().getWidth();
		double height = getContent().getBoundsInLocal().getHeight();

		double x = node.getBoundsInParent().getMaxX();
		double y = node.getBoundsInParent().getMaxY();

		// scrolling values range from 0 to 1
		setVvalue(y / height);
		setHvalue(x / width);
	}

	public void scaleToDefault() {
		scaleValue = 1;
		updateScale();
	}

	public void scaleToFitAllContent() {
		Bounds innerBounds = zoomNode.getLayoutBounds();
		Bounds viewportBounds = getViewportBounds();
		double zoomFactor = Math.min(viewportBounds.getWidth() / innerBounds.getWidth(),
				viewportBounds.getHeight() / innerBounds.getHeight());
		scaleValue = scaleValue * zoomFactor;
		updateScale();
	}

}