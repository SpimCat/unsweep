package net.haesleinhuepf.spimcat.unsweep;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * BasicUnsweep
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2019
 */
public class BasicUnsweep implements PlugInFilter {

    private static int translationX = 0;
    private static double angle = 35;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Unsweep");
        gd.addNumericField("Angle", angle, 2);
        gd.addNumericField("Translation", translationX, 0);
        gd.showDialog();
        if(gd.wasCanceled()) {
            return;
        }
        angle = gd.getNextNumber();
        translationX = (int) gd.getNextNumber();

        ImagePlus inputImp = IJ.getImage();

        CLIJ clij = CLIJ.getInstance();
        ClearCLImage input = clij.convert(inputImp, ClearCLImage.class);
        ClearCLImage output = clij.createCLImage(input);

        AffineTransform3D at = new AffineTransform3D();
        at.translate(input.getWidth() / 2 + translationX, 0, 0 );

        double shear =  1.0 / Math.tan(angle * Math.PI / 180);
        AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
        shearTransform.set(1.0, 0, 0 );
        shearTransform.set(1.0, 1, 1 );
        shearTransform.set(1.0, 2, 2 );
        shearTransform.set(-shear, 0, 2);
        at.concatenate(shearTransform);

        clij.op().affineTransform3D(input, output, at);

        ImagePlus imp = clij.convert(output, ImagePlus.class);
        imp.setTitle(inputImp.getTitle() + " unsweeped");
        imp.setDisplayRange(inputImp.getDisplayRangeMin(), inputImp.getDisplayRangeMax());
        imp.show();
    }
}
