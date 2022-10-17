package org.jmle;

import bmpio.BmpImage;
import bmpio.BmpWriter;
import bmpio.BufferedRgb888Image;
import bmpio.Rgb888Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main2 {
    public static void main(String[] args) throws Exception {
        if ("dat".equals(args[0])) {
            extractDat(args[1], args[2]);
        } else if ("spr".equals(args[0])) {
            extractSpr(args[1], args[2]);
        } else if ("pal".equals(args[0])) {
            printPalette(getFileBytes(args[1]));
        }
    }

    private static void extractDat(String datFilePath, String palFilePath) throws Exception {
        byte[] datBytes = getFileBytes(datFilePath);
        byte[] palBytes = getFileBytes(palFilePath);

        Rgb888Image image = readDatFile(datBytes, palBytes);

        BmpImage bmp = new BmpImage();
        bmp.image = image;
        FileOutputStream out = new FileOutputStream(String.format("%s.bmp", datFilePath));
        BmpWriter.write(out, bmp);
    }

    private static void extractSpr(String sprFilePath, String palFilePath) throws Exception {
        BmpImage bmp = new BmpImage();
        bmp.image = createSprImage(sprFilePath, palFilePath);
        FileOutputStream out = new FileOutputStream(String.format("%s.bmp", sprFilePath));
        BmpWriter.write(out, bmp);
    }

    private static Rgb888Image createSprImage(String sprFilePath, String palFilePath) throws Exception {
        byte[] sprBytes = getFileBytes(sprFilePath);
        byte[] palBytes = getFileBytes(palFilePath);

        int sprOffset   = (sprBytes[0] << 4) & 0xFF;
        int sprWidth    = (sprBytes[sprOffset + 1] | sprBytes[sprOffset]) & 0xFF;
        int sprHeight   = (sprBytes[sprOffset + 3] | sprBytes[sprOffset + 2]) & 0xFF;
        byte[][] planes = new byte[4][(sprWidth / 2) * sprHeight];         // horizontal shrink caused by mode X

        int i = sprHeight;              // cx (size.y)
        int j = sprOffset + 10;         // si (tracks position in original data)
        int k = 0;                      // di (tracks position in new data)
        int p = 0;                      // plane count (Mode X)
        while (p < 4) {
            byte[] plane = planes[p];
            while (i > 0) {
                int b = sprBytes[j++];

                if (b == 0) {                   // 0 == transparency
                    k += (4 - (k % 4));
                    i--;
                } else if (b > 0) {
                    do {
                        int reps = b;
                        while (reps > 0) {      // rep movsb
                            try {
                                plane[k++] = sprBytes[j++];
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println("jarl");
                            }
                            reps--;
                        }
                        b = sprBytes[j++];
                        b = ~b + 1;             // neg al
                        if (b == 0) {
                            break;
                        }
                        k += b;
                        b = sprBytes[j++];
                    } while (b != 0);

                    k += (k % 4 != 0) ? (4 - (k % 4)) : 0;
                    i--;
                } else {
                    b = ~b + 1;                 // neg al
                    do {
                        k += b;
                        b = sprBytes[j++];
                        int reps = b;
                        while (reps > 0) {      // rep movsb
                            try {
                                plane[k++] = sprBytes[j++];
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println("jarl");
                            }
                            reps--;
                        }
                        b = sprBytes[j++];
                        b = ~b + 1;             // neg al
                    } while (b != 0);

                    k += (k % 4 != 0) ? (4 - (k % 4)) : 0;      // prevents from adding 4 if k is "at the end of the plane"
                    i--;
                }
            }
            i = sprHeight;                  // another full loop of size.y
            p++;                                // "select next plane"
            k = 0;                              // reset dest index (new plane)
        }

        byte[] sprite = mergePlanes(planes);
        BufferedRgb888Image image = new BufferedRgb888Image(sprWidth, sprHeight);
        for (int y = 0; y < sprHeight; y++) {
            for (int x = 0; x < sprWidth; x++) {
                image.setRgb888Pixel(x, y, getColor(sprite[sprWidth * y + x], palBytes));
            }
        }

        return image;
    }


    public static byte[] mergePlanes(byte[][] planes) {
        byte[] sprite = new byte[planes[0].length * 4];
        for (int p = 0; p < planes.length; p++) {
            byte[] plane = planes[p];
            for (int i = 0; i < plane.length; i++) {
                sprite[i * 4 + p] = plane[i];
            }
        }
        return sprite;
    }

    private static byte[] getFileBytes(String inputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        FileInputStream inputStream = new FileInputStream(inputFile);
        return inputStream.readAllBytes();
    }

    private static Rgb888Image readDatFile(byte[] datBytes, byte[] palBytes) throws Exception {
        BufferedRgb888Image image = new BufferedRgb888Image(320, 200);
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 320; x++) {
                image.setRgb888Pixel(x, y, getColor(datBytes[320 * y + x], palBytes));
            }
        }
        return image;
    }

    /**
     * Extracts the color from the palette given an index. Each channel (R, G and B) is represented by 6 bits,
     * but the palette is padded to bytes, so the 2 less significant bits of each byte are not used.
     *
     * @param dat       the "index" to the palette
     * @param palette   the palette of colors
     * @return          the RGB color in an int
     */
    private static int getColor(byte dat, byte[] palette) {
        int pi = (dat & 0xFF) * 3;           // Palette index
        int r = palette[pi];
        int g = palette[pi + 1];
        int b = palette[pi + 2];
        return (r << 18 | g << 10 | b << 2) & 0xFFFFFF;
    }

    private static int[] toIntArray(byte[] byteArray) {
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = ((byteArray[i] & 0xFF) << 2) & 0xFF;
        }
        return intArray;
    }

    private static void printPalette(byte[] palette) {
        for (int i = 0; i < palette.length / 3; i++) {
            int pi = (i & 0xFF) * 3;           // Palette index
            int r = palette[pi];
            int g = palette[pi + 1];
            int b = palette[pi + 2];
            System.out.printf("\033[0m%x\033[48;2;%d;%d;%dm%n", i, r, g, b);
        }
    }
}
