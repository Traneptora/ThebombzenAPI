package com.thebombzen.mods.thebombzenapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This InputStream class reads from a given mod resource location.
 * If the mod is in a directory on the filesystem, this will read the resource in that directory.
 * If the mod is a jar file, this will extract the resource from the Jar and read that.
 * @author thebombzen
 */
public class ModResourceInputStream extends FilterInputStream {
	
	/**
	 * This is the ZipFile we read from if the resource is a zip file.
	 */
	private ZipFile zipFile;
	
	/**
	 * Construct a ModResourceInputStream given the location and name of a resource.
	 * @param location The location of the resource, either a directory or a zip file.
	 * @param resource The name of the resource, with a proceeding '/'. 
	 * @throws IOException In the case of an I/O Error.
	 */
	public ModResourceInputStream(File location, String resource) throws IOException {
		super(null);
		if (location.isDirectory()){
			zipFile = null;
			this.in = new FileInputStream((location.getCanonicalPath() + File.separatorChar + resource).replace('/', File.separatorChar).replace(File.separator + File.separator,  File.separator));
		} else {
			zipFile = new ZipFile(location);
			ZipEntry entry = zipFile.getEntry(resource);
			if (entry == null){
				throw new IOException("Resource " + resource + " not found in " + location.getPath() +".");
			}
			this.in = zipFile.getInputStream(entry);
		}
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if (zipFile != null){
			zipFile.close();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		zipFile.close();
	}
	
}
