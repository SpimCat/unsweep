// unsweep script
//
// Installation:
// * Download and install Fiji
// * Activate the update sites "ImageScience" 
//   and "Haesleinhuepf". The second one must 
//   be entered manually.
//
// Author: Robert Haase, rhaase@mpi-cbg.de
//         November 2019
// -----------------------------------------

// enter input folders and file endings
inputFolder = "D:/_MPI-CBG -Data/Daniela/18102019/tiff_stacks/";
outputFolder = "V:/IMAGING/SCAPE/20191018/"

inputSuffix = ".tiff";

// set these values to 1 if you want to prevent scaling
scaleX = 0.5;
scaleY = 0.5;
scaleZ = 0.5;

// SCAPE config
unsweep_angle = 35; // degrees
translationX = 0;

// use batch mode to prevent windows popping up
batch_mode = true;

// ------------ DO NO EDIT BELOW  --------------------------------------

// ensure the output folder exists
File.makeDirectory(outputFolder);
File.makeDirectory(outputFolder + "/thumbnails");

// get list of files and subfolders in input path
subfolderlist = getFileList(inputFolder)

setBatchMode(batch_mode);

// go through all top level folders
for (j = 0; j < lengthOf(subfolderlist); j++) {
	subfolder = inputFolder + "/" + subfolderlist[j];
	if (File.isDirectory(subfolder)){
		
		// get list of files in subfolder
		filelist = getFileList(subfolder); 
		
		// go through all files
		for (i = 0; i < lengthOf(filelist); i++) {
			
			// check if the input file has the right ending
		    if (endsWith(filelist[i], inputSuffix)) { 
		    	print(filelist[i]);
		        open(subfolder + "/" + filelist[i]);
		        
				run("Basic Unsweep (ImageScience)", "angle=" + unsweep_angle + " translation=" + translationX);
		
				// scale the image if configured
				if (scaleX != 1 || scaleY != 1 || scaleZ != 1) {
					getDimensions(width, height, channels, slices, frames);
					run("Scale...", 
					"x=" + scaleX +
					" y=" + scaleY +
					" z=" + scaleZ +
					" width=" + (width * scaleX) + 
					" height=" + (height * scaleY) + 
					" depth=" + (slices * scaleZ) + 
					" interpolation=Bilinear average process create");
				}
		
				// save corrected image
				saveAs("Tiff", outputFolder + "/" + filelist[i] + "_corr.tif");
		 
				// make a maximum projection and save it as thumbnail
		        	run("Z Project...", "projection=[Max Intensity]");
				saveAs("Tiff", outputFolder + "/thumbnails/" + filelist[i] + "_corr.tif");
		
				// cleanup
				run("Close All");
		    } 
		}

		// for debugging
	    //if (j > 4) {
	    //	break;	
	    //}
	}
}

setBatchMode(false);

