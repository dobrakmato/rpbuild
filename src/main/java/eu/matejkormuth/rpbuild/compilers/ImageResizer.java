/**
 * Minecraft resource pack compiler and assembler - rpBuild - Build system for Minecraft resource packs.
 * Copyright (c) 2015, Matej Kormuth <http://www.github.com/dobrakmato>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * "Minecraft" is a trademark of Mojang AB
 */
package eu.matejkormuth.rpbuild.compilers;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.OpenedFile;
import eu.matejkormuth.rpbuild.exceptions.BuildError;
import eu.matejkormuth.rpbuild.exceptions.InvalidSettingsError;

public class ImageResizer extends Compiler {

	private int maxResolution;
	private int interpolationType; // AffineTransformOp

	@Override
	public void onInit() throws InvalidSettingsError {

		// Initialize settings.
		String maxRes_s = this.getSetting("maxResolution").getValue();

		if (maxRes_s == null) {
			throw new InvalidSettingsError(
					"Invalid configuration! MaxResoultion must be set!");
		}

		maxResolution = Integer.valueOf(maxRes_s);

		if (maxResolution <= 0) {
			throw new InvalidSettingsError(
					"Please set maxResoultion to number higher then 0.");
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
			throw new InvalidSettingsError(
					"Please set interpolationType to 'nearest', 'bilinear' or 'bicubic'!");
		}
	}

	@Override
	public void compile(OpenedFile file) throws BuildError {
		// Resize only block textures and items. Resizing other files may crash
		// the game.
		if (!(file.getPath().startsWith("assets/minecraft/textures/blocks") || file
				.getPath().startsWith("assets/minecraft/textures/items"))) {
			return;
		}

		try {
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
		} catch (IOException e) {
			throw new BuildError(e);
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
				this.interpolationType);

		return scaleOp.filter(image,
				new BufferedImage(width, height, image.getType()));
	}

}
