/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cloudrun;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.secretmanager.SecretManagerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.storage.CloudStorageExample;


// PubsubController consumes a Pub/Sub message.
@RestController
public class PubSubController {
	
	@Value("${output.bucket.url}")
	String outputBucket;
	
	@Autowired
	private SecretManagerTemplate secretManagerTemplate;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity receiveMessage(@RequestBody Body body) {
		
		long start = System.currentTimeMillis();
		
		//String outputBucketValue = secretManagerTemplate.getSecretString(outputBucket);
		
		System.out.println("##### OUTPUT BUCKET SECRET VALUE = " + outputBucket);

		// Get PubSub message from request body.

		String projectId = System.getenv("PROJECT_ID");//"supple-cosine-292623";
		String destFilePath = "tmpFiles";

		Body.Message message = body.getMessage();
		
		if (message == null) {
			String msg = "Bad Request: invalid Pub/Sub message format";
			System.out.println(msg);
			return new ResponseEntity(msg, HttpStatus.BAD_REQUEST);
		}
		
		System.out.println("Hello (encoded data)");
		System.out.println(message.getData());
		System.out.println("!");

		String data = message.getData();
		
		String decodedData = !StringUtils.isEmpty(data) ? new String(Base64.getDecoder().decode(data)) : "World";
		String msg = "Hello " + decodedData + "!";

		System.out.println(msg);
		
		// Download & read file from Cloud Storage bucket
		
		if (decodedData != null) {
			JSONObject json = new JSONObject(decodedData);
			String bucketName = json.getString("bucket");
			String objectName = json.getString("name");
			
			CloudStorageExample storageExample = new CloudStorageExample(projectId);
			
			storageExample.downloadObject(bucketName, objectName, destFilePath);
			
			String downloadedFile = destFilePath + "/"+ objectName;
			String newObjectName = "NEW_"+ objectName;
			String newFilePath = destFilePath + "/"+ newObjectName;			
			
			try {
				updateFile(downloadedFile, newFilePath);
				
				//String outputBucket = /*System.getenv("OUTPUT_BUCKET")*/;
				
				//AccessSecretVersionResponse response = client.accessSecretVersion(1);
				
				storageExample.uploadObject(outputBucket, newObjectName, newFilePath);
			    
			 // Managing Firestore database
				
			    /*FirestoreExample firestoreExample = new FirestoreExample(projectId);
			    firestoreExample.run();*/
			    
			}/* catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
		
	    
		System.out.println("Execution time: " + (System.currentTimeMillis() - start));
		
		return new ResponseEntity(msg, HttpStatus.OK);
	}

	private void updateFile(String downloadedFile, String newFilePath) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(downloadedFile));
		PrintWriter printWriter = new PrintWriter(new FileWriter(newFilePath));
		String line = "";
		
		for (int i = 1; (line = bufferedReader.readLine()) != null; i++) {
			printWriter.println(i + ") " + line);
		}
		
		bufferedReader.close();
		printWriter.flush();
		printWriter.close();
	}
}
// [END run_pubsub_handler]
