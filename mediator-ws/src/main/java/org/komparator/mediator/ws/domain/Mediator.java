package org.komparator.mediator.ws.domain;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Mediator {
	// Members ---------------------------------------------------------------
		/**
		 * Map of existing carts. Uses concurrent hash table implementation
		 * supporting full concurrency of retrievals and high expected concurrency
		 * for updates.
		 */
		private Map<String, Cart> carts = new ConcurrentHashMap<>();

		/** Map of items. Also uses concurrent hash table implementation. */
		private Map<String, Item> items = new ConcurrentHashMap<>();

		// For more information regarding concurrent collections, see:
		// https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html#package.description

		// Singleton -------------------------------------------------------------

		/* Private constructor prevents instantiation from other classes */
		private Mediator() {
		}

		/**
		 * SingletonHolder is loaded on the first execution of
		 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
		 * not before.
		 */
		private static class SingletonHolder {
			private static final Mediator INSTANCE = new Mediator();
		}

		public static synchronized Mediator getInstance() {
			return SingletonHolder.INSTANCE;
		}

		// product ---------------------------------------------------------------

		public void reset() {
			items.clear();
			carts.clear();
		}

		public Boolean cartExists(String cid) {
			return carts.containsKey(cid);
		}

		public Set<String> getCartsIDs() {
			return carts.keySet();
		}

		public Cart getCart(String cartId) {
			return carts.get(cartId);
		}

		public void registerCart(String cartId) {
			if (acceptCart(cartId)) {
				carts.put(cartId, new Cart(cartId));
			}
		}

		private Boolean acceptCart(String cartId) {
			return cartId != null && !"".equals(cartId);
		}
		
}
