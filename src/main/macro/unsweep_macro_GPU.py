# unsweep script for the GPU
#
# Installation:
# * Download and install Fiji
# * Activate the update sites "clij", "clij2" 
#   and "Haesleinhuepf". The third one must 
#   be entered manually.
#
# Author: Robert Haase, rhaase@mpi-cbg.de
#		 November 2019
# -----------------------------------------

# enter input folders and file endings
inputFolder = "D:/_MPI-CBG -Data/Daniela/18102019/tiff_stacks/";
outputFolder = "V:/IMAGING/SCAPE/20191018/"

inputSuffix = ".tiff";

# set this value to 1 if you want to prevent scaling
isotropic_scale = 0.5;

# SCAPE config
unsweep_angle = 35; # degrees
translationX = 0;

#/ ------------ DO NO EDIT BELOW  --------------------------------------

import os;

from net.haesleinhuepf.clijx import CLIJx;
from net.haesleinhuepf.spimcat.unsweep import Unsweep
from ij import IJ;

# init GPU
clijx = CLIJx.getInstance();

# get list of files and subfolders in input path
for root, directories, filenames in os.walk(inputFolder):
	for directory in directories:
		print("directory: " + directory);
		for subroot, subdirectories, subfilenames in os.walk(inputFolder + directory):
			filenames.sort();
			for filename in subfilenames:
				print("filename: " + filename);
				# Check for file extension
				if not filename.endswith(inputSuffix):
					continue

			  
				print (filename);

				# load image
				input = clijx.readImageFromDisc(inputFolder + directory + "/" + filename);

				if (isotropic_scale != 1):
					# downsample
					downsampled = clijx.create([input.getWidth() * isotropic_scale, input.getHeight() * isotropic_scale, input.getDepth() * isotropic_scale]);
					clijx.downsample(input, downsampled, isotropic_scale, isotropic_scale, isotropic_scale);

					# unsweep
					unsweeped = clijx.create(downsampled);
					Unsweep.unsweep(clijx.getClij(), downsampled, unsweeped, unsweep_angle, translationX);
					downsampled.close();
				else:
					# unsweep
					unsweeped = clijx.create(input);
					Unsweep.unsweep(clijx.getClij(), input, unsweeped, unsweep_angle, translationX);
				
				input.close();

				# save result stack
				clijx.saveAsTIF(unsweeped, outputFolder + "/" + filename + "_corr.tif");

				# draw max projection
				max_projected = clijx.create([unsweeped.getWidth(), unsweeped.getHeight()], unsweeped.getNativeType());
				clijx.maximumZProjection(unsweeped, max_projected)
				clijx.saveAsTIF(max_projected, outputFolder + "/thumbnails/" + filename + "_corr.tif");

				# cleanu
					# unsweepp
				unsweeped.close();
				max_projected.close();
				
				#break;

		#break;

