package net.haesleinhuepf.spimcat.unsweep;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagescience.transform.Affine;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.imglib2.realtransform.AffineTransform3D;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * BasicUnsweep
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2019
 */
public class BasicUnsweepImageScience implements PlugInFilter {

    private static int translationX = 0;
    private static double angle = 35;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Unsweep using ImageSience");
        gd.addNumericField("Angle", angle, 2);
        gd.addNumericField("Translation", translationX, 0);
        gd.addMessage("Please activate the 'ImageScience' update\nsite to make this plugin run");
        gd.showDialog();
        if(gd.wasCanceled()) {
            return;
        }
        angle = gd.getNextNumber();
        translationX = (int) gd.getNextNumber();
        double shear =  1.0 / Math.tan(angle * Math.PI / 180);

        ImagePlus inputImp = IJ.getImage();

        String filename = IJ.getDirectory("temp");
        filename = filename.replace("\\", "/");
        if (!filename.endsWith("/") ) {
            filename = filename + "/";
        }
        filename = filename + "matrix.txt";

        String matrix = "1\t0\t" + (-shear) + "\t" + (translationX) + "\n" +
                "0\t1\t0\t0\n" +
                "0\t0\t1\t0\n" +
                "0\t0\t0\t1\n";

        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(matrix);
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Files.write(Paths.get(filename), matrix.getBytes(), StandardCharsets.US_ASCII);

        IJ.run(inputImp,"TransformJ Affine", "matrix=" + filename + " interpolation=Linear background=0.0");
    }
}
