/**
 * 
 */
package edu.franklin.comp655.util;

import java.util.Collection;


/**
 * This class provides various types of existence checks
 * @author Don Swartwout
 *
 */
public class Is {
	public static boolean missing(String s) {
		return ( s == null || s.length() < 1 );
	}
	@SuppressWarnings("unchecked")
	public static boolean missing(Collection c) {
		return ( c == null || c.size() == 0 );
	}
	public static boolean missing(Object o) {
		return ( o == null );
	}
	public static boolean present(String s) {
		return !Is.missing(s);
	}
	@SuppressWarnings("unchecked")
	public static boolean present(Collection c) {
		return !Is.missing(c);
	}
	public static boolean present(Object o) {
		return !Is.missing(o);
	}
}
