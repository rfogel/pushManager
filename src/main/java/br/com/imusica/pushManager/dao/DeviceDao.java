package br.com.imusica.pushManager.dao;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.imusica.mongodbProvider.MongoDBProvider;
import br.com.imusica.mongodbProvider.dao.MongoDao;
import br.com.imusica.mongodbProvider.exception.MongoDBException;
import br.com.imusica.pushManager.model.Device;
import br.com.imusica.pushManager.model.DeviceInfo;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class DeviceDao extends MongoDao
{	
	@Autowired
	private Gson parser;
	
	@Autowired
	private MongoDBProvider mongodbProvider;
	
	public DeviceDao() {}
	
	@Override
	public String getCollection() {
		return "device";
	}
	
	@Override
	public MongoDBProvider getMongodbProvider() {
		return mongodbProvider;
	}
	
	public List<Device> findAllDevices()
	{		
		final List<Device> devices = new ArrayList<>();
		
		for (Document document : super.findAll())
			devices.add(parser.fromJson(document.toJson(), Device.class));
		
		return devices;
	}
	
	public long count(List<DeviceInfo> deviceInfos) 
	{				
		final List<Bson> filters = new ArrayList<>();
		
		for (DeviceInfo deviceInfo : deviceInfos)
		{
			filters.add(eq("deviceInfo.key",deviceInfo.getKey()));
			filters.add(eq("deviceInfo.value",deviceInfo.getValue()));
		}
		
		return super.count(and(filters));
	}

	public Device findDevicesByDeviceInfo(List<Device> devices, List<DeviceInfo> deviceInfos, int querySize, Device reference) throws MongoDBException
	{	
		final List<Bson> filters = new ArrayList<>();
		
		for (DeviceInfo deviceInfo : deviceInfos)
		{
			filters.add(eq("deviceInfo.key",deviceInfo.getKey()));
			filters.add(eq("deviceInfo.value",deviceInfo.getValue()));
		}
				
		if ( reference != null ) 
			filters.add(filters.size(), new BasicDBObject(IDENTITY, new BasicDBObject("$gt", new ObjectId(reference.getIdentity()))));
		
		FindIterable<Document> iterable = getMongodbProvider().getDatabase().getCollection(getCollection()).find(and(filters)).sort(new BasicDBObject(IDENTITY,1)).limit(querySize);

		MongoCursor<Document> iterator = iterable.iterator();
		
		final Set<Document> documents = new HashSet<>();
		
		Document lastDocument = null;
		
		while ( iterator.hasNext() )
		{
			Document document = iterator.next();
			
			documents.add(document);
			
			lastDocument = document;
		}
					
		for ( Document document : documents )					
			devices.add(parser.fromJson(document.toJson(), Device.class));
		
		return parser.fromJson(lastDocument.toJson(), Device.class);
	}
}
