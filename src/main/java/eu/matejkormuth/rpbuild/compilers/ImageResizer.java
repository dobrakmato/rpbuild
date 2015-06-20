/*
 *  rpbuild - RPBuild is a build system for Minecraft resource packs.
 *  Copyright (C) 2015 Matej Kormuth 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  "Minecraft" is a trademark of Mojang AB
 */
package eu.matejkormuth.rpbuild.compilers;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import eu.matejkormuth.rpbuild.BuildError;
import eu.matejkormuth.rpbuild.FileCompiler;
import eu.matejkormuth.rpbuild.OpenedFile;

public class ImageResizer extends FileCompiler {

	private int maxResolution;
	private int interpolationType; // AffineTransformOp

	@Override
	public void init() {
		String maxRes_s = this.getSetting("maxResolution").getValue();

		if (maxRes_s == null) {
			throw new BuildError(new RuntimeException(
					"Invalid configuration! MaxResoultion must be set!"));
		}

		maxResolution = Integer.valueOf(maxRes_s);

		if (maxResolution <= 0) {
			throw new BuildError(
					new RuntimeException(
							"Invalid configuration value! Please set maxResoultion to number higher then 0."));
		}

		String interpolation = this.getSetting("interpolation", "nearest")
				.getValue();

		if (interpolation.equalsIgnoreCase("nearest")) {
			interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
		} else if (interpolation.equalsIgnoreCase("bilinear")) {
			interpolationType = AffineTransformOp.TYPE_BILINEAR;
		} else if (interpolation.equalsIgnoreCase("bicubic")) {
			interpolationType = AffineTransformOp.TYPE_BICUBIC;
		} else {
			throw new BuildError(
					new RuntimeException(
							"Invalid configuration value! Please set interpolationType to 'nearest', 'bilinear' or 'bicubic'!"));
		}
	}

	@Override
	public void compile(OpenedFile file) throws Exception {

		// Resize only block textures and items. Resizing other files may crash
		// the game.
		if (!(file.getPath().startsWith("assets/minecraft/textures/blocks") || file
				.getPath().startsWith("assets/minecraft/textures/items"))) {
			return;
		}

		BufferedImage srcImg = ImageIO.read(new ByteArrayInputStream(file
				.getContents()));

		// Resize only files bigger than max. resolution.
		if (srcImg.getWidth() > maxResolution) {
			log.info("Resizing file: " + file.getPath().toString());

			float aspectRatio = (float) srcImg.getHeight()
					/ (float) srcImg.getWidth();
			int newHeight = (int) (aspectRatio * maxResolution);

			BufferedImage scaledImg = getScaledImage(srcImg, maxResolution,
					newHeight);

			// Save new image to file.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaledImg, "png", baos);
			baos.flush();
			file.setContents(baos.toByteArray());
			baos.close();
		}
	}

	private BufferedImage getScaledImage(BufferedImage image, int width,
			int height) throws IOException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleX = (double) width / imageWidth;
		double scaleY = (double) height / imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(
				scaleX, scaleY);
		AffineTransformOp scaleOp = new AffineTransformOp(scaleTransform,
				interpolationType);

		return scaleOp.filter(image,
				new BufferedImage(width, height, image.getType()));
	}

}
