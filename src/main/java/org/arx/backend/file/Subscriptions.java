package org.arx.backend.file;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.arx.Resource;
import org.arx.Observer;

/**
 * Stores all subscriptions to resources.
 */
class Subscriptions {
	private Map<Resource, Subscriptions> subscriptionTree;
	private Map<Resource, Set<SubscriptionObserver>> subscriptions;

	/**
	 * Creates an empty subscriptions object.
	 */
	public Subscriptions() {
		subscriptionTree = new HashMap<Resource, Subscriptions>();
		subscriptions = new HashMap<Resource, Set<SubscriptionObserver>>();
	}

	/**
	 * Adds a subscription
	 * 
	 * @param subscription
	 *            the subscription to be added
	 */
	public synchronized void subscribe(SubscriptionObserver subscription) {
		subscribe(subscription, subscription.getResourcePattern(), 0);
	}

	/**
	 * Removes a subscription
	 * 
	 * @param subscription
	 *            the subscription to be removed
	 * @return the removed subscription or null, if the specified subscription
	 *         cannot be found
	 */
	public synchronized SubscriptionObserver unsubscribe(SubscriptionObserver subscription) {
		return unsubscribe(subscription, subscription.getResourcePattern(), 0);
	}

	/**
	 * Removes all subscriptions for the specified observer.
	 * 
	 * @param observer
	 *            the observer to be used for identification of subscriptions to
	 *            be removed
	 * @return set of removed subscriptions
	 */
	public synchronized Set<SubscriptionObserver> unsubscribeAll(Observer observer) {
		Set<SubscriptionObserver> result = new HashSet<SubscriptionObserver>();
		for (Map.Entry<Resource, Set<SubscriptionObserver>> entry : subscriptions.entrySet()) {
			Set<SubscriptionObserver> delete = new HashSet<SubscriptionObserver>();
			for (SubscriptionObserver subscription : entry.getValue()) {
				if (subscription.getObserver().equals(observer)) {
					delete.add(subscription);
				}
			}
			entry.getValue().removeAll(delete);
			result.addAll(delete);
		}
		for (Map.Entry<Resource, Subscriptions> entry : subscriptionTree.entrySet()) {
			result.addAll(entry.getValue().unsubscribeAll(observer));
		}
		return result;
	}

	/**
	 * Returns all subscriptions that match the specified resource.
	 * 
	 * @param resource
	 *            the resource to be used for identification of matching
	 *            subscriptions
	 * @return set of subscriptions that match the specified resource.
	 */
	public synchronized Set<SubscriptionObserver> match(Resource resource) {
		return match(resource, 0);
	}

	private void subscribe(SubscriptionObserver subscription, Resource resourcePattern, int part) {
		int num = resourcePattern.getLevels().length;
		if (part + 1 == num) {
			Set<SubscriptionObserver> subs = subscriptions.get(resourcePattern);
			if (subs == null) {
				subs = new HashSet<SubscriptionObserver>();
				subscriptions.put(resourcePattern, subs);
			}
			subs.add(subscription);
		} else {
			Resource name = resourcePattern.subresource(0, part + 1);
			Subscriptions subTree = subscriptionTree.get(name);
			if (subTree == null) {
				subTree = new Subscriptions();
				subscriptionTree.put(name, subTree);
			}
			subTree.subscribe(subscription, resourcePattern, part + 1);
		}
	}

	private SubscriptionObserver unsubscribe(SubscriptionObserver subscription, Resource resourcePattern, int part) {
		int num = resourcePattern.getLevels().length;
		if (part + 1 == num) {
			Set<SubscriptionObserver> subs = subscriptions.get(resourcePattern);
			if (subs != null) {
				SubscriptionObserver sub = null;
				for (SubscriptionObserver candidate : subs) {
					if (candidate.equals(subscription)) {
						sub = candidate;
						break;
					}
				}
				subs.remove(subscription);
				return sub;
			}
		} else {
			Resource name = resourcePattern.subresource(0, part + 1);
			Subscriptions subTree = subscriptionTree.get(name);
			if (subTree != null) {
				return subTree.unsubscribe(subscription, resourcePattern, part + 1);
			}
		}
		return null;
	}

	private Set<SubscriptionObserver> match(Resource resource, int part) {
		Set<SubscriptionObserver> result = new HashSet<SubscriptionObserver>();
		Resource partial = resource.subresource(0, part);
		Resource search = partial.resolve(Resource.MULTI_LEVEL_WILDCARD);
		Set<SubscriptionObserver> subs = subscriptions.get(search);
		if (subs != null && !subs.isEmpty()) {
			result.addAll(subs);
		}
		int num = resource.getLevels().length;
		if (part + 1 == num) {
			search = partial.resolve(Resource.SINGLE_LEVEL_WILDCARD);
			subs = subscriptions.get(search);
			if (subs != null && !subs.isEmpty()) {
				result.addAll(subs);
			}
			subs = subscriptions.get(resource);
			if (subs != null && !subs.isEmpty()) {
				result.addAll(subs);
			}
		} else {
			search = partial.resolve(Resource.SINGLE_LEVEL_WILDCARD);
			Subscriptions subTree = subscriptionTree.get(search);
			if (subTree != null) {
				Resource resourcePattern = resource.replaceLevel(part, Resource.SINGLE_LEVEL_WILDCARD);
				result.addAll(subTree.match(resourcePattern, part + 1));
			}
			search = resource.subresource(0, part + 1);
			subTree = subscriptionTree.get(search);
			if (subTree != null) {
				result.addAll(subTree.match(resource, part + 1));
			}
		}
		return result;
	}

}
