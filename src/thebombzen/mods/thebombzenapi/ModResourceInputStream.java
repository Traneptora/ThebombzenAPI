package thebombzen.mods.thebombzenapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModResourceInputStream extends FilterInputStream {
	
	private ZipFile zipFile;
	
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
	
	public void close() throws IOException {
		super.close();
		if (zipFile != null){
			zipFile.close();
		}
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		zipFile.close();
	}
	
}
