package com.datarepublic.simplecab.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.hibernate.Session;
import org.hibernate.query.Query;

public class SimpleCabRepositoryDao implements SimpleCabRepository {
	private Session session;
	private HashMap<String, HashMap<Date, Integer>> cache;

	public SimpleCabRepositoryDao() {
		this.setSession(HibernateUtils.getSession());
		this.cache = new HashMap<String, HashMap<Date, Integer>>();
	}

	public static void main(String[] args) throws ParseException {
		SimpleCabRepositoryDao ss = new SimpleCabRepositoryDao();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateInString = "2013-12-06";
		int aa = ss.getCountByMedallionAndPickupDatetime("A0B5AF0F9B31690CEBB51ECD27D2BE71", formatter.parse(dateInString));
		System.out.println(aa);		
		ArrayList<String> ids = new ArrayList<String>();
		ids.add("A0B5AF0F9B31690CEBB51ECD27D2BE71");
		ids.add("8F0A1E787329C1C08F8F9120EE68E056");
		ids.add("08099C118772D01BBA1C3F486BD8AF1E");
		ids.add("B4C414BA3488EBECF8A633B313A41B00");
		ids.add("01F753DA1D273F049CE7D2E16587C380");
		HashMap<String, HashMap<Date, Integer>> maps = ss.getCountsByMultipleMedallionsAndPickupDatetime(ids, formatter.parse(dateInString), true);
		System.out.println(maps);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Integer getCountByMedallionAndPickupDatetime(String medallionId,
			Date pickupDate) {
		long result = 0;
		try {
			String hql = "SELECT COUNT(*) FROM CabTripData cab WHERE cab.id.medallion = :MEDALLION AND cab.id.pickupDatetime BETWEEN :PICKUPDATEFROM AND :PICKUPDATETO";
			Query<Long> query = session.createQuery(hql);
			query.setParameter("MEDALLION", medallionId);
			query.setParameter("PICKUPDATEFROM", pickupDate);
			// add time for date to the end of the day 23:59:59
			query.setParameter("PICKUPDATETO", new Date(pickupDate.getTime() + 86399999));
			result = query.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			// Rollback in case of an error occurred.
			session.getTransaction().rollback();
		}
		return (int)result;
	}

	@Override
	public HashMap<String, HashMap<Date, Integer>> getCountsByMultipleMedallionsAndPickupDatetime(
			ArrayList<String> medallionIds, Date pickupDate, boolean cached) {
		HashMap<String, HashMap<Date, Integer>> result = new HashMap<String, HashMap<Date, Integer>>();
		for (String medallionId : medallionIds) {
			getCountByMedallionAndPickupDatetime(medallionId, pickupDate);
			if (cached && cache.containsKey(medallionId)
					&& cache.get(medallionId).containsKey(pickupDate)) {
				HashMap<Date, Integer> subResult = new HashMap<Date, Integer>();
				subResult.put(pickupDate, cache.get(medallionId)
						.get(pickupDate));
				result.put(medallionId, subResult);
			} else {

				int count = getCountByMedallionAndPickupDatetime(medallionId,
						pickupDate);

				HashMap<Date, Integer> subResult = new HashMap<Date, Integer>();
				subResult.put(pickupDate, count);
				result.put(medallionId, subResult);

				if (!cache.containsKey(medallionId)
						|| !cache.get(medallionId).containsKey(pickupDate)) {
					cache.putAll(result);
				}
			}
		}

		return result;
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}