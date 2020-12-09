package com.example.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;


/**
 * Implements basic operations to manage cloud storage objects.
 * @author CIzquierdo
 *
 */
public class CloudStorageExample {
	
	Storage storage;
	
	public CloudStorageExample(String projectId) {
		storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
	}
	
	public CloudStorageExample() {
		storage = StorageOptions.newBuilder().build().getService();
	}
	
	public void downloadObject(String bucketName, String objectName, String destFilePath) {
		File folder = new File(destFilePath);
		folder.mkdir();
		
		Blob blob = storage.get(BlobId.of(bucketName, objectName));
		String tmpFile = destFilePath + "/"+ objectName;
		blob.downloadTo(Paths.get(tmpFile));

		System.out.println("Downloaded object " + objectName + " from bucket name " + bucketName + " to " + destFilePath);
	}
	
	public void uploadObject(String bucketName, String objectName, String fileToUpload) {
		BlobId blobId = BlobId.of(bucketName, objectName);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
	    
	    try {
			storage.create(blobInfo, Files.readAllBytes(Paths.get(fileToUpload)));
			System.out.println(
			        "File " + fileToUpload + " uploaded to bucket " + bucketName + " as " + objectName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

}
