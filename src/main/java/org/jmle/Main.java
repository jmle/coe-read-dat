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
        byte[] datBytes = getFileBytes(args[0]);
        byte[] palBytes = getFileBytes(args[1]);

        printPalette(palBytes);

        Rgb888Image image = readFile(datBytes, palBytes);

        BmpImage bmp = new BmpImage();
        bmp.image = image;
        FileOutputStream out = new FileOutputStream(String.format("%s.bmp", args[0]));
        BmpWriter.write(out, bmp);
    }

    private static byte[] getFileBytes(String inputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        FileInputStream inputStream = new FileInputStream(inputFile);
        return inputStream.readAllBytes();
    }

    private static Rgb888Image readFile(byte[] datBytes, byte[] palBytes) throws Exception {
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
