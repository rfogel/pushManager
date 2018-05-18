package br.com.imusica.pushManager.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.imusica.mongodbProvider.MongoDBProvider;
import br.com.imusica.mongodbProvider.dao.MongoDao;
import br.com.imusica.mongodbProvider.exception.MongoDBException;
import br.com.imusica.pushManager.model.Campaign;

import com.google.gson.Gson;

public class CampaignDao extends MongoDao
{	
	@Autowired
	private Gson parser;
	
	@Autowired
	private MongoDBProvider mongodbProvider;
	
	public CampaignDao() {}
	
	@Override
	public String getCollection() {
		return "campaign";
	}
	
	@Override
	public MongoDBProvider getMongodbProvider() {
		return mongodbProvider;
	}
	
	public void saveCampaign(Campaign campaign) throws MongoDBException 
	{
		Document document = createDocument(parser.toJson(campaign));
		
		super.insertOne(document);
		
		campaign.setIdentity(document.getObjectId(MongoDao.IDENTITY).toHexString());
	}
	
	public void updateCampaign(Campaign campaign) throws MongoDBException {
		super.insertOrUpdate(createDocument(parser.toJson(campaign)));
	}
	
	public void deleteCampaign(Campaign campaign) throws MongoDBException {
		super.deleteById(campaign.getIdentity());
	}
	
	public Campaign findCampaign(Campaign campaign) throws MongoDBException {
		return parser.fromJson(super.findOne(super.createDocument(parser.toJson(campaign))).toJson(),Campaign.class);
	}
	
	public List<Campaign> findAllCampaign() throws MongoDBException 
	{	
		List<Campaign> campaigns = new ArrayList<>();
		
		for (Document document : super.findAll()) {
			campaigns.add(parser.fromJson(document.toJson(), Campaign.class));
		}
		
		return campaigns;
	}
}
