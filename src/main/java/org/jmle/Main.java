package org.jmle;

import bmpio.BmpImage;
import bmpio.BmpWriter;
import bmpio.BufferedRgb888Image;
import bmpio.Rgb888Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
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

        int firstSprOffset = (sprBytes[0] << 4) & 0xFF;
        int firstSprSizeX = (sprBytes[firstSprOffset + 1] | sprBytes[firstSprOffset]) & 0xFF;
        int firstSprSizeY = (sprBytes[firstSprOffset + 3] | sprBytes[firstSprOffset + 2]) & 0xFF;
        int numBytes = firstSprSizeX * firstSprSizeY;
        byte[] sprite = new byte[numBytes];

        int i = firstSprSizeY;          // cx (size.y)
        int j = firstSprOffset + 10;    // si (tracks position in original data)
        int k = 0;                      // di (tracks position in new data)
        int p = 4;                      // plane count (Mode X)
        while (p > 0) {
            while (i > 0) {
                int b = sprBytes[j++];

                if (b == 0) {
                    k += 4;
                    i--;
                } else if (b > 0) {
                    do {
                        int reps = b;
                        while (reps > 0) {      // rep movsb
                            sprite[k++] = sprBytes[j++];
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

                    k += 4;                     // byte is zero
                    i--;
                } else {
                    b = ~b + 1;                 // neg al
                    do {
                        k += b;
                        b = sprBytes[j++];
                        int reps = b;
                        while (reps > 0) {      // rep movsb
                            sprite[k++] = sprBytes[j++];
                            reps--;
                        }
                        b = sprBytes[j++];
                        b = ~b + 1;             // neg al
                    } while (b != 0);

                    k += 4;                     // byte is zero
                    i--;
                }
            }
            i = firstSprSizeY;                  // another full loop of size.y
            p--;                                // "select next plane"
            k = 4 - p;                          // "next plane": set "write offset" for next plane
        }

        BufferedRgb888Image image = new BufferedRgb888Image(firstSprSizeX, firstSprSizeY);
        for (int y = 0; y < firstSprSizeY; y++) {
            for (int x = 0; x < firstSprSizeX; x++) {
                image.setRgb888Pixel(x, y, getColor(sprBytes[firstSprSizeX * y + x], palBytes));
            }
        }

        return image;
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
            System.out.printf("\033[48;2;%d;%d;%dm%n", r, g, b);
        }
    }
}
