package org.springframework.samples.resources.handlebars;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

/**
 * @author Brian Clozel
 */
public class ProfileHelper implements Helper<Object> {

	public static final String NAME = "hasProfile";

	private final Set<String> profiles;

	public ProfileHelper(String[] profiles) {
		this.profiles = new HashSet<String>(Arrays.asList(profiles));
	}

	@Override
	public CharSequence apply(final Object context, final Options options)
			throws IOException {

		System.out.println("Handling charSequence: profile:  " + profiles.toString());
		if (profiles.contains(context)) {
			System.out.println("found profile: " + options.fn());
			return options.fn();
		} else {
			return options.inverse();
		}
	}
}
