package io.vaibhav.inbox;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import io.vaibhav.inbox.email.EmailRepository;
import io.vaibhav.inbox.email.EmailService;
import io.vaibhav.inbox.emailslist.EmailListItemRepository;
import io.vaibhav.inbox.folders.Folder;
import io.vaibhav.inbox.folders.FolderRepository;
import io.vaibhav.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
@RestController
public class InboxApp {

	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private EmailListItemRepository emailListItemRepository;

	@Autowired private EmailRepository emailRepository;

	@Autowired private UnreadEmailStatsRepository unreadEmailStatsRepository;

	@Autowired private EmailService emailService;

	public static void main(String[] args) {
		SpringApplication.run(InboxApp.class, args);
	}

	@RequestMapping("/user")
	/**
	principal represents the object for currently logged in user
	 */
	public String user(@AuthenticationPrincipal OAuth2User principal) {
		System.out.println(principal);
		return principal.getAttribute("login");
	}

//	This is necessary to have the spring boot app use the astra secure bundel
//			to connect to the database
	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}
	@Bean
	public DriverConfigLoaderBuilderCustomizer defaultProfile(){
		return builder -> builder.withString(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, "10 seconds").build();
	}
	@PostConstruct
	public void init() {
		folderRepository.save(new Folder("vaibhav-tomar", "Work", "blue"));
		folderRepository.save(new Folder("vaibhav-tomar", "Home", "green"));
		folderRepository.save(new Folder("vaibhav-tomar", "Family", "yellow"));


		for(int i=0; i<10; i++) {
			emailService.sendEmail("vaibhav-tomar", Arrays.asList("vaibhav-tomar", "abc"), "Hello" + i, "Body");
		}
	}
}
