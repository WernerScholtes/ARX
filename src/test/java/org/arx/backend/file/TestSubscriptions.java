package org.arx.backend.file;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.arx.Credentials;
import org.arx.Resource;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestSubscriptions {
	@Test
	public void testSingleObserver() {
		String[] res = {"a/b/c", "a/b/+", "a/b/#", "a/+/c", "a/+/+", "a/#", 
				"+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"};
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Subscriptions subs = new Subscriptions();
		for ( String re : res ) {
			Resource resource = new SimpleResource(re);
			SubscriptionObserver sub = new SubscriptionObserver(credentials,observer,resource,false);
			subs.subscribe(sub);
		}
		Set<SubscriptionObserver> match = subs.match(new SimpleResource("a/b/c"));
		Set<String> result = convertToStringSet(match);
		Set<String> expected = new HashSet<String>(Arrays.asList(res));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/b/d"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/b/+","a/b/#","a/+/+","a/#","+/b/+", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/b/d/e"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/b/#","a/#","+/#","#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/d/c"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/+/c", "a/+/+", "a/#", "+/+/c", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/d/e"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/+/+", "a/#", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/d"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/#", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("a/d/e/f"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("a/#","+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("d/b/c"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("d/b/e"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/b/+", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("d/e/c"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/+/c", "+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("d/e/f"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/+/+", "+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("c/d/e/f"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("c/d"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("+/#", "#"));
		assertEquals(expected,result);
		match = subs.match(new SimpleResource("c"));
		result = convertToStringSet(match);
		expected = new HashSet<String>(Arrays.asList("#"));
		assertEquals(expected,result);
	}
	
	@Test
	public void testComplex() {
		String[] res1 = {"a/b/c", "+/b/c", "+/+/c"};
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer1 = new Utils.QueingObserver();
		SubscriptionObserver[] s1 = new SubscriptionObserver[res1.length];
		for ( int i = 0; i < res1.length; ++i) {
			s1[i] = new SubscriptionObserver(credentials, observer1, new SimpleResource(res1[i]), false);
		}
		String[] res2 = {"a/b/+", "+/b/+", "+/+/#"};
		Utils.QueingObserver observer2 = new Utils.QueingObserver();
		SubscriptionObserver[] s2 = new SubscriptionObserver[res1.length];
		for ( int i = 0; i < res2.length; ++i) {
			s2[i] = new SubscriptionObserver(credentials, observer2, new SimpleResource(res2[i]), false);
		}
		Subscriptions subs = new Subscriptions();
		for ( SubscriptionObserver s : s1 ) {
			subs.subscribe(s);
		}
		for ( SubscriptionObserver s : s2 ) {
			subs.subscribe(s);
		}
		Set<SubscriptionObserver> result = subs.match(new SimpleResource("a/b/c"));
		Set<SubscriptionObserver> expected = new HashSet<SubscriptionObserver>(Arrays.asList(s1));
		expected.addAll(Arrays.asList(s2));
		assertEquals(expected,result);
		result = subs.match(new SimpleResource("a/b/d"));
		expected.clear();
		expected.addAll(Arrays.asList(s2));
		assertEquals(expected,result);
		result = subs.match(new SimpleResource("a/b/d/e"));
		expected.clear();
		expected.add(s2[2]);
		assertEquals(expected,result);
		result = subs.match(new SimpleResource("d/b/c"));
		expected.clear();
		expected.addAll(Arrays.asList(s1[1],s1[2],s2[1],s2[2]));
		assertEquals(expected,result);
		result = subs.match(new SimpleResource("d/e/f"));
		expected.clear();
		expected.addAll(Arrays.asList(s2[2]));
		assertEquals(expected,result);
		result = subs.match(new SimpleResource("c/d"));
		expected.clear();
		assertEquals(expected,result);
		// Test unsubscribe
		subs.unsubscribe(s1[0]);
		result = subs.match(new SimpleResource("a/b/c"));
		expected.clear();
		expected.addAll(Arrays.asList(s1[1],s1[2],s2[0],s2[1],s2[2]));
		assertEquals(expected,result);
		// Test unsubscribeAll
		subs.unsubscribeAll(observer1);
		result = subs.match(new SimpleResource("d/b/c"));
		expected.clear();
		expected.addAll(Arrays.asList(s2[1],s2[2]));
		assertEquals(expected,result);
		subs.unsubscribeAll(observer2);
		result = subs.match(new SimpleResource("d/b/c"));
		expected.clear();
		assertEquals(expected,result);
	}

	private Set<String> convertToStringSet(Set<SubscriptionObserver> subs) {
		Set<String> result = new HashSet<String>();
		for ( SubscriptionObserver sub : subs ) {
			result.add(sub.getResourcePattern().getName());
		}
		return result;
	}
}
