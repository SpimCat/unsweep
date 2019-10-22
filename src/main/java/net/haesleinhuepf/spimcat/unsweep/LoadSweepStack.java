/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package net.haesleinhuepf.spimcat.unsweep;

import fiji.util.gui.GenericDialogPlus;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.io.FileInfo;
import ij.plugin.HyperStackMaker;
import ij.plugin.RGBStackMerge;
import ij.plugin.Raw;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author: Robert Haase
 *          October 2019
 */
public class LoadSweepStack implements PlugInFilter {

    String folder = "";

    /**
     * This main function serves for development purposes.
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        new ImageJ();
        LoadSweepStack.loadSweepStack("C:/structure/data/SCAPE/20191022/runA_run2_HR").show();
    }


    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialogPlus gd = new GenericDialogPlus("Load sweep stack");
        gd.addDirectoryField("Folder", folder);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        folder = gd.getNextString();
        ImagePlus imp = loadSweepStack(folder);
        imp.show();
    }

    public static ImagePlus loadSweepStack(String folder) {
        File directory = new File(folder);

        String headerFile = directory + "/acquisitionmetadata.ini";

        // random default values
        int width = 1024;
        int height = 1024;
        int imagesPerFile = 3;
        String fileSuffix = ".dat";

        if (new File(headerFile).exists()) {
            try {
                String header = new String(Files.readAllBytes(Paths.get(headerFile)));
                for (String line : header.split("\n")) {

                    String searchString = "AOIHeight = ";
                    if (line.startsWith(searchString)) {
                        height = Integer.parseInt(line.substring(searchString.length()).trim());
                    }
                    searchString = "AOIWidth = ";
                    if (line.startsWith(searchString)) {
                        width = Integer.parseInt(line.substring(searchString.length()).trim());
                    }
                    searchString = "ImagesPerFile = ";
                    if (line.startsWith(searchString)) {
                        imagesPerFile = Integer.parseInt(line.substring(searchString.length()).trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Warning: no header found");
        }

        System.out.println("Loading... [" + width + "/" + height + "/" + imagesPerFile + "]");

        FileInfo fi = new FileInfo();
        fi.fileType = FileInfo.GRAY16_UNSIGNED;
        fi.width = width;
        fi.height = height;
        fi.nImages = imagesPerFile;
        fi.intelByteOrder = true;

        ArrayList<ImagePlus> images = new ArrayList<ImagePlus>();
        for (File file : directory.listFiles()) {
            System.out.println(file.getName());
            if (file.getName().endsWith(fileSuffix)) {
                ImagePlus imp = Raw.open(file.toString(), fi);
                images.add(imp);
            }
        }

        //ImagePlus[] imageArray = new ImagePlus[images.size()];
        //images.toArray(imageArray);
        //ImagePlus stack = RGBStackMerge.mergeChannels(imageArray, false);
        ImageStack stack = new ImageStack();
        for (ImagePlus imp : images) {
            for (int z = 0; z < imp.getNSlices(); z++) {
                imp.setZ(z + 1);
                stack.addSlice(imp.getProcessor());
            }
        }
        return new ImagePlus(directory.getName(), stack);
    }


}
