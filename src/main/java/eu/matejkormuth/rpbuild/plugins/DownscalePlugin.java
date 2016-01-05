/**
 * rpBuild 2 - Improved build system for Minecraft resource packs.
 * Copyright (c) 2015 - 2016, Matej Kormuth <http://www.github.com/dobrakmato>
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
package eu.matejkormuth.rpbuild.plugins;

import com.typesafe.config.Config;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginType;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.exceptions.InvalidConfigurationException;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Provides basic downscaling.
 */
public class DownscalePlugin extends Plugin {

    private static final String NAME = "rpbuild-downscale-plugin";
    private static final String VERSION = "1.0";
    private static final String AUTHOR = "Matej Kormuth";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORM_FILES;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public void transform(Config config, OpenedFile file) {
        int maxResolution = 256;
        String interpolation = "nearest";

        if (config.hasPath("maxResolution")) {
            maxResolution = config.getInt("maxResolution");
        }

        if (config.hasPath("interpolation")) {
            interpolation = config.getString("interpolation");
        }

        int interpolationType;
        if (interpolation.equalsIgnoreCase("nearest")) {
            interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        } else if (interpolation.equalsIgnoreCase("bilinear")) {
            interpolationType = AffineTransformOp.TYPE_BILINEAR;
        } else if (interpolation.equalsIgnoreCase("bicubic")) {
            interpolationType = AffineTransformOp.TYPE_BICUBIC;
        } else {
            throw new InvalidConfigurationException("Please set interpolation to 'nearest', 'bilinear' or 'bicubic'!");
        }

        try {
            BufferedImage srcImg = ImageIO.read(new ByteArrayInputStream(file.getData()));

            // Resize only files bigger than max. resolution.
            if (srcImg.getWidth() > maxResolution) {

                float aspectRatio = (float) srcImg.getHeight() / (float) srcImg.getWidth();
                int newHeight = (int) (aspectRatio * maxResolution);

                BufferedImage scaledImg = getScaledImage(srcImg, maxResolution, newHeight, interpolationType);

                // Save new image to file.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaledImg, "png", baos);
                baos.flush();
                file.setData(baos.toByteArray());
                baos.close();

            }
        } catch (IOException e) {
            throw new TaskException(e);
        }

    }

    private BufferedImage getScaledImage(BufferedImage image, int width,
                                         int height, int interpolationType) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleTransform, interpolationType);

        return scaleOp.filter(image, new BufferedImage(width, height, image.getType()));
    }
}
