package org.springframework.samples.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.samples.resources.handlebars.ProfileHelper;
import org.springframework.samples.resources.handlebars.ResourceUrlHelper;
import org.springframework.util.Assert;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private Environment env;

//	@Autowired
//	private GroovyMarkupViewResolver groovyMarkupViewResolver;

	@Autowired
	private ResourceUrlProvider urlProvider;

	@Value("${resources.projectroot:}")
	private String projectRoot;


	@Value("${config.AUTH_URL}")
	private String authUrl;

	@Value("${config.APP_VERSION}")
	private String appVersion;
	@Value("${config.MIXPANEL_TOKEN}")
	private String mixPanelToken;


	@Value("${authenticationserver.jdbc.driver}")
	String jdbcDriver;

	@Value("${authenticationserver.jdbc.url}")
	String jdbcUrl;

	@Value("${authenticationserver.jdbc.username}")
	String jdbcUsername;


	@Value("${authenticationserver.jdbc.password}")
	String jdbcPassword;

	@Value("${transloadit.key}")
	String transloaditKey;
	@Value("${transloadit.file.id}")
	String transloaditFileId;
	@Value("${transloadit.image.id}")
	String transloaditImageId;
	@Value("${transloadit.video.id}")
	String transloaditVideoId;


	//mix-panel



	private String getProjectRootRequired() {
		Assert.state(this.projectRoot != null, "Please set \"resources.projectRoot\" in application.yml");
		return this.projectRoot;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index");
	}

	@Bean
	public HandlebarsViewResolver handlebarsViewResolver() {
		HandlebarsViewResolver resolver = new HandlebarsViewResolver();
		resolver.setPrefix("classpath:/handlebars/");

		//TODO add partials path "classpath:/handlebars/partials ..


		resolver.registerHelper("src", new ResourceUrlHelper(this.urlProvider));
		resolver.registerHelper(ProfileHelper.NAME, new ProfileHelper(this.env.getActiveProfiles()));
		resolver.setCache(!this.env.acceptsProfiles("development"));
		resolver.setFailOnMissingFile(false);

		//app versioning..
		resolver.setAttributesMap(Collections.singletonMap("applicationVersion", getApplicationVersion()));
		resolver.setAttributesMap(Collections.singletonMap("appVersion", this.appVersion));
		//jdbc..
		resolver.setAttributesMap(Collections.singletonMap("jdbcUrl", this.jdbcUrl));
		resolver.setAttributesMap(Collections.singletonMap("jdbcUsername", this.jdbcUsername));
		resolver.setAttributesMap(Collections.singletonMap("jdbcPassword", this.jdbcPassword));
		resolver.setAttributesMap(Collections.singletonMap("jdbcDriver", this.jdbcDriver));
		//transloadit...
		resolver.setAttributesMap(Collections.singletonMap("transloaditKey", this.transloaditKey));
		resolver.setAttributesMap(Collections.singletonMap("transloaditFileId", this.transloaditFileId));
		resolver.setAttributesMap(Collections.singletonMap("transloaditImageId", this.transloaditImageId));
		//mix-panel..
		resolver.setAttributesMap(Collections.singletonMap("mixPanelToken", this.mixPanelToken));

		return resolver;
	}

	@Bean
	public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
		return new ResourceUrlEncodingFilter();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		boolean devMode = this.env.acceptsProfiles("development");

		String location = devMode ? "file:///" + getProjectRootRequired() + "/client/src/" : "classpath:static/";
		Integer cachePeriod = devMode ? 0 : null;
		boolean useResourceCache = !devMode;
		String version = getApplicationVersion();

		AppCacheManifestTransformer appCacheTransformer = new AppCacheManifestTransformer();
		VersionResourceResolver versionResolver = new VersionResourceResolver()
				.addFixedVersionStrategy(version, "/**/*.js", "/**/*.map")
				.addContentVersionStrategy("/**");

		registry.addResourceHandler("/**")
				.addResourceLocations(location)
				.setCachePeriod(cachePeriod)
				.resourceChain(useResourceCache)
					.addResolver(versionResolver)
					.addTransformer(appCacheTransformer);

	}

	protected String getApplicationVersion() {

		System.out.println("appVersion: " + this.appVersion);
//		return this.env.acceptsProfiles("development") ? "dev" : this.appVersion;
		return this.appVersion.isEmpty() ? "dev" : this.appVersion;

	}

}
