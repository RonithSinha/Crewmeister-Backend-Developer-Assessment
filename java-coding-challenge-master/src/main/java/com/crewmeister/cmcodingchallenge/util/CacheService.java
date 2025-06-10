package com.crewmeister.cmcodingchallenge.util;

import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheService {
	private final CacheManager cacheManager;

	public CacheService(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void evictCaches(List<String> cacheNames) {
		for (String cacheName : cacheNames) {
			Cache cache = cacheManager.getCache(cacheName);
			if (cache != null) {
				cache.clear();
				log.info("Evicted cache: " + cacheName);
			} else {
				log.info("Cache not found: " + cacheName);
			}
		}
	}
}
