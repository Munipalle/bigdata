package com.mongodb;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.jetty.util.Fields;

import com.helpers.Helper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

/**
 * To test Mongo insert,delete,update 
 * operation via java
 * @author arunsadhasivam
 */
public class MongoCrudOperations {
	
	private MongoClient client;
	MongoDatabase db;
	MongoCollection<Document> documentCollection;
	MongoCrudOperations(){
		//NOTE: every time delete mongod.lock if in case show any
		//error in running in starting mongod command to run server.
		//every start it create a lock file.
		
		//1. To connect to DB.
		MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
		//option 1.1:look for default available port
		client = new MongoClient();
		//option 1.2: choose the port.
		client = new MongoClient(uri);
		
		
		
		//option 1.3:mongoconnection options- configure cluster server.
		List<ServerAddress> list= new ArrayList<ServerAddress>();
		
		ServerAddress[] server = new ServerAddress[2];
		server[0]= new ServerAddress("localhost:27017");
		server[1]= new ServerAddress("localhost:27017");
		
		
		//optional to 
		MongoClientOptions options = MongoClientOptions.builder()
		.connectionsPerHost(1)//no of connections
		.connectTimeout(100)//timeout
		//Sets whether there is a a finalize method created that 
		//cleans up instances of DBCursor that the
		//client does not close
		.cursorFinalizerEnabled(true)
		.build();
		client = new MongoClient(Arrays.asList(server),options);
		db = client.getDatabase("test");
		//2. To create collection.
		//db.createCollection("document");
		System.out.println("Collection created sucessfully....");
		
		
		//3. To display available collections Names.
		MongoIterable<String> itCol = db.listCollectionNames();
		MongoCursor<String> itCursor = itCol.iterator();
		while(itCursor.hasNext()){
			System.out.println("collections:"+itCursor.next());
		}
		
		
		//4.To get a document.(if there is no document1 collection exists it creats one else use that)
		documentCollection = db.getCollection("document");
		
		CRUD();
		
		printCollection();
	
		filter();
		
		projection();
				
		sort();
		
		updateAndReplace();
		documentCollection.drop();
		
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
		documentCollection = db.getCollection("document");
		//5.1 to Insert a document//.
		Document document = new Document();
		document.append("name", "test");
		document.append("content", "hello");
		document.append("version", "1.0");
		document.append("Author", "mongodb");
		document.append("publishyear", "2015");
		Helper.printJsonDocument(document);
		documentCollection.insertOne(document);
		Helper.printJsonDocument(document);
		//5.1 END 
		
		
		
		//5.2 to Insert a document.
		List<Document> documentList = getDocumentList();
		documentCollection.insertMany(documentList);
		System.out.println("collection inserted Sucessfully...");
		//END
		
		
		
		//6.to update a document.
		 List<String> names = Arrays.asList("alice", "bobby", "cathy", "david", "ethan");
		 DB dbDatabase = client.getDB("test");
		 DBCollection dbCollection =   dbDatabase.getCollection("document");
		 
		 BasicDBObject dbObject = new BasicDBObject();
	        for (String name : names) {
	        	dbCollection.insert(new BasicDBObject("name",name));
	        }
	        
	    
	    System.out.println("collection Before Updated");
	    dbCollection.update(new BasicDBObject("name", "alice"), new BasicDBObject("$set", new BasicDBObject("age",25)));
	    //incremented age with 1 if age is 25
	    dbCollection.update(new BasicDBObject("age", 25), new BasicDBObject("$inc", new BasicDBObject("age",1)));
	    dbCollection.find(new BasicDBObject("$exists", "age"), new BasicDBObject("$inc", new BasicDBObject("age",1)));
	    
		System.out.println("collection Updated Sucessfully...");
		//END 
		//7.remove document.
		documentCollection = db.getCollection("document");
		//documentCollection.drop();
		System.out.println("collection dropped Sucessfully...");
	}
	
	
	/**
	 * operations in collections
	 * @param documentCollection
	 */
	private void printCollection(){
		System.out.println("MongoCrudOperations.printCollection():BEGIN");
		documentCollection = db.getCollection("document");
		List<Document> result = documentCollection.find().into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		//copy collection
		
		//Helper.printJsonDocument(doc);
		System.out.println("MongoCrudOperations.printCollection():END");
	}
	
	
	/**
	 * filter
	 * @param args
	 */
	public void filter(){
		System.out.println("MongoCrudOperations.filter():BEGIN");
		documentCollection = db.getCollection("document");
		
		for(int i=0;i<3;i++){
			documentCollection.insertOne(new Document().append("filterx",new Random().nextInt(2)));
			documentCollection.insertOne(new Document().append("filtery",new Random().nextInt(100)));
		}
		Bson filter = new Document("name","alice");
		
		//filter only age > 25 and name is alice.
		Bson andCondition  = and(new Document("age",new Document("$gt",25)),filter);
		List<Document> result  = documentCollection.find(andCondition)
								.into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		
		System.out.println("MongoCrudOperations.filter():END");
		
	}
	
	/**
	 * projection
	 * @param args
	 */
	public void projection(){
		System.out.println("MongoCrudOperations.projection():BEGIN");
		documentCollection = db.getCollection("document");
		
		for(int i=0;i<3;i++){
			documentCollection.insertOne(new Document("projectionx",i).append("x", i));
			documentCollection.insertOne(new Document("projectiony",i).append("y", i));
		}
		
		Bson filter = new Document("name","alice");
		//include only age field and dont display id field
		Bson projection = fields(include("age"),exclude("_id"));
		
		
		
		List<Document> result  = documentCollection.find(filter).
								projection(projection)
								.into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		
		System.out.println("MongoCrudOperations.projection():END");
		
	}
	
	
	/**
	 * sort
	 * @param args
	 */
	public void sort(){
		System.out.println("MongoCrudOperations.sort():BEGIN");
		documentCollection = db.getCollection("document");
		
		for(int i=0;i<3;i++){
			documentCollection.insertOne(new Document("projectionx",i));
			documentCollection.insertOne(new Document("projectiony",i));
		}
		
		
		
		Bson sort = new Document("name",1);//1 asc ;-1 desc
		Bson sort1 = orderBy(ascending("name"));//1 asc ;-1 desc
		
		/*
		 * 
		Document{{Name=test1}}
		Document{{Name=test2}}
		Document{{Name=test3}}
		Document{{Name=test4}}
		Document{{filterx=0}}
		Document{{filtery=94}}
		Document{{filterx=1}}
		Document{{filtery=52}}
		Document{{filterx=1}}
		Document{{filtery=29}}
		Document{{projectionx=0, x=0}}
		Document{{y=0}}
		Document{{projectionx=1, x=1}}
		Document{{y=1}}
		Document{{projectionx=2, x=2}}
		Document{{y=2}}
		Document{{projectionx=0}}
		Document{{}}
		Document{{projectionx=1}}
		Document{{}}
		Document{{projectionx=2}}
		Document{{}}
		Document{{name=alice, age=26}}
		Document{{name=bobby}}
		Document{{name=cathy}}
		Document{{name=david}}
		Document{{name=ethan}}
		Document{{name=test, content=hello, version=1.0, Author=mongodb, publishyear=2015}
		
		
		
		 * 
		 */
		
		//NOTE: use fields to add multiple projections
		//since .projections accept only one document
		List<String> exclusionList = new  ArrayList<String>();
//		exclusionList.add("filterx");
//		exclusionList.add("filtery");
//		exclusionList.add("projectionx");
		exclusionList.add("projectiony");
		//Projection cannot have a mix of inclusion and exclusion
		//NOT supported:i.e below is not supported
		//Bson projection = fields( excludeId(),exclude(exclusionList),include("name"));
		
		Bson projection = fields( excludeId(),exclude(exclusionList));
		List<Document> result  = documentCollection.find()
								.projection(projection)
								.sort(sort)//sort1 or sort both same
								//.limit(1) //only 1 record should be shown
								.skip(3) //skip 
								.into(new ArrayList<Document>());
		
		//skip 3 record and then print
		//Document{{Name=test4}}
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		
		System.out.println("MongoCrudOperations.sort():END");
		
	}
	
	private void updateAndReplace(){
		System.out.println("MongoCrudOperations.updateAndReplace():BEGIN");
		documentCollection = db.getCollection("document");
		Bson filter = new Document("name","alice");
		
		//filter only age > 25 and name is alice.
		Bson andCondition  = and(new Document("age",new Document("$gt",25)),filter);
		
//		Document replaceWithVal = new Document("age",);
		
		/*
		 * Document{{_id=55cf41b3deec2473c4c63eb7, name=test, content=hello, version=1.0, Author=mongodb, publishyear=2015}}
		Document{{_id=55cf41b3deec2473c4c63eb8, Name=test1}}
		Document{{_id=55cf41b3deec2473c4c63eb9, Name=test2}}
		Document{{_id=55cf41b3deec2473c4c63eba, Name=test3}}
		Document{{_id=55cf41b3deec2473c4c63ebb, Name=test4}}
		Document{{_id=55cf41b3deec2473c4c63ebc, name=alice, age=26}}
		Document{{_id=55cf41b3deec2473c4c63ebd, name=bobby}}
		Document{{_id=55cf41b3deec2473c4c63ebe, name=cathy}}
		Document{{_id=55cf41b3deec2473c4c63ebf, name=david}}
		Document{{_id=55cf41b3deec2473c4c63ec0, name=ethan}}
		Document{{_id=55cf41b3deec2473c4c63ec1, filterx=1}}
		Document{{_id=55cf41b3deec2473c4c63ec2, filtery=69}}
		Document{{_id=55cf41b3deec2473c4c63ec3, filterx=1}}
		Document{{_id=55cf41b3deec2473c4c63ec4, filtery=95}}
		Document{{_id=55cf41b3deec2473c4c63ec5, filterx=1}}
		Document{{_id=55cf41b3deec2473c4c63ec6, filtery=16}}
		Document{{_id=55cf41b3deec2473c4c63ec7, projectionx=0, x=0}}
		Document{{_id=55cf41b3deec2473c4c63ec8, projectiony=0, y=0}}
		Document{{_id=55cf41b3deec2473c4c63ec9, projectionx=1, x=1}}
		Document{{_id=55cf41b3deec2473c4c63eca, projectiony=1, y=1}}
		Document{{_id=55cf41b3deec2473c4c63ecb, projectionx=2, x=2}}
		Document{{_id=55cf41b3deec2473c4c63ecc, projectiony=2, y=2}}
		Document{{_id=55cf41b3deec2473c4c63ecd, projectionx=0}}
		Document{{_id=55cf41b3deec2473c4c63ece, projectiony=0}}
		Document{{_id=55cf41b3deec2473c4c63ecf, projectionx=1}}
		Document{{_id=55cf41b3deec2473c4c63ed0, projectiony=1}}
		Document{{_id=55cf41b3deec2473c4c63ed1, projectionx=2}}
		Document{{_id=55cf41b3deec2473c4c63ed2, projectiony=2}}
		 * 
		 * 
		 */
		documentCollection.updateMany(gte("projectionx",1), new Document("$inc",new Document("projectionx",1)));
		List<Document> result  = documentCollection.find()
								.into(new ArrayList<Document>());
		Iterator<Document> it = result.iterator();
		while(it.hasNext()){
			System.out.println("\t\t"+it.next());
		}
		
		/*
		 * 
		 Document{{_id=55cf420ddeec246954004ef9, name=test, content=hello, version=1.0, Author=mongodb, publishyear=2015}}
		Document{{_id=55cf420ddeec246954004efa, Name=test1}}
		Document{{_id=55cf420ddeec246954004efb, Name=test2}}
		Document{{_id=55cf420ddeec246954004efc, Name=test3}}
		Document{{_id=55cf420ddeec246954004efd, Name=test4}}
		Document{{_id=55cf420ddeec246954004efe, name=alice, age=26}}
		Document{{_id=55cf420ddeec246954004eff, name=bobby}}
		Document{{_id=55cf420ddeec246954004f00, name=cathy}}
		Document{{_id=55cf420ddeec246954004f01, name=david}}
		Document{{_id=55cf420ddeec246954004f02, name=ethan}}
		Document{{_id=55cf420ddeec246954004f03, filterx=1}}
		Document{{_id=55cf420ddeec246954004f04, filtery=65}}
		Document{{_id=55cf420ddeec246954004f05, filterx=1}}
		Document{{_id=55cf420ddeec246954004f06, filtery=11}}
		Document{{_id=55cf420ddeec246954004f07, filterx=1}}
		Document{{_id=55cf420ddeec246954004f08, filtery=35}}
		Document{{_id=55cf420ddeec246954004f09, projectionx=0, x=0}}
		Document{{_id=55cf420ddeec246954004f0a, projectiony=0, y=0}}
		Document{{_id=55cf420ddeec246954004f0b, projectionx=2, x=1}}
		Document{{_id=55cf420ddeec246954004f0c, projectiony=1, y=1}}
		Document{{_id=55cf420ddeec246954004f0d, projectionx=3, x=2}}
		Document{{_id=55cf420ddeec246954004f0e, projectiony=2, y=2}}
		Document{{_id=55cf420ddeec246954004f0f, projectionx=0}}
		Document{{_id=55cf420ddeec246954004f10, projectiony=0}}
		Document{{_id=55cf420ddeec246954004f11, projectionx=2}}
		Document{{_id=55cf420ddeec246954004f12, projectiony=1}}
		Document{{_id=55cf420ddeec246954004f13, projectionx=3}}
		Document{{_id=55cf420ddeec246954004f14, projectiony=2}}
		 * 
		 */
		
		System.out.println("MongoCrudOperations.updateAndReplace():END");
	}
	
	public static void main(String args[]){
		new MongoCrudOperations();
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
