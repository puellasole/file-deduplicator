import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDeduplicator {
	
	private final File directory;
	private final ArrayList<FileRecord> records;
	private final Map<String, List<Path>> map;
	
	FileDeduplicator(final String filePath){
		this.directory = new File(filePath);
		if(!this.directory.isDirectory()) {
			throw new IllegalStateException("Supplied directory does not exist");
		}
		this.records = new ArrayList<>();
		this.map = new HashMap<>();
	}
	
	private void setUp() {
		traverseDirectory(this.directory, this.records);
		findDublicates(this.records, this.map);
	}
	
	private void traverseDirectory(final File directory, final List<FileRecord> records) {
	    if (directory.isDirectory()) {
	        for (final File file : directory.listFiles()) {
	            if (file.isFile()) {
	                records.add(new FileRecord(file));
	            } else if (file.isDirectory()) {
	                traverseDirectory(file, records);
	            }
	        }
	    }
	}
	
	private void findDublicates(final ArrayList<FileRecord> records, final Map<String, List<Path>> map) {
		for(final FileRecord record : records) {
			final String hashCode = record.getHashCode();
			final Path filePath = record.getPath();
			if(map.containsKey(hashCode)) {
				final List<Path> filePaths = map.get(hashCode);
				filePaths.add(filePath);
			} else {
				final List<Path> filePaths = new ArrayList<>();
				filePaths.add(filePath);
				map.put(hashCode, filePaths);
			}
		}
		
		for (final Map.Entry<String, List<Path>> entry : map.entrySet()) {
	        final List<Path> filePaths = entry.getValue();
	        if (filePaths.size() > 1) {
	            System.out.println("Duplicates found for hash: " + entry.getKey());
	            for (final Path path : filePaths) {
	                System.out.println(path.toString());
	            }
	            System.out.println("------------------------");
	        }
	    }

	}
	
	public static void main(String[] args) {
		System.out.println("Scanning started...");
		FileDeduplicator test = new FileDeduplicator("C:\\Users\\79114\\Downloads");
		test.setUp();
	}
	
	static class FileRecord {
		private final Path path;
		private final String hashCode;
		
		FileRecord(final File file){
			this.path = file.toPath();
			this.hashCode = hashFile(file);
		}
		
		private String hashFile(final File file) {
			try {
	            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            final byte[] buffer = new byte[8192];
	            int read = 0;

	            try (final FileInputStream fis = new FileInputStream(file)) {
	                while ((read = fis.read(buffer)) > 0) {
	                    digest.update(buffer, 0, read);
	                }
	            }

	            final byte[] hashBytes = digest.digest();
	            return bytesToHex(hashBytes);
	        } catch (NoSuchAlgorithmException | IOException e) {
	            throw new RuntimeException(e);
	        }
		}
		
		private String bytesToHex(byte[] bytes) {
	        final StringBuilder result = new StringBuilder();
	        for (final byte b : bytes) {
	            result.append(String.format("%02x", b));
	        }
	        return result.toString();
		}
		
		public Path getPath() {
			return this.path;
		}
		
		public String getHashCode() {
			return this.hashCode;
		}
	}
}

