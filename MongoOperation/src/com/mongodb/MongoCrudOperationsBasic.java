package com.mongodb;


import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.helpers.Helper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateOptions;

/**
 * To test Mongo insert,delete,update 
 * operation via java
 * @author arunsadhasivam
 */
public class MongoCrudOperationsBasic {
	
	private MongoClient client;
	MongoDatabase db;
	MongoCollection<Document> documentCollection;
	MongoCrudOperationsBasic() {

		try {
			// NOTE: every time delete mongod.lock if in case show any
			// error in running in starting mongod command to run server.
			// every start it create a lock file.

			// 1. To connect to DB.
			
			//NOTE:
			//if below 1.1,1.2,1.3 all three available it shows below error (3 open connections)
			//(3 connections now open)
			//2015-08-16T04:13:59.078-0700 I NETWORK  [conn414] end connection 127.0.0.1:4043
			//(3 connections now open)

			//After commenting 1.1,1.2 and by having only 1.3 ,It shows one open connection -Also get unexpected results sometimes. 
			//2015-08-16T04:19:14.062-0700 I NETWORK  [conn416] end connection 127.0.0.1:4105
			//(1 connection now open)

			
			
			//MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
			// option 1.1:look for default available port
			
			
			//client = new MongoClient();
			// option 1.2: choose the port.
			//client = new MongoClient(uri);
			
			


			// option 1.3:mongoconnection options- configure cluster server.
			List<ServerAddress> list = new ArrayList<ServerAddress>();

			ServerAddress[] server = new ServerAddress[2];
			server[0] = new ServerAddress("localhost:27017");
			server[1] = new ServerAddress("localhost:27017");

			// optional to
			MongoClientOptions options = MongoClientOptions.builder()
					.connectionsPerHost(1)// no of connections
					.connectTimeout(100)// timeout
					// Sets whether there is a a finalize method created that
					// cleans up instances of DBCursor that the
					// client does not close
					.cursorFinalizerEnabled(true).build();
			client = new MongoClient(Arrays.asList(server), options);
			db = client.getDatabase("test");
			// 2. To create collection.
			// db.createCollection("document");
			System.out.println("Collection created sucessfully....");

			// 3. To display available collections Names.
			MongoIterable<String> itCol = db.listCollectionNames();
			MongoCursor<String> itCursor = itCol.iterator();
			while (itCursor.hasNext()) {
				System.out.println("collections:" + itCursor.next());
			}

			// crud operation
			CRUD();
			// filter the mongodb
			filter();
			// use projection in mongodb.
			projection();
			// sort mongodb
			sort();
			// update and replace operation in mongodb.
			updateAndReplace();
		}catch(DuplicateKeyException e){
			//NOTE: if any error occurs  because some operations fails before calling drop() of
			//collection in sort,filter,projection operation record need to be deleted.
			//i.e when filter invoke , after insertion fails in filter actual invocation and before
			//calling the collection drop () record will exists and next time throwing
			//duplicate key exception hence if any error occur in mid of operation drop collection
			// so that next time the duplicate error won't occur.

			System.out.println("========================");
			System.err
					.println("ERROR:");
			documentCollection.drop();
			e.printStackTrace();
			System.out.println("========================");
			
		}catch(Exception e){
			System.out.println("========================");
			System.err
					.println("ERROR:");
			documentCollection.drop();
			e.printStackTrace();
			System.out.println("========================");

		}finally {
			client.close();
			//db.drop();
		}

	}
	
	private void CRUD(){
		/*> db.document.find()
		{ "_id" : ObjectId("55cef69fdeec248bb8de42b1"), "name" : "test", "content" : "he
		llo", "version" : "1.0", "Author" : "mongodb", "publishyear" : "2015" }
		{ "_id" : ObjectId("55cef6a0deec248bb8de42b2"), "Name" : "test1" }
		{ "_id" : ObjectId("55cef6a0deec248bb8de42b3"), "Name" : "test2" }
		{ "_id" : ObjectId("55cef6a0deec248bb8de42b4"), "Name" : "test3" }
		{ "_id" : ObjectId("55cef6a0deec248bb8de42b5"), "Name" : "test4" }
		
		>
		
		*/
		System.err.println("MongoCrudOperations.CRUD():BEGIN");
		documentCollection = db.getCollection("CRUD");
		//5.1 to Insert a document//.
		Document document = new Document();
		document.append("name", "test");
		document.append("content", "hello");
		document.append("version", "1.0");
		document.append("Author", "mongodb");
		document.append("publishyear", "2015");
		Helper.printJsonDocument(document);
		System.out.println("\tMongoCrudOperationsBasic.CRUD():Single DOc insert sucessfull.");
		documentCollection.insertOne(document);
		Helper.printJsonDocument(document);
		
		//5.1 END 
		
		
		
		//5.2 to Insert a document.
		List<Document> documentList = getDocumentList();
		documentCollection.insertMany(documentList); 
		printCollection();
		documentCollection.drop();
		System.out.println("\tMongoCrudOperationsBasic.CRUD():Multiple Doc insert sucessfull.");
		System.out.println("collection inserted Sucessfully...");
		//END
		
		
		
		//6.to update a document.
		 List<String> names = Arrays.asList("alice", "bobby", "cathy", "david", "ethan");
		 db = client.getDatabase("test");
		 documentCollection =   db.getCollection("UPDATE");
	        for (String name : names) {
	        	documentCollection.insertOne(new Document("name",name));
	        }
	        
	    
	    System.out.println("collection Before Updated");
	    printCollection();
	    //NOTE:
	    //documentCollection.updateOne(new Document("name", "alice"), new Document("$set", new BasicDBObject("age",25)));
	    //see the above will also works but it tracke DBObject and Document separately.
	    documentCollection.updateOne(new Document("name", "alice"), new Document("$set", new Document("age",25)));
	    
	    System.out.println("Added extra field age through update");
	    printCollection();
	    //incremented age with 1 if age is 25
	    documentCollection.updateOne(new Document("age", 25), new Document("$inc", new Document("age",1)));
	   // documentCollection.updateOne(new Document("$exists", "age"), new Document("$inc", new Document("age",1)));
	    System.out.println("collection After Updated");
	    printCollection();
		System.out.println("collection Updated Sucessfully...");
		
		documentCollection.drop();
		//END 
		//7.remove document.
		//documentCollection = db.getCollection("document");
		//documentCollection.drop();
		//System.out.println("collection dropped Sucessfully...");
		
		
		deleteMany();
		System.err.println("MongoCrudOperations.CRUD():END");
	}
	
	/**
	 * delete Many
	 */
	private void deleteMany(){
		System.err.println("MongoCrudOperations.deleteMany():BEGIN");
		documentCollection = db.getCollection("DELETE");
		documentCollection.drop();
		for(int i=1;i<5;i++){
			documentCollection.insertOne(new Document("_id",i));
		}
		System.out.println("Before Delete Operation");
		printCollection();
		
		
		
		documentCollection.deleteMany(gt("_id",0));
		
		System.out.println("After Delete Operation");
		printCollection();
		
		System.err.println("MongoCrudOperations.deleteMany():END");
	}
	
	
	/**
	 * operations in collections
	 * @param documentCollection
	 */
	private void printCollection(){
		List<Document> result = documentCollection.find().into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		boolean isRecordExist = false;
		while(it.hasNext()){
			isRecordExist = true;
			System.out.println("\t\t"+it.next());
		}
		
		if(!isRecordExist){
			System.out.println("\t\t{}");
		}
		

	}
	
	/**
	 * operations in collections
	 * @param documentCollection
	 */
	private void printDbCollection(){
		DB dbDatabase = client.getDB("test");
		DBCollection dbCollection =   dbDatabase.getCollection("SORT");
		DBCursor result = dbCollection.find();
		Iterator<DBObject> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}

	}
	
	
	/**
	 * filter
	 * @param args
	 */
	public void filter() throws DuplicateKeyException{
		System.err.println("MongoCrudOperations.filter():BEGIN");
		String student[] = {"alice", "bobby", "cathy", "david", "ethan"};
		String studentRollNo[] = {"1001", "1002", "1003", "1004", "1005"};
		String studentMarks[] = {"30", "70", "80", "90", "100"};
		documentCollection =   db.getCollection("FILTER");
		
		for(int i=0;i<student.length;i++){
			Document dbObject = new Document("_id",studentRollNo[i])
				.append("name",student[i])
				.append("marks", studentMarks[i]);
			documentCollection.insertOne(dbObject);
		}
		System.out.println("MongoCrudOperationsBasic.filter():BEFORE APPLY FILTER:"+documentCollection.count());
		printCollection();
		
		
		//filter only id > 25 and name is alice.
		//NOTE: use "" for all data types for int also if 60 enter without "" not working.
		Bson andFilter  = and(new Document("marks",new Document("$gt","60")) ,
								new Document("marks",new Document("$lte","90")));
		 // andFilter  = new Document("marks",new Document("$lte","35")); 
				
		List<Document> result  = documentCollection.find(andFilter)
								.into(new ArrayList<Document>());
		System.out.println("MongoCrudOperationsBasic.filter():AFTER APPLY FILTER:"+result.size());
		Iterator<Document>  it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
	 
		documentCollection.drop();
		
		System.err.println("MongoCrudOperations.filter():END");
		
	}
	
	/**
	 * projection
	 * @param args
	 */
	public void projection()throws DuplicateKeyException{
		System.err.println("MongoCrudOperations.projection():BEGIN");
		String student[] = {"alice", "bobby", "cathy", "david", "ethan"};
		String studentRollNo[] = {"1001", "1002", "1003", "1004", "1005"};
		String studentMarks[] = {"30", "70", "80", "90", "100"};
		documentCollection = db.getCollection("PROJECTION");
		
		for(int i=0;i<3;i++){
			Document dbObject = new Document("_id",studentRollNo[i])
			.append("name",student[i])
			.append("marks", studentMarks[i]);
			documentCollection.insertOne(dbObject);
		}
		
		Bson filter = new Document("name","alice");
		//include only age field and dont display id field
		Bson projection = fields(include("name"),include("marks"),exclude("_id"));
		
		
		
		List<Document> result  = documentCollection.find(filter).
								projection(projection)
								.into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		documentCollection.drop();
		
		System.err.println("MongoCrudOperations.projection():END");
		
	}
	
	
	/**
	 * sort
	 * @param args
	 */
	public void sort()throws DuplicateKeyException{
		System.err.println("MongoCrudOperations.sort():BEGIN");
		documentCollection = db.getCollection("SORT");
		String student[] = {"alice", "bobby", "cathy", "david", "ethan"};
		String studentRollNo[] = {"1001", "1002", "1003", "1004", "1005"};
		String studentMarks[] = {"30", "70", "80", "90", "100"};
		DB dbDatabase = client.getDB("test");
		
		
		DBCollection dbCollection =   dbDatabase.getCollection("SORT");
		for(int i=0;i<5;i++){
			BasicDBObject dbObject = new BasicDBObject("_id",studentRollNo[i])
			.append("name",student[i])
			.append("marks", studentMarks[i]);
			dbCollection.insert(dbObject);
		}
		System.out.println("MongoCrudOperationsBasic.sort():Before Assign DB:"+documentCollection.count());
		documentCollection = db.getCollection("SORT");
		System.out.println("MongoCrudOperationsBasic.sort():After Assign db:"+documentCollection.count());
		//NOTE: values can be inserte using DBObject and retrieved using Document.
		Bson sort = new Document("name",-1);//1 asc ;-1 desc
		//NOTE: use fields to add multiple projections
		//since .projections accept only one document
		List<String> exclusionList = new  ArrayList<String>();
		exclusionList.add("studentRollNo");
		//Projection cannot have a mix of inclusion and exclusion
		//NOT supported:i.e below is not supported
		//Bson projection = fields( excludeId(),exclude(exclusionList),include("name"));
		
		Bson projection = fields( excludeId(),exclude(exclusionList));
		List<Document> result  = documentCollection.find()
								.projection(projection)
								.sort(sort)//sort1 or sort both same
								//.limit(1) //only 1 record should be shown
								//.skip(3) //skip 
								.into(new ArrayList<Document>());
		
		//skip 3 record and then print
		//Document{{Name=test4}}
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		dbCollection.drop();
		documentCollection.drop();
		System.err.println("MongoCrudOperations.sort():END");
		
	}
	
	private void updateAndReplace()throws DuplicateKeyException{
		System.err.println("MongoCrudOperations.updateAndReplace():BEGIN");
		
		documentCollection = db.getCollection("UPDATEANDREPLACE");
		
		String student[] = {"alice", "bobby", "cathy", "david", "ethan"};
		String studentRollNo[] = {"1001", "1002", "1003", "1004", "1005"};
		int[] studentMarks = {30, 31, 33, 90, 100};
		for(int i=0;i<5;i++){
			Document dbObject = new Document("_id",studentRollNo[i])
			.append("name",student[i])
			.append("marks", studentMarks[i]);
			documentCollection.insertOne(dbObject);
		}
		System.out.println("Before Update values...");
		printCollection();
		
		boolean upsert =true;//create new doc if no document exists
		
		//update multiple document matches query else only first one
		//will be updated.
		boolean multi = true; 
		
		
		//documentCollection.updateMany(lte("marks",new Document("$lt","30")), new Document("name","351"),options);
		//to make pass
		UpdateOptions options = new UpdateOptions();
		options.upsert(upsert);
		//$inc wont work if you use above student marks in string. 
		//values inserted should be in integer to apply $inc
		//else it saves invalid bson or $inc should be applied to integer
		//else create Document{{_id=55d097da22bd6ae7b7be1ff5, marks=5}}
		//example
		/*After Update
		Document{{_id=1001, name=alice, marks=30}}
		Document{{_id=1002, name=bobby, marks=31}}
		Document{{_id=1003, name=cathy, marks=33}}
		Document{{_id=1004, name=david, marks=90}}
		Document{{_id=1005, name=ethan, marks=100}}
		Document{{_id=55d0980a22bd6ae7b7be1ff6, marks=5}}*/
		documentCollection.updateMany(lte("marks",35), new Document("$inc",new Document("marks",5)),options);

		System.out.println("After Update");
		List<Document> result  = documentCollection.find()
				//.limit(1) //only 1 record should be shown
				//.skip(3) //skip 
				.into(new ArrayList<Document>());
		
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		//correct 
		/*Before Update values...
		Document{{_id=1001, name=alice, marks=30}}
		Document{{_id=1002, name=bobby, marks=31}}
		Document{{_id=1003, name=cathy, marks=33}}
		Document{{_id=1004, name=david, marks=90}}
		Document{{_id=1005, name=ethan, marks=100}}
		After Update
		Document{{_id=1001, name=alice, marks=35}}
		Document{{_id=1002, name=bobby, marks=36}}
		Document{{_id=1003, name=cathy, marks=38}}
		Document{{_id=1004, name=david, marks=90}}
		Document{{_id=1005, name=ethan, marks=100}}*/
		//dbCollection.drop(); 
		documentCollection.drop();
		System.err.println("MongoCrudOperations.updateAndReplace():END");
	}
	
	public static void main(String args[]){
		new MongoCrudOperationsBasic();
	}
	
	/**
	 * To insert Multiple document.
	 * @return
	 */
	private List<Document> getDocumentList(){
		List<Document> documentList = new ArrayList<Document>();
		 
		for(int i=1;i<5;i++){
			Document document = new Document();
			document.append( "Name","test"+i);
			
			documentList.add(document);
		}
		
		return documentList;
	}

}
