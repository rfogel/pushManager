package br.com.imusica.pushManager.dao;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.imusica.mongodbProvider.MongoDBProvider;
import br.com.imusica.mongodbProvider.dao.MongoDao;
import br.com.imusica.pushManager.model.PushParameterList;

public class PushParameterDao extends MongoDao
{
	@Autowired
	private MongoDBProvider mongodbProvider;
	
	public PushParameterDao() {}

	@Override
	public String getCollection() {
		return "pushParameter";
	}
	
	@Override
	public MongoDBProvider getMongodbProvider() {
		return mongodbProvider;
	}
	
	public PushParameterList findAllPushParameters()
	{		
		PushParameterList list = new PushParameterList();
		
		for (Document document : super.findAll())
			list.getPushParameters().add(document.getString(MongoDao.IDENTITY));
		
		return list;
	}
}
