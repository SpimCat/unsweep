package net.haesleinhuepf.spimcat.unsweep;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.plugin.Plugin;

/**
 * Unsweep
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 *         October 2019
 */
@Plugin(type = CLIJMacroPlugin.class, name = "SPIMCAT_unsweep")
public class Unsweep extends AbstractCLIJPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public String getParameterHelpText() {
        return "Image input, Image destination, Number angle, Number translationX";
    }

    @Override
    public boolean executeCL() {
        ClearCLBuffer input = (ClearCLBuffer) args[0];
        ClearCLBuffer output = (ClearCLBuffer) args[1];
        Float angle = asFloat(args[2]);
        Integer translationX = asInteger(args[3]);

        return unsweep(clij, input, output, angle,translationX);
    }

    public static boolean unsweep(CLIJ clij, ClearCLBuffer input, ClearCLBuffer output, float angle, int translationX) {
        //String transform = "translateX=400 shearXZ=1.4281";

        AffineTransform3D at = new AffineTransform3D();
        at.translate(input.getWidth() / 2 + translationX, 0, 0 );

        double shear =  1.0 / Math.tan(angle * Math.PI / 180);
        AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
        shearTransform.set(1.0, 0, 0 );
        shearTransform.set(1.0, 1, 1 );
        shearTransform.set(1.0, 2, 2 );
        shearTransform.set(-shear, 0, 2);
        at.concatenate(shearTransform);

        return clij.op().affineTransform3D(input, output, at);
    }

    @Override
    public String getDescription() {
        return "Unsweep a sweeped stack.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }

}
