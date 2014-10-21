package com.aizhizu.service.house;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePane extends JPanel {
	private static final long serialVersionUID = 5397776194796791122L;
	Image image;
	BufferedImage bufImage;
	BufferedImage originalBufImage;
	Graphics2D bufImageG;

	public void loadImage(String fileName) {
		this.image = getToolkit().getImage(getClass().getResource(fileName));
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(this.image, 0);
		try {
			mt.waitForAll();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.originalBufImage = new BufferedImage(this.image.getWidth(this),
				this.image.getHeight(this), 2);
		this.bufImage = this.originalBufImage;
		this.bufImageG = this.bufImage.createGraphics();
		this.bufImageG.drawImage(this.image, 0, 0, this);
		repaint();
	}

	public void ratoteImage(double angle) {
		if (this.bufImage == null)
			return;
		BufferedImage filteredBufImage = new BufferedImage(
				this.image.getWidth(this), this.image.getHeight(this), 2);
		AffineTransform transform = new AffineTransform();
		transform.rotate(angle, this.image.getWidth(this) / 2,
				this.image.getHeight(this) / 2);
		AffineTransformOp imageOp = new AffineTransformOp(transform, null);
		imageOp.filter(this.originalBufImage, filteredBufImage);
		this.bufImage = filteredBufImage;
		repaint();
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		if (this.bufImage != null) {
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(this.bufImage,
					getWidth() / 2 - this.bufImage.getWidth() / 2, getHeight()
							/ 2 - this.bufImage.getHeight() / 2, this);
		}
	}
}