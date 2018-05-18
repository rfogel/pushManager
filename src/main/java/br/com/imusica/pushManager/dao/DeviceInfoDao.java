package br.com.imusica.pushManager.dao;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.imusica.mongodbProvider.MongoDBProvider;
import br.com.imusica.mongodbProvider.dao.MongoDao;
import br.com.imusica.pushManager.model.DeviceInfoTypeList;

public class DeviceInfoDao extends MongoDao
{
	@Autowired
	private MongoDBProvider mongodbProvider;
	
	public DeviceInfoDao() {}

	@Override
	public String getCollection() {
		return "deviceInfo";
	}
	
	@Override
	public MongoDBProvider getMongodbProvider() {
		return mongodbProvider;
	}
	
	public DeviceInfoTypeList findAllDeviceInfoTypes()
	{		
		DeviceInfoTypeList list = new DeviceInfoTypeList();
		
		for (Document document : super.findAll())
			list.getDeviceTypes().add(document.getString(MongoDao.IDENTITY));
		
		return list;
	}
}
