/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.FULL_COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.PALETTE;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * What a piece of optimization; Performs incredibly fast Minecraft color conversion and dithering.
 *
 * @author jetp250, BananaPuncher714
 */
public class FloydDither implements DitherAlgorithm {

  FloydDither() {}

  private int getColorFromMinecraftPalette(final byte val) {
    return PALETTE[(val + 256) % 256];
  }

  private byte getBestColorIncludingTransparent(final int rgb) {
    return (rgb >>> 24 & 0xFF) == 0 ? 0 : this.getBestColor(rgb);
  }

  private byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  private byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private int getBestFullColor(final int red, final int green, final int blue) {
    return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  @Contract(pure = true)
  private byte @NotNull [] simplify(final int @NotNull [] buffer) {
    final byte[] map = new byte[buffer.length];
    for (int index = 0; index < buffer.length; index++) {
      final int rgb = buffer[index];
      final int red = rgb >> 16 & 0xFF;
      final int green = rgb >> 8 & 0xFF;
      final int blue = rgb & 0xFF;
      final byte ptr = this.getBestColor(red, green, blue);
      map[index] = ptr;
    }
    return map;
  }

  @Override
  public void dither(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasPrevX = x > 0;
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_r);
            buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
              buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
            }
            buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
            buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_r);
              buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
            }
          }
          buffer[index] = closest;
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasPrevX = x < widthMinus;
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_b);
            buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
              buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
            }
            buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
            buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_b);
              buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
            }
          }
          buffer[index] = closest;
        }
      }
    }
  }

  @Override
  public @NotNull BufferCarrier ditherIntoMinecraft(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    final ByteBuf data = Unpooled.buffer(buffer.length);
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasPrevX = x > 0;
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_r);
            buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
              buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
            }
            buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
            buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_r);
              buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
            }
          }
          data.setByte(index, this.getBestColor(closest));
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasPrevX = x < widthMinus;
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_b);
            buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
              buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
            }
            buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
            buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_b);
              buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
            }
          }
          data.setByte(index, this.getBestColor(closest));
        }
      }
    }
    return ByteBufCarrier.ofByteBufCarrier(data);
  }

  private int[] getRGBArray(@NotNull final BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }
}
